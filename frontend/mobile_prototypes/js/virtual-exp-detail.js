(function () {
  "use strict";

  var SIM_URL = "https://www.zcat.cn/web/3d9b6395d21fd2c37ae916d749f5d6c30c280dbf5bac998f77b74afd8d383a57";

  var BACK_TARGETS = {
    video: "06-video-detail.html",
    task: "11-task-detail.html",
    work: "13-work-detail.html",
    list: "07-virtual-exp-list.html",
  };

  function qs(sel, root) { return (root || document).querySelector(sel); }

  function initVirtualExpDetail() {
    var root = qs("[data-sim-embed]");
    if (!root) return;

    var params = new URLSearchParams(window.location.search);
    var title = params.get("title") || "模拟实验";
    var from = params.get("from") || "list";
    var backHref = BACK_TARGETS[from] || BACK_TARGETS.list;

    var titleEl = qs("[data-sim-title]", root);
    if (titleEl) titleEl.textContent = title;
    document.title = title + " · 模拟实验 · 宝山区小实验社区";

    var back = qs("[data-sim-back]", root);
    if (back) back.setAttribute("href", backHref);

    var frame = qs("[data-sim-frame]", root);
    if (frame && !frame.getAttribute("src")) {
      frame.setAttribute("src", SIM_URL);
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initVirtualExpDetail);
  } else {
    initVirtualExpDetail();
  }
})();
