(function () {
  "use strict";

  var params = new URLSearchParams(window.location.search);
  var fromTask = params.get("from") === "task";

  var remixBtn = document.querySelector("[data-action-remix]");
  var taskBtn = document.querySelector("[data-action-task]");
  if (fromTask) {
    if (remixBtn) remixBtn.hidden = true;
    if (taskBtn) taskBtn.hidden = false;
  } else {
    if (remixBtn) remixBtn.hidden = false;
    if (taskBtn) taskBtn.hidden = true;
  }

  var backBtn = document.querySelector("[data-video-back]");
  if (backBtn) {
    backBtn.addEventListener("click", function () {
      var fallback = fromTask ? "11-task-detail.html" : "03-home-primary.html";
      if (window.history.length > 1) {
        window.history.back();
      } else {
        window.location.href = fallback;
      }
    });
  }

  var subscribeBtn = document.querySelector("[data-series-subscribe]");
  if (subscribeBtn) {
    subscribeBtn.addEventListener("click", function () {
      var subscribed = subscribeBtn.classList.toggle("is-subscribed");
      subscribeBtn.textContent = subscribed ? "已订阅" : "订阅系列";
      subscribeBtn.setAttribute("aria-pressed", subscribed ? "true" : "false");
    });
  }

  initMediaCarousel();

  function initMediaCarousel() {
    var root = document.querySelector("[data-media-carousel]");
    if (!root) return;

    var track = root.querySelector("[data-media-track]");
    var slides = Array.prototype.slice.call(root.querySelectorAll(".lab-watch__media-slide"));
    var dotsWrap = root.querySelector("[data-media-dots]");
    var counter = root.querySelector("[data-media-counter]");
    var prevBtn = root.querySelector("[data-media-prev]");
    var nextBtn = root.querySelector("[data-media-next]");
    if (!track || !slides.length) return;

    var index = 0;
    var touchStartX = 0;

    slides.forEach(function (_, i) {
      var dot = document.createElement("button");
      dot.type = "button";
      dot.className = "lab-watch__media-dot" + (i === 0 ? " is-active" : "");
      dot.setAttribute("aria-label", "第 " + (i + 1) + " 张");
      dot.addEventListener("click", function () { goTo(i); });
      if (dotsWrap) dotsWrap.appendChild(dot);
    });

    function pauseAllVideos() {
      slides.forEach(function (slide) {
        var video = slide.querySelector("video");
        if (video) {
          video.pause();
        }
      });
    }

    function playActiveVideo() {
      var active = slides[index];
      if (!active) return;
      var video = active.querySelector("video");
      if (!video) return;
      var playPromise = video.play();
      if (playPromise && typeof playPromise.catch === "function") {
        playPromise.catch(function () { /* autoplay blocked */ });
      }
    }

    function goTo(nextIndex) {
      if (nextIndex < 0) nextIndex = slides.length - 1;
      if (nextIndex >= slides.length) nextIndex = 0;
      index = nextIndex;
      track.style.transform = "translateX(-" + (index * 100) + "%)";
      slides.forEach(function (slide, i) {
        slide.classList.toggle("is-active", i === index);
      });
      if (dotsWrap) {
        Array.prototype.forEach.call(dotsWrap.children, function (dot, i) {
          dot.classList.toggle("is-active", i === index);
        });
      }
      if (counter) counter.textContent = (index + 1) + " / " + slides.length;
      pauseAllVideos();
      playActiveVideo();
    }

    if (prevBtn) prevBtn.addEventListener("click", function () { goTo(index - 1); });
    if (nextBtn) nextBtn.addEventListener("click", function () { goTo(index + 1); });

    root.addEventListener("touchstart", function (e) {
      if (!e.changedTouches || !e.changedTouches.length) return;
      touchStartX = e.changedTouches[0].clientX;
    }, { passive: true });

    root.addEventListener("touchend", function (e) {
      if (!e.changedTouches || !e.changedTouches.length) return;
      var delta = e.changedTouches[0].clientX - touchStartX;
      if (Math.abs(delta) < 40) return;
      if (delta < 0) goTo(index + 1);
      else goTo(index - 1);
    }, { passive: true });

    goTo(0);
  }
})();
