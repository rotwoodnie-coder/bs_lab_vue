(function () {
  "use strict";

  var HIDDEN_CLASS = "is-task-hidden";

  function qs(sel, root) { return (root || document).querySelector(sel); }
  function qsa(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  function getInitialType() {
    var param = new URLSearchParams(window.location.search).get("type");
    if (param === "quiz" || param === "homework" || param === "remix") return param;
    return "all";
  }

  function getInitialStatus() {
    var param = new URLSearchParams(window.location.search).get("status");
    return param === "done" ? "done" : "pending";
  }

  function setVisible(el, visible) {
    el.classList.toggle(HIDDEN_CLASS, !visible);
    el.hidden = !visible;
  }

  function applyQuizState(root) {
    var block = qs("[data-task-quiz-block]", root);
    if (!block) return;

    var quizStatus = new URLSearchParams(window.location.search).get("quiz") === "pending"
      ? "pending"
      : "done";

    block.setAttribute("data-task-status", quizStatus);

    var doneRow = qs("[data-quiz-done]", block);
    var pendingRow = qs("[data-quiz-pending]", block);
    if (doneRow) doneRow.hidden = quizStatus !== "done";
    if (pendingRow) pendingRow.hidden = quizStatus !== "pending";
  }

  function applyFilters(root, type, status) {
    qsa("[data-task-type]", root).forEach(function (card) {
      var cardType = card.getAttribute("data-task-type");
      var cardStatus = card.getAttribute("data-task-status") || "pending";
      var typeMatch = type === "all" || cardType === type;
      var statusMatch = cardStatus === status;
      setVisible(card, typeMatch && statusMatch);
    });

    qsa("[data-task-tab]", root).forEach(function (tab) {
      tab.classList.toggle("active", tab.getAttribute("data-task-tab") === type);
    });
    qsa("[data-task-status-tab]", root).forEach(function (tab) {
      tab.classList.toggle("active", tab.getAttribute("data-task-status-tab") === status);
    });

    var empty = qs("[data-tasks-empty]", root);
    if (empty) {
      var anyVisible = qsa("[data-task-type]", root).some(function (card) {
        return !card.hidden;
      });
      empty.hidden = anyVisible;
    }
  }

  function initFab(root) {
    var fabBtn = qs("[data-tasks-fab]", root);
    var overlay = qs("[data-tasks-fab-overlay]", root);
    var menu = qs("[data-tasks-fab-menu]", root);
    if (!fabBtn || !overlay || !menu) return;

    function closeFab() {
      overlay.hidden = true;
      menu.hidden = true;
      fabBtn.classList.remove("is-open");
      fabBtn.setAttribute("aria-expanded", "false");
    }

    function openFab() {
      overlay.hidden = false;
      menu.hidden = false;
      fabBtn.classList.add("is-open");
      fabBtn.setAttribute("aria-expanded", "true");
    }

    fabBtn.addEventListener("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      if (menu.hidden) openFab();
      else closeFab();
    });

    overlay.addEventListener("click", closeFab);

    qsa("[data-tasks-fab-action]", menu).forEach(function (item) {
      item.addEventListener("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        closeFab();
        window.location.href = item.getAttribute("data-href") || "14-upload.html";
      });
    });
  }

  function initTasksList() {
    var root = qs("[data-tasks-list]");
    if (!root) return;

    applyQuizState(root);

    var currentType = getInitialType();
    var currentStatus = getInitialStatus();

    applyFilters(root, currentType, currentStatus);

    root.addEventListener("click", function (e) {
      var typeTab = e.target.closest("[data-task-tab]");
      if (typeTab) {
        e.preventDefault();
        e.stopPropagation();
        currentType = typeTab.getAttribute("data-task-tab");
        applyFilters(root, currentType, currentStatus);
        return;
      }
      var statusTab = e.target.closest("[data-task-status-tab]");
      if (statusTab) {
        e.preventDefault();
        e.stopPropagation();
        currentStatus = statusTab.getAttribute("data-task-status-tab");
        applyFilters(root, currentType, currentStatus);
      }
    });

    initFab(root);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initTasksList);
  } else {
    initTasksList();
  }
})();
