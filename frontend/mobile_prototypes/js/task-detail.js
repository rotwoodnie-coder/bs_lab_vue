(function () {
  "use strict";

  function qs(sel) { return document.querySelector(sel); }

  function initTaskDetail() {
    var root = qs("[data-task-detail]");
    if (!root) return;

    var type = new URLSearchParams(window.location.search).get("type") || "homework";
    var isRemix = type === "remix";

    root.setAttribute("data-task-detail-type", type);

    var homeworkBlocks = document.querySelectorAll("[data-task-homework-only]");
    var remixBlocks = document.querySelectorAll("[data-task-remix-only]");

    homeworkBlocks.forEach(function (el) { el.hidden = isRemix; });
    remixBlocks.forEach(function (el) { el.hidden = !isRemix; });

    document.title = isRemix
      ? "拍同款任务 · 宝山区小实验社区"
      : "作业任务 · 宝山区小实验社区";

    var titleEl = qs("[data-task-detail-title]");
    if (titleEl) {
      titleEl.textContent = isRemix ? "拍同款任务" : "作业任务";
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initTaskDetail);
  } else {
    initTaskDetail();
  }
})();
