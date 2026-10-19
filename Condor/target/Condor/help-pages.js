/* help-pages.js
   Shared behaviour for in-app Help pages: FAQ accordion, sidebar
   scroll-highlighting, and a "top of page" link inside the sidebar.
   Framework-free, matching the style of treeview-navigation.js.
   Nothing here is hardcoded to any one page - it discovers sections
   and sidebar links generically, so it applies automatically to
   every current and future page that links this file.
*/

document.addEventListener('DOMContentLoaded', function () {

  /* ---- FAQ accordion ---- */

  var questions = document.querySelectorAll('.help-faq-question');
  for (var i = 0; i < questions.length; i++) {
    questions[i].addEventListener('click', function () {
      var item = this.closest('.help-faq-item');
      item.classList.toggle('open');
    });
  }

  /* ---- Sidebar scroll-highlighting ---- */

  var sidebarLinks = document.querySelectorAll('.help-sidebar nav a[href^="#"]');
  if (sidebarLinks.length > 0) {
    var sections = [];
    for (var j = 0; j < sidebarLinks.length; j++) {
      var id = sidebarLinks[j].getAttribute('href').slice(1);
      var el = document.getElementById(id);
      if (el) {
        sections.push({ link: sidebarLinks[j], el: el });
      }
    }

    // The vertical line (in pixels from the top of the viewport) used
    // to decide which section(s) count as "active". Sections can sit
    // close together - cards side-by-side in the same grid row share
    // almost the same top/bottom - so rather than picking one winner,
    // this highlights every section whose vertical span currently
    // contains the line. For a single-column section that's normally
    // just one link; for a row of grid cards it's correctly all of
    // them at once, since they really are all in view together.
    var ACTIVE_LINE = 220;

    var ticking = false;

    function updateActiveLinks() {
      var anyActive = false;
      for (var k = 0; k < sections.length; k++) {
        var rect = sections[k].el.getBoundingClientRect();
        var isActive = rect.top <= ACTIVE_LINE && rect.bottom > ACTIVE_LINE;
        sections[k].link.classList.toggle('active', isActive);
        if (isActive) anyActive = true;
      }
      // Before the first section reaches the line yet (top of page),
      // default to the first entry so something is always highlighted.
      if (!anyActive && sections.length > 0) {
        sections[0].link.classList.add('active');
      }
      ticking = false;
    }

    window.addEventListener('scroll', function () {
      if (!ticking) {
        window.requestAnimationFrame(updateActiveLinks);
        ticking = true;
      }
    });

    updateActiveLinks();
  }

  /* ---- "Top of page" link, inside the sidebar ---- */

  var sidebars = document.querySelectorAll('.help-sidebar');
  for (var s = 0; s < sidebars.length; s++) {
    var topLink = document.createElement('a');
    topLink.href = '#';
    topLink.className = 'help-sidebar-totop';
    topLink.innerHTML = '&uarr; Top of page';
    topLink.addEventListener('click', function (e) {
      e.preventDefault();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
    sidebars[s].appendChild(topLink);
  }

  /* ---- Walkthrough step expand/collapse ----
     Same click-to-toggle-a-class mechanic as the FAQ accordion above,
     just applied to .walkthrough-step instead of .help-faq-item -
     this is a separate handler because it targets a different
     selector, not something the FAQ code above already covers. */

  var walkthroughStepHeaders = document.querySelectorAll('.walkthrough-step-header');
  for (var wh = 0; wh < walkthroughStepHeaders.length; wh++) {
    walkthroughStepHeaders[wh].addEventListener('click', function () {
      var step = this.closest('.walkthrough-step');
      step.classList.toggle('open');
    });
  }

  /* ---- Walkthrough progress tracking ----
     Generic: works for any .walkthrough element containing
     .walkthrough-step blocks, each carrying a data-step-id and a
     .walkthrough-mark-done control. Progress is stored in this
     browser's localStorage only (see the callout on the page itself)
     - there is no server-side record of it, by design, since this is
     a per-visitor convenience, not data that needs to persist across
     devices or be visible to anyone else.

     A separate, always-visible checklist can sit anywhere else on the
     page (e.g. in the sidebar) showing the same progress - link it to
     a walkthrough by giving it data-walkthrough-target="<the
     walkthrough's id>", with .walkthrough-checklist-item children
     each carrying a matching data-step-id. This is entirely optional;
     a walkthrough works fine with no checklist present at all. */

  var walkthroughs = document.querySelectorAll('.walkthrough');
  for (var w = 0; w < walkthroughs.length; w++) {
    (function (walkthrough) {
      var storageKey = 'owl-walkthrough-progress:' + (walkthrough.id || 'default');
      var steps = walkthrough.querySelectorAll('.walkthrough-step');
      var checklist = walkthrough.id
        ? document.querySelector('.walkthrough-checklist[data-walkthrough-target="' + walkthrough.id + '"]')
        : null;
      var checklistItems = checklist ? checklist.querySelectorAll('.walkthrough-checklist-item') : [];
      var counter = checklist ? checklist.querySelector('.walkthrough-progress-count') : null;

      function loadProgress() {
        try {
          var raw = window.localStorage.getItem(storageKey);
          return raw ? JSON.parse(raw) : [];
        } catch (e) {
          return [];
        }
      }

      function saveProgress(done) {
        try {
          window.localStorage.setItem(storageKey, JSON.stringify(done));
        } catch (e) {
          // localStorage unavailable (private browsing, disabled, etc.)
          // - progress simply won't persist for this visit. Nothing to
          // do here; the walkthrough still works, it just won't remember.
        }
      }

      function toggleStep(stepId) {
        var done = loadProgress();
        var idx = done.indexOf(stepId);
        if (idx === -1) {
          done.push(stepId);
        } else {
          done.splice(idx, 1); // clicking again un-marks it
        }
        saveProgress(done);
        render();
      }

      function render() {
        var done = loadProgress();
        var doneCount = 0;
        for (var i = 0; i < steps.length; i++) {
          var stepId = steps[i].getAttribute('data-step-id');
          var isDone = done.indexOf(stepId) !== -1;
          steps[i].classList.toggle('done', isDone);
          var badge = steps[i].querySelector('.walkthrough-step-badge');
          if (badge) {
            badge.textContent = isDone ? '\u2713' : (i + 1);
          }
          var markDone = steps[i].querySelector('.walkthrough-mark-done');
          if (markDone) {
            markDone.textContent = isDone ? 'Marked as done' : 'Mark as done';
          }
          if (isDone) doneCount++;
        }
        for (var k = 0; k < checklistItems.length; k++) {
          var itemStepId = checklistItems[k].getAttribute('data-step-id');
          checklistItems[k].classList.toggle('done', done.indexOf(itemStepId) !== -1);
        }
        if (counter) {
          counter.textContent = doneCount + ' of ' + steps.length + ' steps completed';
        }
      }

      for (var j = 0; j < steps.length; j++) {
        var markDoneLink = steps[j].querySelector('.walkthrough-mark-done');
        if (markDoneLink) {
          markDoneLink.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation(); // don't also trigger the accordion toggle
            var step = this.closest('.walkthrough-step');
            toggleStep(step.getAttribute('data-step-id'));
          });
        }
      }

      for (var m = 0; m < checklistItems.length; m++) {
        checklistItems[m].addEventListener('click', function () {
          toggleStep(this.getAttribute('data-step-id'));
        });
      }

      render();
    })(walkthroughs[w]);
  }

  /* ---- Sortable table headers ----
     Generic: applies to every .owl-table on the page automatically,
     no per-page setup needed. Click a header to sort by that column
     (ascending), click again for descending, click a different
     header to switch columns. Numeric-looking cell content sorts
     numerically; everything else sorts as text.

     Uses event delegation (one listener on the table itself, not on
     each <th>) rather than attaching a listener directly to each
     header. This was a genuine bug in the first version: some of
     these same tables already run a third-party jQuery
     ddTableFilter() widget, which rebuilds the header row (adding
     its own per-column filter dropdowns) after this script's
     DOMContentLoaded listener had already run and attached its
     click handlers directly to the original <th> elements -
     ddTableFilter's rebuild replaced those elements, silently
     destroying the listeners along with them. A listener on the
     table itself survives that rebuild; only the header cells
     underneath it were being replaced, not the table. The column
     index is also recalculated at click time rather than cached
     from page load, for the same reason - a rebuilt header row may
     not have the same cell structure it started with. */

  var sortableTables = document.querySelectorAll('.owl-table');
  for (var st = 0; st < sortableTables.length; st++) {
    (function (table) {
      var currentSortCol = null;
      var currentSortDir = 'asc';

      function getHeaderRow() {
        return table.querySelector('tr');
      }

      function getDataRows() {
        var allRows = table.querySelectorAll('tr');
        var dataRows = [];
        for (var i = 0; i < allRows.length; i++) {
          if (allRows[i].querySelector('td')) {
            dataRows.push(allRows[i]);
          }
        }
        return dataRows;
      }

      table.addEventListener('click', function (event) {
        var th = event.target.closest ? event.target.closest('th') : null;
        if (!th) return;
        var headerRow = getHeaderRow();
        if (!headerRow || th.parentNode !== headerRow) return; // only the actual header row's <th> cells sort

        var headers = headerRow.querySelectorAll('th');
        var colIndex = -1;
        for (var i = 0; i < headers.length; i++) {
          if (headers[i] === th) { colIndex = i; break; }
        }
        if (colIndex === -1) return;

        var dir = (currentSortCol === colIndex && currentSortDir === 'asc') ? 'desc' : 'asc';
        currentSortCol = colIndex;
        currentSortDir = dir;

        var rows = getDataRows();
        rows.sort(function (a, b) {
          var aCell = a.children[colIndex];
          var bCell = b.children[colIndex];
          var aText = aCell ? aCell.textContent.trim() : '';
          var bText = bCell ? bCell.textContent.trim() : '';
          var aNum = parseFloat(aText);
          var bNum = parseFloat(bText);
          var cmp;
          if (!isNaN(aNum) && !isNaN(bNum) && aText !== '' && bText !== '') {
            cmp = aNum - bNum;
          } else {
            cmp = aText.localeCompare(bText, undefined, { sensitivity: 'base' });
          }
          return dir === 'asc' ? cmp : -cmp;
        });

        var appendTarget = table.tBodies && table.tBodies[0] ? table.tBodies[0] : table;
        for (var r = 0; r < rows.length; r++) {
          appendTarget.appendChild(rows[r]);
        }

        for (var hh = 0; hh < headers.length; hh++) {
          headers[hh].classList.remove('owl-sort-asc', 'owl-sort-desc');
        }
        th.classList.add(dir === 'asc' ? 'owl-sort-asc' : 'owl-sort-desc');
      });
    })(sortableTables[st]);
  }

});
