(function () {
  "use strict";

  var STORAGE_KEY = "bslab-home-notices-read";
  var NOTICE_ID = "20260603-science-week";

  var MOCK_NOTICE = {
    id: NOTICE_ID,
    title: "科学周活动通知",
    date: "2026-06-03",
    body: "本周起开展「校园科学周」主题活动，请同学们完成至少 1 个家庭小实验并上传成果。优秀作品将展示在作品墙。"
  };

  function qs(sel) { return document.querySelector(sel); }

  function isRead() {
    try {
      var read = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
      return read.indexOf(NOTICE_ID) !== -1;
    } catch (e) {
      return false;
    }
  }

  function markRead() {
    try {
      var read = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
      if (read.indexOf(NOTICE_ID) === -1) read.push(NOTICE_ID);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(read));
    } catch (e) { /* ignore */ }
  }

  function openModal() {
    var modal = qs("[data-notice-modal]");
    if (modal) modal.hidden = false;
  }

  function closeModal() {
    var modal = qs("[data-notice-modal]");
    if (modal) modal.hidden = true;
  }

  function initNoticeModal() {
    var root = qs("[data-home-notices]");
    if (!root || root.getAttribute("data-home-notices") === "none") return;

    var modal = qs("[data-notice-modal]");
    var check = qs("[data-notice-read-check]");
    var confirmBtn = qs("[data-notice-confirm]");
    var titleEl = qs("[data-notice-title]");
    var dateEl = qs("[data-notice-date]");
    var bodyEl = qs("[data-notice-body]");

    if (titleEl) titleEl.textContent = MOCK_NOTICE.title;
    if (dateEl) dateEl.textContent = MOCK_NOTICE.date;
    if (bodyEl) bodyEl.textContent = MOCK_NOTICE.body;

    if (check && confirmBtn) {
      check.addEventListener("change", function () {
        confirmBtn.disabled = !check.checked;
      });
      confirmBtn.addEventListener("click", function () {
        if (!check.checked) return;
        markRead();
        closeModal();
      });
    }

    var backdrop = qs("[data-notice-backdrop]");
    if (backdrop) {
      backdrop.addEventListener("click", function () {
        /* 必须勾选已读才能关闭 */
      });
    }

    if (!isRead()) {
      window.requestAnimationFrame(openModal);
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initNoticeModal);
  } else {
    initNoticeModal();
  }
})();
