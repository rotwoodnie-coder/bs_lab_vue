(function () {
  "use strict";

  var TYPE_LABELS = {
    homework: "我的实验",
    remix: "拍同款",
    creative: "创意实验",
  };

  function qs(sel, root) { return (root || document).querySelector(sel); }
  function qsa(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  function getInitialType() {
    var param = new URLSearchParams(window.location.search).get("type");
    if (param && TYPE_LABELS[param]) return param;
    return "all";
  }

  function updateCount(root, type) {
    var cards = qsa("[data-work-type]", root);
    var visible = type === "all"
      ? cards.length
      : cards.filter(function (c) { return c.getAttribute("data-work-type") === type; }).length;
    var total = cards.length;
    var label = type === "all" ? total : visible;
    qsa("[data-works-count]", root).forEach(function (el) {
      el.textContent = "共 " + label + " 件";
    });
  }

  function filterWorks(type, root) {
    qsa("[data-work-type]", root).forEach(function (card) {
      card.hidden = type !== "all" && card.getAttribute("data-work-type") !== type;
    });
    qsa("[data-works-tab]", root).forEach(function (tab) {
      var tabType = tab.getAttribute("data-works-tab");
      tab.classList.toggle("active", tabType === type);
    });
    updateCount(root, type);
  }

  function initWorksWall() {
    var root = qs("[data-works-wall]");
    if (!root) return;

    var initial = getInitialType();
    filterWorks(initial, root);

    root.addEventListener("click", function (e) {
      var tab = e.target.closest("[data-works-tab]");
      if (!tab) return;
      filterWorks(tab.getAttribute("data-works-tab"), root);
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initWorksWall);
  } else {
    initWorksWall();
  }
})();
