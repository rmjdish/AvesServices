/* Category tree page (design A2): tick boxes for MULTIPLE categories, a sticky
   "Selected" panel with full paths, filter, expand/collapse all.

   Ticking a parent ticks all its sub-categories (and unticking clears them); a parent
   shows as ticked only when all its children are. Parent counts show the total of their
   sub-categories. The tree starts collapsed.

   Selection is handled here, not by treeitemClick.js: clicks inside the tree are
   intercepted in the capture phase so the old single-select handler never runs.

   What is submitted: every selected category is sent as its own "category" field,
   in the same text format the old handler used (e.g. "Blood biochemistry[2011] has
   199 items"). The first one goes in the existing #last_action field, the rest as
   extra hidden fields, so a server that reads only getParameter("category") still
   searches the first category; reading getParameterValues("category") gets them all. */
(function () {
  function init() {
    var tree = document.querySelector(".owl-cat-tree"), panel = document.getElementById("owlCatPanel");
    if (!tree || !panel) return;
    var $ = function (id) { return document.getElementById(id); };
    var form = tree.closest("form");
    var items = [].slice.call(tree.querySelectorAll('[role="treeitem"]'));
    var selected = new Set();                /* treeitems that are ticked */

    function row(it) { return it.firstElementChild; }
    function isSection(it) { return it.hasAttribute("aria-expanded"); }
    function parent(it) { return it.parentElement && it.parentElement.closest('[role="treeitem"]'); }
    function ancestors(it) { var a = [], p = parent(it); while (p) { a.unshift(p); p = parent(p); } return a; }

    /* read name + count once, before anything is injected into the rows */
    items.forEach(function (it) {
      var c = row(it).cloneNode(true), n = c.querySelector(".owl-tree-count");
      it._count = n ? n.textContent.trim() : "";
      if (n) n.remove();
      it._name = c.textContent.replace(/\s+/g, " ").trim();
      it._value = it._name + it._count;      /* same text the old handler posted */
      var r = row(it);
      var ctl = document.createElement("span");
      ctl.className = "owl-cat-ctl";
      if (isSection(it)) {
        var b = document.createElement("button");
        b.type = "button"; b.className = "owl-cat-tog"; b.setAttribute("aria-label", "Expand or collapse " + it._name);
        ctl.appendChild(b);
      } else {
        var sp = document.createElement("i"); sp.className = "owl-cat-sp"; ctl.appendChild(sp);
      }
      var cb = document.createElement("input");
      cb.type = "checkbox"; cb.className = "owl-cat-cb"; cb.setAttribute("aria-label", "Select " + it._name);
      ctl.appendChild(cb);
      r.insertBefore(ctl, r.firstChild);
      it._cb = cb;
    });

    /* parent counts: the server reports 0 for parents, so show the total of everything below */
    function num(it) { var m = /has\s+([\d,]+)\s+item/.exec(it._count); return m ? parseInt(m[1].replace(/,/g, ""), 10) : 0; }
    function kids(it) { return [].slice.call(it.querySelectorAll(":scope > ul > li[role=\"treeitem\"]")); }
    function total(it) { var k = kids(it); return num(it) + k.reduce(function (s, c) { return s + total(c); }, 0); }
    items.forEach(function (it) {
      if (!isSection(it)) { it._total = num(it); return; }
      it._own = num(it);
      it._total = total(it);
      it._sub = it._total - it._own;
      var id = (/^\[[^\]]*\]/.exec(it._count) || [""])[0];
      var n = row(it).querySelector(".owl-tree-count");
      if (n) {
        n.innerHTML = "";
        var own = document.createElement("b"); own.textContent = (id ? id + " " : "") + it._own + " here";
        var sub = document.createElement("i"); sub.textContent = " \u00b7 " + it._sub + " in sub-categories";
        n.appendChild(own); n.appendChild(sub);
        n.title = it._own + " variables directly in this category, " + it._sub + " more in its sub-categories";
      }
      it._count = it._own + " here \u00b7 " + it._sub + " in sub-categories";
    });
    items.forEach(function (it) { if (isSection(it)) it.setAttribute("aria-expanded", "false"); });

    function path(it) { return ancestors(it).map(function (a) { return a._name; }); }

    /* highest fully-ticked items: what the panel lists */
    function effective() {
      return items.filter(function (it) { return selected.has(it) && !(parent(it) && selected.has(parent(it))); });
    }
    function descendants(it) { return [].slice.call(it.querySelectorAll('[role="treeitem"]')); }

    /* keep the form fields in step with the selection */
    var extra = document.createElement("span");
    extra.id = "owlCatExtra"; extra.hidden = true; form.appendChild(extra);
    function syncForm() {
      var all = items.filter(function (it) { return selected.has(it); });   /* every ticked category, tree order */
      var first = document.getElementById("last_action");
      if (first) first.value = all.length ? all[0]._value : "";
      extra.innerHTML = "";
      all.slice(1).forEach(function (it) {
        var h = document.createElement("input");
        h.type = "hidden"; h.name = "category"; h.value = it._value; extra.appendChild(h);
      });
      return all.length;
    }

    var list = panel.querySelector(".owl-cat-list"), empty = panel.querySelector(".owl-cat-empty"),
        totalEl = panel.querySelector(".owl-cat-total"), btn = $("owlCatPanelSearch");

    /* remember the tree between visits: restored only when the page is reached with
       the browser's Back/Forward buttons, so a fresh visit always starts clean */
    var KEY = "owlCatState";
    function idOf(it) { var m = /\[(\d+)\]/.exec(it._value); return m ? m[1] : null; }
    function save() {
      try {
        sessionStorage.setItem(KEY, JSON.stringify({
          sel: items.filter(function (it) { return selected.has(it); }).map(idOf),
          open: items.filter(function (it) { return isSection(it) && it.getAttribute("aria-expanded") === "true"; }).map(idOf),
          q: $("owlCatFilter").value,
          y: window.pageYOffset
        }));
      } catch (e) {}
    }
    function cameBack() {
      var nav = window.performance && performance.getEntriesByType ? performance.getEntriesByType("navigation")[0] : null;
      return nav ? nav.type === "back_forward" : !!(window.performance && performance.navigation && performance.navigation.type === 2);
    }
    function restore() {
      var st = null;
      try { st = JSON.parse(sessionStorage.getItem(KEY) || "null"); } catch (e) {}
      if (!st || !cameBack()) return null;
      var sel = {}, open = {};
      (st.sel || []).forEach(function (i) { sel[i] = 1; });
      (st.open || []).forEach(function (i) { open[i] = 1; });
      items.forEach(function (it) {
        var id = idOf(it);
        if (sel[id]) selected.add(it);
        if (isSection(it) && open[id]) it.setAttribute("aria-expanded", "true");
      });
      return st;
    }

    function render() {
      items.forEach(function (it) {
        var on = selected.has(it);
        it._cb.checked = on; it.classList.toggle("owl-cat-sel", on);
        var some = !on && isSection(it) && descendants(it).some(function (d) { return selected.has(d); });
        it._cb.indeterminate = some;
      });
      var eff = effective(), all = syncForm();
      $("owlCatN").textContent = eff.length;
      empty.hidden = eff.length > 0;
      list.innerHTML = "";
      eff.forEach(function (it) {
        var anc = path(it), d = document.createElement("div");
        d.className = "owl-cat-item";
        var subs = descendants(it).length;
        if (subs) d.title = "Includes " + subs + (subs === 1 ? " sub-category" : " sub-categories");
        var main = document.createElement("div"); main.className = "owl-cat-main";
        var a = document.createElement("div"); a.className = "owl-cat-anc"; a.textContent = anc.length ? anc.join(" \u203a ") + " \u203a" : "Top level";
        var n = document.createElement("div"); n.className = "owl-cat-name"; n.textContent = it._name;
        var c = document.createElement("span"); c.className = "owl-cat-cnt"; c.textContent = it._count ? "  \u00b7  " + it._count : "";
        n.appendChild(c);
        main.appendChild(a); main.appendChild(n);
        var act = document.createElement("div"); act.className = "owl-cat-actions";
        var show = document.createElement("button"); show.type = "button"; show.className = "owl-cat-show"; show.textContent = "Show in tree";
        show.onclick = function () { reveal(it); };
        var rm = document.createElement("button"); rm.type = "button"; rm.className = "owl-cat-remove"; rm.innerHTML = "&times;";
        rm.setAttribute("aria-label", "Remove " + it._name);
        rm.onclick = function () { toggle(it, false); };
        act.appendChild(show); act.appendChild(rm);
        d.appendChild(main); d.appendChild(act);
        list.appendChild(d);
      });
      totalEl.hidden = all === eff.length;
      totalEl.textContent = all + " categories in total, including sub-categories";
      save();
      btn.disabled = !eff.length;
      btn.textContent = eff.length ? "Search " + eff.length + (eff.length === 1 ? " category" : " categories") : "Select a category to search";
    }

    function toggle(it, on) {
      if (on === undefined) on = !selected.has(it);
      [it].concat(descendants(it)).forEach(function (x) { if (on) selected.add(x); else selected.delete(x); });
      /* a parent is ticked only while all of its children are */
      var p = parent(it);
      while (p) {
        var allKids = kids(p).every(function (k) { return selected.has(k); });
        if (allKids) selected.add(p); else selected.delete(p);
        p = parent(p);
      }
      render();
    }

    function reveal(it) {
      $("owlCatFilter").value = ""; items.forEach(function (x) { x.classList.remove("owl-cat-hide"); });
      nomatch.hidden = true;
      ancestors(it).forEach(function (a) { a.setAttribute("aria-expanded", "true"); });
      row(it).scrollIntoView({ block: "center", behavior: "smooth" });
    }

    /* clicks in the tree: intercepted so the old single-select handler is skipped */
    tree.addEventListener("click", function (e) {
      var it = e.target.closest && e.target.closest('[role="treeitem"]');
      if (!it || !tree.contains(it)) return;
      var r = row(it);
      if (!r.contains(e.target)) return;
      e.stopPropagation();
      if (e.target.closest(".owl-cat-tog")) {
        e.preventDefault();
        it.setAttribute("aria-expanded", it.getAttribute("aria-expanded") === "true" ? "false" : "true");
        return;
      }
      if (e.target.classList.contains("owl-cat-cb")) { toggle(it, e.target.checked); return; }
      toggle(it);
    }, true);

    $("owlCatClear").onclick = function () { selected.clear(); render(); };

    function setAll(v) { items.forEach(function (it) { if (isSection(it)) it.setAttribute("aria-expanded", v); }); }
    $("owlCatExpand").onclick = function () { setAll("true"); };
    $("owlCatCollapse").onclick = function () { setAll("false"); };

    var nomatch = tree.querySelector(".owl-cat-nomatch");
    $("owlCatFilter").addEventListener("input", function () {
      var q = this.value.trim().toLowerCase(), any = true;
      items.forEach(function (it) { it.classList.remove("owl-cat-hide"); });
      if (q) {
        var keep = new Set();
        items.forEach(function (it) {
          if (it._name.toLowerCase().indexOf(q) > -1) { keep.add(it); ancestors(it).forEach(function (a) { keep.add(a); }); }
        });
        items.forEach(function (it) {
          if (!keep.has(it)) it.classList.add("owl-cat-hide");
          else if (isSection(it)) it.setAttribute("aria-expanded", "true");
        });
        any = keep.size > 0;
      }
      nomatch.hidden = any;
    });
    $("owlCatFilter").addEventListener("keydown", function (e) { if (e.key === "Enter") e.preventDefault(); });

    var st = restore();
    render();
    if (st && st.q) { $("owlCatFilter").value = st.q; $("owlCatFilter").dispatchEvent(new Event("input")); }
    if (st && st.y) { setTimeout(function () { window.scrollTo(0, st.y); }, 0); }
    new MutationObserver(save).observe(tree, { subtree: true, attributes: true, attributeFilter: ["aria-expanded"] });
    window.addEventListener("pagehide", save);
  }
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", init); else init();
})();
