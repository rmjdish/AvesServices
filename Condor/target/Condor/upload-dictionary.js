/*
   upload-dictionary.js - client-side parsing for the Upload Data
   Dictionary page (Help-UploadDictionary.html / uploadDictionary
   servlet).

   Adapted from OWL's basket.html upload panel (same file-parsing and
   validation logic: missing "NSHD Variable Name" column, missing
   names, invalid "Request variable" values, duplicate names), but
   deliberately narrower, since several things that page does have no
   equivalent need here:

   - No restricted-variable check before submitting. OWL can check a
     variable against a static JSON file fetched in the browser;
     Condor's restriction status lives in the database, only checked
     server-side. That check still happens - just one step later, on
     the Basket Management page shown after confirming, which already
     displays it with the reason for each one.
   - No "Sync linked sweeps" opt-in checkbox. Condor already expands
     linked variables automatically, server-side, whenever anything is
     added - see Variable.java's expandVars() - with the existing
     "Keep linked variables" toggle on the results page to remove them
     afterward if they're not wanted. There's nothing to opt into here.
   - No basket-size warning before submitting. Condor's own basket and
     save-basket pages already carry this messaging once the variables
     are actually in the session basket.
   - No localStorage basket and no client-side add step at all.
     Confirming builds one hidden form field per variable name (named
     after the variable itself) and submits it as a normal POST to
     Variable's existing endpoint - exactly what Multiple Add's own
     checkboxes already do - so every bit of server-side logic that
     already exists (expansion, restriction checks, the Basket
     Management results page) runs completely unchanged.
*/

