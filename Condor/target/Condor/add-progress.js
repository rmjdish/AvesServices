/* add-progress.js
 *
 * Shows "Adding N of M" with a progress bar on the full-page "please wait"
 * overlay while a Multiple Add is running.
 *
 * How it works: the browser still submits the form normally and waits for
 * the result page, exactly as before. Alongside that, this script asks the
 * server every half second how far the add has got (GET
 * variable?command=addProgress&token=...), and Variable.doPost() answers
 * from a counter it updates after each variable. The old page stays alive
 * until the result page starts arriving, so the polling keeps running right
 * up to the moment the browser navigates away; nothing needs stopping.
 *
 * Used by Search results (Multiple Add) and Upload Data Dictionary.
 *
 * Usage:   OwlAddProgress.start(form, expectedCount)
 *   form           the <form> that is about to be submitted
 *   expectedCount  how many variables were selected (shown until the
 *                  server reports its own total)
 * Call it immediately before the form submits. If this script fails to
 * load, callers fall back to the plain spinner, so the add still works.
 */
(function () {
  "use strict";

  var POLL_MS = 500;
  var TOKEN_FIELD = "addtoken"; // must match Variable.ADD_TOKEN_PARAM

  function newToken() {
    var rnd = "";
    if (window.crypto && window.crypto.getRandomValues) {
      var a = new Uint32Array(2);
      window.crypto.getRandomValues(a);
      rnd = a[0].toString(36) + a[1].toString(36);
    } else {
      rnd = Math.random().toString(36).slice(2);
    }
    return Date.now().toString(36) + "-" + rnd;
  }

  function el(tag, cls, text) {
    var e = document.createElement(tag);
    if (cls) e.className = cls;
    if (text !== undefined) e.textContent = text;
    return e;
  }

  function fmt(n) {
    try { return Number(n).toLocaleString("en-GB"); } catch (e) { return String(n); }
  }

  // Adds the bar and counter to the existing overlay box, once.
  function buildBar(box) {
    var existing = box.querySelector(".owl-progress");
    if (existing) {
      return {
        fill: existing.querySelector(".owl-progress-fill"),
        track: existing,
        count: box.querySelector(".owl-progress-count")
      };
    }
    var track = el("div", "owl-progress");
    track.setAttribute("role", "progressbar");
    track.setAttribute("aria-valuemin", "0");
    track.setAttribute("aria-valuemax", "100");
    track.setAttribute("aria-valuenow", "0");
    var fill = el("div", "owl-progress-fill");
    track.appendChild(fill);
    var count = el("p", "owl-progress-count");
    count.setAttribute("aria-live", "polite");

    // Sits under the "Adding your variables…" line, above the subtext.
    var sub = box.querySelector(".owl-wait-subtext");
    if (sub) {
      box.insertBefore(track, sub);
      box.insertBefore(count, sub);
    } else {
      box.appendChild(track);
      box.appendChild(count);
    }
    return { fill: fill, track: track, count: count };
  }

  function render(ui, done, total) {
    var pct = total > 0 ? Math.min(100, Math.round((done / total) * 100)) : 0;
    ui.fill.style.width = pct + "%";
    ui.track.setAttribute("aria-valuenow", String(pct));
    ui.count.textContent = "Adding " + fmt(done) + " of " + fmt(total) +
      " (" + pct + "%)";
  }

  function start(form, expectedCount) {
    var overlay = document.getElementById("owlWaitOverlay");
    if (!overlay || !form) return;

    var token = newToken();

    // One extra field on the form. Variable.doPost() skips it when it
    // collects variable names, and uses it to match progress to this add.
    var old = form.querySelector('input[name="' + TOKEN_FIELD + '"]');
    if (old) old.parentNode.removeChild(old);
    var field = document.createElement("input");
    field.type = "hidden";
    field.name = TOKEN_FIELD;
    field.value = token;
    form.appendChild(field);

    var box = overlay.querySelector(".owl-wait-box") || overlay;
    var ui = buildBar(box);
    var total = expectedCount > 0 ? expectedCount : 0;
    render(ui, 0, total);
    overlay.style.display = "flex";

    if (!window.fetch) return; // plain spinner only on very old browsers

    var url = "variable?command=addProgress&token=" + encodeURIComponent(token);
    var stopped = false;
    var failures = 0;

    function poll() {
      if (stopped) return;
      fetch(url, { credentials: "same-origin", cache: "no-store" })
        .then(function (r) { return r.ok ? r.json() : Promise.reject(r.status); })
        .then(function (p) {
          failures = 0;
          // "started" is false until the server has begun THIS add, so we
          // never show a stale count from an earlier one.
          if (p && p.started && p.total > 0) {
            total = p.total;
            render(ui, p.done, total);
          }
        })
        .catch(function () {
          // A failed poll just means no update this time. The add itself
          // is unaffected; give up polling after a run of failures.
          failures++;
          if (failures >= 10) stopped = true;
        })
        .then(function () {
          if (!stopped) setTimeout(poll, POLL_MS);
        });
    }
    setTimeout(poll, POLL_MS);

    // Navigating to the results page tears this page down; stop politely.
    window.addEventListener("pagehide", function () { stopped = true; });
  }

  window.OwlAddProgress = { start: start };
})();
