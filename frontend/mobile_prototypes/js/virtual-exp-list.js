(function () {
  "use strict";

  var HIDDEN_CLASS = "is-exp-hidden";

  function qs(sel, root) { return (root || document).querySelector(sel); }
  function qsa(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  function getInitialSubject() {
    var param = new URLSearchParams(window.location.search).get("subject");
    var allowed = { 科学: 1, 物理: 1, 化学: 1, 生物: 1, 地理: 1 };
    return allowed[param] ? param : "all";
  }

  function setVisible(card, visible) {
    card.classList.toggle(HIDDEN_CLASS, !visible);
    card.hidden = !visible;
  }

  function applyFilters(root, subject) {
    qsa(".pad-explore__waterfall > [data-exp-subject]", root).forEach(function (card) {
      var cardSubject = card.getAttribute("data-exp-subject");
      var subjectMatch = subject === "all" || cardSubject === subject;
      setVisible(card, subjectMatch);
    });

    qsa("[data-exp-subject-tab]", root).forEach(function (tab) {
      tab.classList.toggle("active", tab.getAttribute("data-exp-subject-tab") === subject);
    });

    var empty = qs("[data-exp-empty]", root);
    if (empty) {
      var anyVisible = qsa(".pad-explore__waterfall > [data-exp-subject]", root).some(function (card) {
        return !card.hidden;
      });
      empty.hidden = anyVisible;
      if (!anyVisible) {
        empty.textContent = "该分类下暂无模拟实验";
      }
    }
  }

  function initVirtualExpList() {
    var root = qs("[data-exp-list]");
    if (!root) return;

    var currentSubject = getInitialSubject();
    applyFilters(root, currentSubject);

    root.addEventListener("click", function (e) {
      var tab = e.target.closest("[data-exp-subject-tab]");
      if (tab) {
        e.preventDefault();
        e.stopPropagation();
        currentSubject = tab.getAttribute("data-exp-subject-tab");
        applyFilters(root, currentSubject);
      }
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initVirtualExpList);
  } else {
    initVirtualExpList();
  }
})();