window.addEventListener("load", function () {

  const NAME_COL = "nshd variable name";
  const REQUEST_COL = "request variable";
  const LABEL_COL_CANDIDATES = ["variable label", "variable_label", "label"];
  const EXCELJS_CDN_URL = "https://cdn.jsdelivr.net/npm/exceljs@4.4.0/dist/exceljs.min.js";

  let exceljsLoadPromise = null;
  let uploadParsedItems = []; // { name, label }

  function normCell(v) {
    return (v === null || v === undefined) ? "" : String(v).trim();
  }

  function findColIndex(headerRow, targetLower) {
    for (let i = 0; i < headerRow.length; i++) {
      if (normCell(headerRow[i]).toLowerCase() === targetLower) return i;
    }
    return -1;
  }

  function findFirstColIndex(headerRow, candidatesLower) {
    for (const c of candidatesLower) {
      const idx = findColIndex(headerRow, c);
      if (idx !== -1) return idx;
    }
    return -1;
  }

  function loadExcelJS() {
    if (window.ExcelJS) return Promise.resolve(window.ExcelJS);
    if (exceljsLoadPromise) return exceljsLoadPromise;
    exceljsLoadPromise = new Promise(function (resolve, reject) {
      const script = document.createElement("script");
      script.src = EXCELJS_CDN_URL;
      script.onload = function () {
        if (window.ExcelJS) resolve(window.ExcelJS);
        else reject(new Error("ExcelJS failed to initialise after loading."));
      };
      script.onerror = function () {
        reject(new Error("Failed to load ExcelJS from CDN."));
      };
      document.head.appendChild(script);
    });
    return exceljsLoadPromise;
  }

  function showUploadProgress(pct, label, indeterminate) {
    const container = document.getElementById("owlUploadProgressContainer");
    const fill = document.getElementById("owlUploadProgressFill");
    const labelEl = document.getElementById("owlUploadProgressLabel");
    container.style.display = "block";
    labelEl.style.display = "block";
    labelEl.textContent = label;
    if (indeterminate) {
      fill.classList.add("owl-indeterminate");
      fill.textContent = "";
    } else {
      fill.classList.remove("owl-indeterminate");
      fill.style.width = pct + "%";
      fill.textContent = pct + "%";
    }
  }

  function hideUploadProgress() {
    document.getElementById("owlUploadProgressContainer").style.display = "none";
    document.getElementById("owlUploadProgressLabel").style.display = "none";
    const fill = document.getElementById("owlUploadProgressFill");
    fill.classList.remove("owl-indeterminate");
    fill.style.width = "0%";
    fill.textContent = "0%";
  }

  function resetUploadPanel() {
    document.getElementById("owlUploadStatus").textContent = "";
    document.getElementById("owlMissingNameBox").style.display = "none";
    document.getElementById("owlInvalidValueBox").style.display = "none";
    document.getElementById("owlDuplicateBox").style.display = "none";
    document.getElementById("owlUploadPreview").style.display = "none";
    document.getElementById("owlUploadNote").style.display = "none";
    uploadParsedItems = [];
  }

  function setUploadIntroVisible(visible) {
    const intro = document.getElementById("owlUploadIntro");
    if (intro) intro.style.display = visible ? "" : "none";
  }

  function handleUploadFile(file, uploadBtn) {
    resetUploadPanel();
    uploadBtn.disabled = true;
    setUploadIntroVisible(false);
    showUploadProgress(0, "Reading file... 0%", false);

    const isCsv = /\.csv$/i.test(file.name);
    const reader = new FileReader();

    reader.onprogress = function (e) {
      if (e.lengthComputable) {
        const pct = Math.round((e.loaded / e.total) * 100);
        showUploadProgress(pct, "Reading file... " + pct + "%", false);
      }
    };

    reader.onerror = function () {
      hideUploadProgress();
      document.getElementById("owlUploadStatus").textContent = "Could not read the file.";
      uploadBtn.disabled = false;
    };

    reader.onload = function (e) {
      showUploadProgress(100, "Parsing file, please wait...", true);
      setTimeout(function () {
        if (isCsv) {
          parseUploadCsv(e.target.result, uploadBtn);
        } else {
          parseUploadXlsx(e.target.result, uploadBtn);
        }
      }, 30);
    };

    if (isCsv) {
      reader.readAsText(file);
    } else {
      reader.readAsArrayBuffer(file);
    }
  }

  function parseUploadCsv(text, uploadBtn) {
    const rows = text.split(/\r?\n/)
      .filter(l => l.trim() !== "")
      .map(l => l.split(",").map(c => c.replace(/^"|"$/g, "").trim()));
    processUploadRows(rows, uploadBtn);
  }

  async function parseUploadXlsx(arrayBuffer, uploadBtn) {
    function fail(msg) {
      hideUploadProgress();
      document.getElementById("owlUploadStatus").textContent = msg;
      uploadBtn.disabled = false;
    }

    let ExcelJS;
    try {
      ExcelJS = await loadExcelJS();
    } catch (err) {
      fail("Could not load the spreadsheet reader. Check your connection and try again.");
      return;
    }

    const workbook = new ExcelJS.Workbook();
    try {
      await workbook.xlsx.load(arrayBuffer);
    } catch (err) {
      fail("Could not parse this file as a spreadsheet.");
      return;
    }

    const sheet = workbook.getWorksheet("Data_dictionary") || workbook.worksheets[0];
    if (!sheet) {
      fail("No sheet found in this spreadsheet.");
      return;
    }

    const rows = [];
    sheet.eachRow({ includeEmpty: false }, function (row) {
      const values = row.values.slice(1); // ExcelJS rows are 1-indexed; drop the leading undefined
      rows.push(values.map(function (v) {
        if (v === null || v === undefined) return "";
        if (typeof v === "object" && v.text !== undefined) return v.text; // rich text cells
        return v;
      }));
    });

    if (rows.length < 2) {
      fail("No data rows found in the spreadsheet.");
      return;
    }

    processUploadRows(rows, uploadBtn);
  }

  function processUploadRows(rows, uploadBtn) {
    function fail(msg) {
      hideUploadProgress();
      document.getElementById("owlUploadStatus").textContent = msg;
      uploadBtn.disabled = false;
    }

    const headerRow = rows[0];
    const nameIdx = findColIndex(headerRow, NAME_COL);
    const labelIdx = findFirstColIndex(headerRow, LABEL_COL_CANDIDATES);
    const reqIdx = findColIndex(headerRow, REQUEST_COL);

    if (nameIdx === -1) {
      fail('Could not find a "NSHD Variable Name" column. Check the column headers in the file.');
      return;
    }

    const requireY = reqIdx !== -1;

    uploadParsedItems = [];
    const invalidRequestValues = [];
    const missingNameRows = [];
    const duplicateNames = [];
    const seen = new Set();

    for (let i = 1; i < rows.length; i++) {
      const row = rows[i] || [];
      const name = normCell(row[nameIdx]);
      const reqVal = requireY ? normCell(row[reqIdx]) : "";

      if (requireY) {
        if (reqVal === "") continue; // not requested, skip silently
        if (reqVal !== "Y") {
          invalidRequestValues.push({ row: i + 1, name: name, value: reqVal });
          continue;
        }
        if (name === "") {
          missingNameRows.push(i + 1);
          continue;
        }
      } else {
        if (name === "") continue; // blank row in a plain basket file, skip silently
      }

      if (seen.has(name)) {
        duplicateNames.push(name);
        continue;
      }
      seen.add(name);

      const label = labelIdx !== -1 ? normCell(row[labelIdx]) : "";
      uploadParsedItems.push({ name: name, label: label });
    }

    if (missingNameRows.length > 0) {
      document.getElementById("owlMissingNameSummary").textContent =
        missingNameRows.length + " row(s) marked Y have no NSHD Variable Name (skipped):";
      document.getElementById("owlMissingNameDetail").textContent =
        "Rows: " + missingNameRows.slice(0, 50).join(", ") +
        (missingNameRows.length > 50 ? ", ...and " + (missingNameRows.length - 50) + " more" : "");
      document.getElementById("owlMissingNameBox").style.display = "block";
    }

    if (invalidRequestValues.length > 0) {
      document.getElementById("owlInvalidValueSummary").textContent =
        invalidRequestValues.length + ' variable(s) have something other than Y in "Request variable" (skipped):';
      const list = invalidRequestValues.slice(0, 50).map(function (r) {
        return (r.name !== "" ? r.name : "row " + r.row + " (no name)") + " \u2192 \"" + r.value + "\"";
      });
      document.getElementById("owlInvalidValueDetail").textContent =
        list.join(", ") + (invalidRequestValues.length > 50 ? ", ...and " + (invalidRequestValues.length - 50) + " more" : "");
      document.getElementById("owlInvalidValueBox").style.display = "block";
    }

    if (duplicateNames.length > 0) {
      document.getElementById("owlDuplicateSummary").textContent =
        duplicateNames.length + " duplicate variable name(s) in the file were skipped after the first occurrence:";
      document.getElementById("owlDuplicateDetail").textContent =
        duplicateNames.slice(0, 50).join(", ") +
        (duplicateNames.length > 50 ? ", ...and " + (duplicateNames.length - 50) + " more" : "");
      document.getElementById("owlDuplicateBox").style.display = "block";
    }

    hideUploadProgress();
    uploadBtn.disabled = false;

    if (uploadParsedItems.length === 0) {
      document.getElementById("owlUploadStatus").textContent = requireY
        ? 'No valid rows marked "Y" with a variable name were found.'
        : "No variable names were found in this file.";
      return;
    }

    document.getElementById("owlUploadVarCount").textContent = uploadParsedItems.length;
    document.getElementById("owlUploadPreviewBody").innerHTML = uploadParsedItems.map(function (item, idx) {
      return "<tr><td>" + (idx + 1) + "</td><td>" + item.name + "</td><td>" + item.label + "</td></tr>";
    }).join("");
    document.getElementById("owlUploadPreview").style.display = "block";
    document.getElementById("owlUploadNote").style.display = "block";
  }

  function closeUploadPanel() {
    const fileInput = document.getElementById("owlUploadFile");
    if (fileInput) fileInput.value = "";
    resetUploadPanel();
    hideUploadProgress();
    setUploadIntroVisible(true);
  }

  // Builds the hidden form (one input per variable name, named after
  // the variable itself) and submits it - see the template's own
  // comment on #owlUploadForm for why this is enough on its own to
  // reuse Variable's existing Multiple Add handling untouched.
  function submitToBasket() {
    if (uploadParsedItems.length === 0) return;
    const form = document.getElementById("owlUploadForm");
    form.innerHTML = "";
    uploadParsedItems.forEach(function (item) {
      const input = document.createElement("input");
      input.type = "hidden";
      input.name = item.name;
      input.value = "1";
      form.appendChild(input);
    });
    // Shown right before navigating away, not any earlier - the server-
    // side add (expansion + restriction checks for every variable) can
    // take a while for a large file, same as Multiple Add elsewhere on
    // the site, and there's nothing to show progress on client-side
    // once the form has actually submitted.
    const overlay = document.getElementById("owlWaitOverlay");
    if (overlay) overlay.style.display = "flex";
    form.submit();
  }

  const uploadBtn = document.getElementById("owlUploadBtn");
  const uploadFileInput = document.getElementById("owlUploadFile");
  if (uploadBtn && uploadFileInput) {
    uploadBtn.addEventListener("click", function () {
      resetUploadPanel();
      setUploadIntroVisible(true);
      uploadFileInput.click();
    });
    uploadFileInput.addEventListener("change", function () {
      if (uploadFileInput.files[0]) {
        handleUploadFile(uploadFileInput.files[0], uploadBtn);
      }
    });
  }

  ["owlConfirmUploadBtn", "owlConfirmUploadBtnBottom"].forEach(function (id) {
    const btn = document.getElementById(id);
    if (btn) btn.addEventListener("click", submitToBasket);
  });

  ["owlCancelUploadBtn", "owlCancelUploadBtnBottom"].forEach(function (id) {
    const btn = document.getElementById(id);
    if (btn) btn.addEventListener("click", closeUploadPanel);
  });

});
