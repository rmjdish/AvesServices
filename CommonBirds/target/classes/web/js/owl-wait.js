/* "Please wait" overlay for the search pages.
   Shown straight away when:
     - a link into any search page is clicked (href like search?type=c, l, t, y, ky ...)
     - a search form is submitted: it posts to "search", has a hidden search "type"
       field (n name, k keyword, l library, t topic, y year, ky keyword+year, c category),
       or has the class owl-wait. Forms submitted from script (form.submit()) count too.
   so the slow category/library pages and the slow results pages never look frozen.
   Self-contained (injects its own styles) and safe to include on every page. */
(function () {
  if (window.owlWait) return;

  /* Same look as the Multiple Add "please wait" in owl.css: dark backdrop, white box,
     navy (#2c3e6b) text and spinner. Own class names (owl-sw-*), so it can never clash
     with the .owl-wait-* rules that Multiple Add uses. */
  var css =
    ".owl-sw-overlay{display:none;position:fixed;top:0;left:0;right:0;bottom:0;" +
    "background:rgba(0,0,0,.55);z-index:99999;align-items:center;justify-content:center}" +
    ".owl-sw-overlay.on{display:flex}" +
    ".owl-sw-box{background:#fff;border-radius:10px;padding:28px 36px;text-align:center;" +
    "box-shadow:0 4px 20px rgba(0,0,0,.3);max-width:340px;min-width:280px;font-family:Arial,sans-serif}" +
    ".owl-sw-spinner{width:40px;height:40px;margin:0 auto;border:4px solid #e0e0e0;border-top:4px solid #2c3e6b;" +
    "border-radius:50%;animation:owl-sw-spin .8s linear infinite}" +
    ".owl-sw-box p{margin:4px 0 0;font-size:15px;font-weight:600;color:#2c3e6b}" +
    ".owl-sw-box p.owl-sw-sub{margin-top:6px;font-size:13px;font-weight:normal;color:#666}" +
    "@keyframes owl-sw-spin{to{transform:rotate(360deg)}}" +
    "@media (prefers-reduced-motion:reduce){.owl-sw-spinner{animation-duration:2.4s}}";
  var style = document.createElement("style");
  style.textContent = css;
  document.head.appendChild(style);

  var ov = null, timer = null;
  function build() {
    ov = document.createElement("div");
    ov.className = "owl-sw-overlay";
    ov.setAttribute("role", "status");
    ov.setAttribute("aria-live", "polite");
    ov.innerHTML = '<div class="owl-sw-box"><div class="owl-sw-spinner"></div>' +
      '<p class="owl-sw-msg"></p><p class="owl-sw-sub">This can take a few moments.</p></div>';
    document.body.appendChild(ov);
  }
  function show(msg) {
    if (!ov) build();
    ov.querySelector(".owl-sw-msg").textContent = msg || "Please wait\u2026";
    ov.classList.add("on");
    clearTimeout(timer);
    timer = setTimeout(hide, 120000);      /* never leave the page blocked */
  }
  function hide() {
    if (ov) ov.classList.remove("on");
    clearTimeout(timer);
  }
  window.owlWait = { show: show, hide: hide };

  /* links into a search page */
  var SEARCH_LINK = /(^|\/)search\?(?:[^#]*&)?type=([a-z]+)/i;
  document.addEventListener("click", function (e) {
    if (e.defaultPrevented || e.button || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
    var a = e.target.closest && e.target.closest("a[href]");
    if (!a || (a.target && a.target !== "_self")) return;
    var m = SEARCH_LINK.exec(a.getAttribute("href") || "");
    if (!m) return;
    var t = m[2].toLowerCase();
    show(t === "c" ? "Loading the category tree\u2026" :
         t === "lib" ? "Searching\u2026 please wait" : "Loading\u2026 please wait");
  });

  /* search forms */
  function isSearchForm(f) {
    if (!f || f.tagName !== "FORM") return false;
    var action = (f.getAttribute("action") || "").split("?")[0];
    return f.classList.contains("owl-wait") || /(^|\/)search$/i.test(action) ||
      !!f.querySelector('input[name="type"]');
  }
  document.addEventListener("submit", function (e) {
    if (e.defaultPrevented) return;                    /* validation said no: stay put */
    if (isSearchForm(e.target)) show("Searching\u2026 please wait");
  });
  var nativeSubmit = HTMLFormElement.prototype.submit; /* form.submit() fires no submit event */
  HTMLFormElement.prototype.submit = function () {
    if (isSearchForm(this)) show("Searching\u2026 please wait");
    return nativeSubmit.apply(this, arguments);
  };

  /* coming back with the Back button must not show a stale overlay */
  window.addEventListener("pageshow", hide);
})();
