(function () {
  "use strict";

  var HIDDEN_CLASS = "is-home-feed-hidden";

  function qsa(sel, root) {
    return Array.prototype.slice.call((root || document).querySelectorAll(sel));
  }

  function filterHomeFeed(subject, root) {
    qsa(".home-feed > [data-home-subject]", root).forEach(function (card) {
      var cardSubject = card.getAttribute("data-home-subject");
      var show = subject === "all" || cardSubject === subject;
      card.classList.toggle(HIDDEN_CLASS, !show);
      card.hidden = !show;
    });
    qsa("[data-home-subject-tab]", root).forEach(function (tab) {
      tab.classList.toggle("active", tab.getAttribute("data-home-subject-tab") === subject);
    });
  }

  function bindTab(tab, root) {
    tab.addEventListener("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      filterHomeFeed(tab.getAttribute("data-home-subject-tab"), root);
    });
  }

  function initHomeFeed() {
    var root = document.querySelector("[data-home-feed]");
    if (!root) return;

    filterHomeFeed("all", root);

    qsa("[data-home-subject-tab]", root).forEach(function (tab) {
      bindTab(tab, root);
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initHomeFeed);
  } else {
    initHomeFeed();
  }
})();
