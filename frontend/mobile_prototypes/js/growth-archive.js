(function () {
  "use strict";

  function qs(sel, root) { return (root || document).querySelector(sel); }
  function qsa(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  var PLAN_LABELS = {
    content: { exp: "实验", work: "作品", badge: "勋章", rank: "排名" },
    visibility: { self: "仅本人", parent: "本人 + 家长", class: "本人 + 家长 + 班级" },
    range: { term: "本学期", year: "本学年", all: "全部记录" },
  };

  function getPlanSummary() {
    var form = qs("[data-growth-plan-form]");
    if (!form) return { content: "", visibility: "", range: "" };

    var content = qsa("[name='plan-content']:checked", form).map(function (el) {
      return PLAN_LABELS.content[el.value] || el.value;
    }).join(" · ");

    var visibilityEl = qs("[name='plan-visibility']:checked", form);
    var rangeEl = qs("[name='plan-range']:checked", form);

    return {
      content: content || "实验 · 作品 · 勋章",
      visibility: visibilityEl ? (PLAN_LABELS.visibility[visibilityEl.value] || visibilityEl.value) : "本人 + 家长",
      range: rangeEl ? (PLAN_LABELS.range[rangeEl.value] || rangeEl.value) : "本学期",
    };
  }

  function renderPlanSummary() {
    var summary = getPlanSummary();
    qsa("[data-plan-content]").forEach(function (el) { el.textContent = summary.content; });
    qsa("[data-plan-visibility]").forEach(function (el) { el.textContent = summary.visibility; });
    qsa("[data-plan-range]").forEach(function (el) { el.textContent = summary.range; });
  }

  function openSheet() {
    var overlay = qs("[data-growth-plan-overlay]");
    var sheet = qs("[data-growth-plan-sheet]");
    if (overlay) overlay.classList.add("show");
    if (sheet) sheet.classList.add("open");
  }

  function closeSheet() {
    var overlay = qs("[data-growth-plan-overlay]");
    var sheet = qs("[data-growth-plan-sheet]");
    if (overlay) overlay.classList.remove("show");
    if (sheet) sheet.classList.remove("open");
  }

  function initGrowthArchive() {
    var root = qs("[data-layout='growth-archive']");
    if (!root) return;

    renderPlanSummary();

    root.addEventListener("click", function (e) {
      if (e.target.closest("[data-growth-plan-open]")) {
        e.preventDefault();
        openSheet();
        return;
      }
      if (e.target.closest("[data-growth-plan-close]")) {
        e.preventDefault();
        closeSheet();
      }
    });

    var form = qs("[data-growth-plan-form]");
    if (form) {
      form.addEventListener("submit", function (e) {
        e.preventDefault();
        renderPlanSummary();
        closeSheet();
      });
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initGrowthArchive);
  } else {
    initGrowthArchive();
  }
})();
