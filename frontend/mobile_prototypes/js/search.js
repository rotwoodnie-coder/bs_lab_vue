/* ============================================================
   搜索 & 语音搜索（YouTube 移动端交互原型）
   ============================================================ */
(function () {
  var RECENT_KEY = "bslab-search-recent";
  var EXP_RECENT_KEY = "bslab-exp-search-recent";
  var DEMO_VOICE_TEXT = "自制风向标实验";

  var MOCK_RESULTS = [
    { title: "自制风向标", grade: "三年级（第一学期）", subject: "科学", views: "1.2万次播放", teacher: "王老师", rank: "一级教师", school: "宝山实验小学", tag: "视频", avatar: "王", grad: "avatar-grad-ocean", media: "card-media-grad-warm", href: "06-video-detail.html" },
    { title: "自制风向标", grade: "三年级（上学期）", subject: "科学", views: "6200次播放", teacher: "李老师", rank: "高级教师", school: "友谊路小学", tag: "视频", avatar: "李", grad: "avatar-grad-sunset", media: "card-media-grad-warm", href: "06-video-detail.html" },
    { title: "彩虹投影", grade: "四年级（第一学期）", subject: "科学", views: "8600次播放", teacher: "李老师", rank: "高级教师", school: "友谊路小学", tag: "实验", avatar: "李", grad: "avatar-grad-sunset", media: "card-media-grad-sunset", href: "06-video-detail.html" },
    { title: "种子发芽观察", grade: "二年级（上学期）", subject: "科学", views: "5600次播放", teacher: "张老师", rank: "二级教师", school: "顾村实验小学", tag: "视频", avatar: "张", grad: "avatar-grad-forest", media: "card-media-grad-forest", href: "06-video-detail.html" },
    { title: "简易串联灯", grade: "五年级（第一学期）", subject: "物理", views: "4300次播放", teacher: "王老师", rank: "一级教师", school: "吴淞附属小学", tag: "视频", avatar: "王", grad: "avatar-grad-amber-rose", media: "card-media-grad-amber-rose", href: "06-video-detail.html" },
    { title: "磁力小车", grade: "三年级（上学期）", subject: "物理", views: "9800次播放", teacher: "陈老师", rank: "骨干教师", school: "行知实验小学", tag: "视频", avatar: "陈", grad: "avatar-grad-ocean", media: "card-media-grad-ocean", href: "06-video-detail.html" },
    { title: "浮力实验", grade: "七年级（第一学期）", subject: "物理", views: "1.5万次播放", teacher: "王老师", rank: "高级教师", school: "宝山中学", tag: "视频", avatar: "王", grad: "avatar-grad-ocean", media: "card-media-grad-ocean", href: "06-video-detail.html" },
    { title: "酸碱中和反应", grade: "八年级（上学期）", subject: "化学", views: "9200次播放", teacher: "周老师", rank: "骨干教师", school: "行知中学", tag: "实验", avatar: "周", grad: "avatar-grad-cool", media: "card-media-grad-cool", href: "06-video-detail.html" },
  ];

  var SUGGESTIONS = [
    "自制风向标", "彩虹投影", "种子发芽", "串联电路", "水的表面张力", "磁力小车", "电路搭建模拟",
  ];

  var EXP_MOCK_RESULTS = [
    { title: "串联电路", subject: "物理", desc: "拖拽连接灯泡和电池", avatar: "电", grad: "avatar-grad-cool", media: "card-media-grad-ocean" },
    { title: "火山喷发", subject: "化学", desc: "调节小苏打和醋的比例", avatar: "化", grad: "avatar-grad-warm", media: "card-media-grad-warm" },
    { title: "种子发芽", subject: "生物", desc: "调节水分和光照", avatar: "生", grad: "avatar-grad-forest", media: "card-media-grad-forest" },
    { title: "光的折射", subject: "物理", desc: "调整入射角度", avatar: "光", grad: "avatar-grad-sunset", media: "card-media-grad-sunset" },
    { title: "电磁铁", subject: "物理", desc: "线圈匝数实验", avatar: "磁", grad: "avatar-grad-amber-rose", media: "card-media-grad-amber-rose" },
    { title: "水的三态", subject: "化学", desc: "温度与状态变化", avatar: "水", grad: "avatar-grad-ocean", media: "card-media-grad-cool" },
    { title: "简易风向标", subject: "科学", desc: "观察风向变化", avatar: "风", grad: "avatar-grad-sunset", media: "card-media-grad-warm" },
    { title: "光合作用", subject: "生物", desc: "光照强度对比", avatar: "叶", grad: "avatar-grad-forest", media: "card-media-grad-forest" },
    { title: "酸碱中和", subject: "化学", desc: "滴定模拟实验", avatar: "酸", grad: "avatar-grad-violet-indigo", media: "card-media-grad-violet-indigo" },
    { title: "天体运行", subject: "地理", desc: "太阳系模拟", avatar: "天", grad: "avatar-grad-ocean", media: "card-media-grad-ocean" },
  ];

  var EXP_SUGGESTIONS = [
    "串联电路", "火山喷发", "种子发芽", "简易风向标", "电磁铁", "光合作用", "酸碱中和",
  ];

  function qs(sel, root) { return (root || document).querySelector(sel); }
  function qsa(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  function getParam(name) {
    return new URLSearchParams(window.location.search).get(name) || "";
  }

  function loadRecent() {
    try {
      return JSON.parse(localStorage.getItem(RECENT_KEY) || "[]");
    } catch (e) {
      return [];
    }
  }

  function saveRecent(q) {
    var trimmed = (q || "").trim();
    if (!trimmed) return;
    var list = loadRecent().filter(function (item) { return item !== trimmed; });
    list.unshift(trimmed);
    localStorage.setItem(RECENT_KEY, JSON.stringify(list.slice(0, 8)));
  }

  function removeRecent(q) {
    var list = loadRecent().filter(function (item) { return item !== q; });
    localStorage.setItem(RECENT_KEY, JSON.stringify(list));
  }

  function metaSearchText(item) {
    return [item.grade, item.subject, item.views, item.teacher, item.rank, item.school].join(" ");
  }

  function renderMetaBlock(item) {
    return (
      '<div class="video-card__meta-block">' +
        '<p class="video-card__meta-line">' + item.grade + '<span class="video-card__dot">·</span>' + item.subject + '<span class="video-card__dot">·</span>' + item.views + '</p>' +
        '<p class="video-card__meta-line video-card__meta-line--creator"><span class="video-card__author">' + item.teacher + '</span><span class="video-card__dot">·</span>' + item.rank + '<span class="video-card__dot">·</span>' + item.school + '</p>' +
      '</div>'
    );
  }

  function filterResults(query, config) {
    var q = (query || "").trim().toLowerCase();
    if (!q) return [];
    var pool = config.mockResults || MOCK_RESULTS;
    return pool.filter(function (item) {
      if (config.scope === "exp") {
        return item.title.toLowerCase().indexOf(q) !== -1
          || item.subject.toLowerCase().indexOf(q) !== -1
          || item.desc.toLowerCase().indexOf(q) !== -1;
      }
      return item.title.toLowerCase().indexOf(q) !== -1
        || metaSearchText(item).toLowerCase().indexOf(q) !== -1;
    });
  }

  function getSearchConfig(root) {
    if (root.getAttribute("data-search-scope") === "exp") {
      return {
        scope: "exp",
        recentKey: EXP_RECENT_KEY,
        searchPage: "07-virtual-exp-search.html",
        suggestions: EXP_SUGGESTIONS,
        mockResults: EXP_MOCK_RESULTS,
        resultsMeta: function (q, count) {
          return "找到 " + count + " 个与「" + q + "」相关的模拟实验";
        },
      };
    }
    return {
      scope: "home",
      recentKey: RECENT_KEY,
      searchPage: "03-search.html",
      suggestions: SUGGESTIONS,
      mockResults: MOCK_RESULTS,
      resultsMeta: function (q, count) {
        return "找到 " + count + " 条与「" + q + "」相关的内容";
      },
    };
  }

  function renderExpResultCard(item, index) {
    var delay = "delay-" + ((index % 4) + 1);
    var href = "07-virtual-exp-detail.html?title=" + encodeURIComponent(item.title);
    return (
      '<a href="' + href + '" class="waterfall-card video-card video-card--sim card-link anim-fade-up ' + delay + '">' +
        '<div class="video-card__media ' + item.media + '">' +
          '<span class="video-card__tag">模拟</span>' +
          '<span class="video-card__play"><i data-lucide="play" class="icon"></i></span>' +
        '</div>' +
        '<div class="video-card__body">' +
          '<div class="video-card__info">' +
            '<span class="video-card__avatar avatar avatar-sm ' + item.grad + '">' + item.avatar + '</span>' +
            '<div class="video-card__text">' +
              '<h3 class="video-card__title">' + item.title + '</h3>' +
              '<p class="video-card__meta">' + item.subject + ' · ' + item.desc + '</p>' +
            '</div>' +
          '</div>' +
        '</div>' +
      '</a>'
    );
  }

  function renderResultCard(item, index) {
    var delay = "delay-" + ((index % 4) + 1);
    return (
      '<a href="' + item.href + '" class="waterfall-card video-card card-link anim-fade-up ' + delay + '">' +
        '<div class="video-card__media ' + item.media + '">' +
          '<span class="video-card__tag">' + item.tag + '</span>' +
          '<span class="video-card__play"><i data-lucide="play" class="icon"></i></span>' +
        '</div>' +
        '<div class="video-card__body">' +
          '<div class="video-card__info">' +
            '<span class="video-card__avatar avatar avatar-sm ' + item.grad + '">' + item.avatar + '</span>' +
            '<div class="video-card__text">' +
              '<h3 class="video-card__title">' + item.title + '</h3>' +
              renderMetaBlock(item) +
            '</div>' +
          '</div>' +
        '</div>' +
      '</a>'
    );
  }

  function renderIcons() {
    if (window.lucide && typeof window.lucide.createIcons === "function") {
      window.lucide.createIcons();
    }
  }

  function navigateSearch(query) {
    var url = "03-search.html?q=" + encodeURIComponent(query);
    window.location.href = url;
  }

  function initSearchPage() {
    var root = qs("[data-search-page]");
    if (!root) return;

    var config = getSearchConfig(root);
    var input = qs("[data-search-input]", root);
    var emptyView = qs("[data-search-empty]", root);
    var resultsView = qs("[data-search-results]", root);
    var resultsGrid = qs("[data-search-results-grid]", root);
    var resultsMeta = qs("[data-search-results-meta]", root);
    var suggestList = qs("[data-search-suggest]", root);
    var recentList = qs("[data-search-recent]", root);
    var form = qs("[data-search-form]", root);
    var micBtn = qs("[data-search-mic]", root);

    var initialQuery = getParam("q");
    var fromVoice = getParam("from") === "voice";

    function loadScopeRecent() {
      try {
        return JSON.parse(localStorage.getItem(config.recentKey) || "[]");
      } catch (e) {
        return [];
      }
    }

    function saveScopeRecent(q) {
      var trimmed = (q || "").trim();
      if (!trimmed) return;
      var list = loadScopeRecent().filter(function (item) { return item !== trimmed; });
      list.unshift(trimmed);
      localStorage.setItem(config.recentKey, JSON.stringify(list.slice(0, 8)));
    }

    function removeScopeRecent(q) {
      var list = loadScopeRecent().filter(function (item) { return item !== q; });
      localStorage.setItem(config.recentKey, JSON.stringify(list));
    }

    function setView(mode) {
      if (emptyView) emptyView.hidden = mode !== "empty";
      if (resultsView) resultsView.hidden = mode !== "results";
      if (suggestList && suggestList.closest("[data-search-suggest-wrap]")) {
        suggestList.closest("[data-search-suggest-wrap]").hidden = mode === "results";
      }
    }

    function renderRecent() {
      if (!recentList) return;
      var items = loadScopeRecent();
      if (!items.length) {
        recentList.innerHTML = '<li class="search-page__empty-hint">暂无搜索记录</li>';
        return;
      }
      recentList.innerHTML = items.map(function (item) {
        return (
          '<li class="search-page__recent-item">' +
            '<button type="button" class="search-page__recent-btn" data-recent-query="' + item + '">' +
              '<i data-lucide="history" class="icon"></i>' +
              '<span>' + item + '</span>' +
            '</button>' +
            '<button type="button" class="search-page__recent-remove" data-remove-recent="' + item + '" aria-label="删除">' +
              '<i data-lucide="x" class="icon"></i>' +
            '</button>' +
          '</li>'
        );
      }).join("");
      renderIcons();
    }

    function renderSuggestions(filter) {
      if (!suggestList) return;
      var q = (filter || "").trim().toLowerCase();
      var list = config.suggestions.filter(function (s) {
        return !q || s.toLowerCase().indexOf(q) !== -1;
      }).slice(0, 6);
      suggestList.innerHTML = list.map(function (item) {
        return (
          '<li><button type="button" class="search-page__suggest-btn" data-suggest-query="' + item + '">' +
            '<i data-lucide="search" class="icon"></i><span>' + item + '</span>' +
          '</button></li>'
        );
      }).join("");
      renderIcons();
    }

    function showResults(query) {
      var q = (query || "").trim();
      if (!q) {
        setView("empty");
        renderSuggestions("");
        return;
      }
      var results = filterResults(q, config);
      if (!results.length) {
        results = config.mockResults.slice(0, 3);
      }
      if (resultsMeta) {
        resultsMeta.textContent = config.resultsMeta(q, results.length);
      }
      if (resultsGrid) {
        var renderCard = config.scope === "exp" ? renderExpResultCard : renderResultCard;
        resultsGrid.innerHTML = results.map(renderCard).join("");
      }
      setView("results");
      saveScopeRecent(q);
      renderRecent();
      renderIcons();
    }

    function submitSearch() {
      if (!input) return;
      showResults(input.value);
    }

    if (input) {
      input.addEventListener("input", function () {
        var val = input.value.trim();
        if (!val) {
          setView("empty");
          renderSuggestions("");
          return;
        }
        renderSuggestions(val);
        setView("empty");
      });

      window.requestAnimationFrame(function () {
        if (!document.documentElement.classList.contains("in-iframe")) {
          input.focus();
        }
        if (initialQuery) {
          input.value = initialQuery;
          showResults(initialQuery);
          if (fromVoice) {
            input.setSelectionRange(initialQuery.length, initialQuery.length);
          }
        }
      });
    }

    if (form) {
      form.addEventListener("submit", function (e) {
        e.preventDefault();
        submitSearch();
      });
    }

    root.addEventListener("click", function (e) {
      var recentBtn = e.target.closest("[data-recent-query]");
      if (recentBtn && input) {
        input.value = recentBtn.getAttribute("data-recent-query");
        submitSearch();
        return;
      }
      var suggestBtn = e.target.closest("[data-suggest-query]");
      if (suggestBtn && input) {
        input.value = suggestBtn.getAttribute("data-suggest-query");
        submitSearch();
        return;
      }
      var removeBtn = e.target.closest("[data-remove-recent]");
      if (removeBtn) {
        removeScopeRecent(removeBtn.getAttribute("data-remove-recent"));
        renderRecent();
      }
    });

    if (micBtn) {
      var returnUrl = config.searchPage;
      if (input && input.value.trim()) {
        returnUrl += "?q=" + encodeURIComponent(input.value.trim());
      }
      micBtn.href = "03-voice-search.html?return=" + encodeURIComponent(returnUrl);
    }

    renderRecent();
    renderSuggestions("");
    if (!initialQuery) setView("empty");
  }

  function initVoiceSearchPage() {
    var root = qs("[data-voice-search-page]");
    if (!root) return;

    var transcriptEl = qs("[data-voice-transcript]", root);
    var statusEl = qs("[data-voice-status]", root);
    var listenTipEl = qs("[data-voice-listen-tip]", root);
    var retryTipEl = qs("[data-voice-retry-tip]", root);
    var pulseEl = qs("[data-voice-pulse]", root);
    var heroEl = qs("[data-voice-hero]", root);
    var returnUrl = decodeURIComponent(getParam("return") || "03-search.html");
    var closeBtn = qs("[data-voice-close]", root);

    var finalText = DEMO_VOICE_TEXT;
    var displayed = "";
    var charIndex = 0;
    var typingTimer = null;
    var finishTimer = null;
    var startTimer = null;
    var finished = false;
    var voiceState = "listening";
    var recognition = null;

    function clearTimers() {
      window.clearTimeout(typingTimer);
      window.clearTimeout(finishTimer);
      window.clearTimeout(startTimer);
    }

    function stopRecognition() {
      if (!recognition) return;
      try {
        recognition.abort();
      } catch (e) {
        try {
          recognition.stop();
        } catch (e2) { /* ignore */ }
      }
      recognition = null;
    }

    function goToSearch(text) {
      if (finished) return;
      finished = true;
      voiceState = "done";
      var parts = returnUrl.split("?");
      var path = parts[0];
      var params = new URLSearchParams(parts[1] || "");
      params.set("q", text);
      params.set("from", "voice");
      window.location.href = path + "?" + params.toString();
    }

    function hideListenTip() {
      if (listenTipEl) listenTipEl.hidden = true;
    }

    function showListenTip() {
      if (listenTipEl) listenTipEl.hidden = false;
    }

    function showStatus(text) {
      if (!statusEl) return;
      statusEl.textContent = text;
      statusEl.hidden = false;
    }

    function hideStatus() {
      if (statusEl) statusEl.hidden = true;
    }

    function showRetryTip() {
      if (retryTipEl) retryTipEl.hidden = false;
    }

    function hideRetryTip() {
      if (retryTipEl) retryTipEl.hidden = true;
    }

    function enterRetryState() {
      if (voiceState === "done") return;
      voiceState = "retry";
      clearTimers();
      stopRecognition();
      charIndex = 0;
      displayed = "";
      if (transcriptEl) transcriptEl.textContent = "";
      hideStatus();
      hideListenTip();
      if (pulseEl) {
        pulseEl.classList.remove("is-done");
        pulseEl.classList.add("is-retry");
      }
      if (heroEl) {
        heroEl.classList.remove("is-done");
        heroEl.classList.add("is-retry");
      }
      showRetryTip();
    }

    function enterListeningState() {
      if (voiceState === "done") return;
      voiceState = "listening";
      charIndex = 0;
      displayed = "";
      if (transcriptEl) transcriptEl.textContent = "";
      hideStatus();
      hideRetryTip();
      showListenTip();
      if (pulseEl) pulseEl.classList.remove("is-retry", "is-done");
      if (heroEl) heroEl.classList.remove("is-retry", "is-done");
      startListening();
    }

    function finishRecognition(text) {
      if (voiceState !== "listening") return;
      voiceState = "done";
      hideListenTip();
      hideRetryTip();
      if (heroEl) heroEl.classList.add("is-done");
      showStatus("识别完成，正在搜索…");
      if (pulseEl) {
        pulseEl.classList.remove("is-retry");
        pulseEl.classList.add("is-done");
      }
      finishTimer = window.setTimeout(function () {
        goToSearch(text);
      }, 600);
    }

    function typeNextChar(fullText) {
      if (voiceState !== "listening") return;
      if (charIndex === 0) hideListenTip();
      if (charIndex >= fullText.length) {
        finishRecognition(fullText);
        return;
      }
      displayed += fullText.charAt(charIndex);
      charIndex += 1;
      if (transcriptEl) transcriptEl.textContent = displayed;
      typingTimer = window.setTimeout(function () {
        typeNextChar(fullText);
      }, 80);
    }

    function startSimulation() {
      if (voiceState !== "listening") return;
      startTimer = window.setTimeout(function () {
        typeNextChar(finalText);
      }, 900);
    }

    function startWebSpeech() {
      if (voiceState !== "listening") return;
      var SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      if (!SpeechRecognition) {
        startSimulation();
        return;
      }

      stopRecognition();
      recognition = new SpeechRecognition();
      recognition.lang = "zh-CN";
      recognition.interimResults = true;
      recognition.continuous = false;

      recognition.onresult = function (event) {
        if (voiceState !== "listening") return;
        var interim = "";
        var final = "";
        for (var i = event.resultIndex; i < event.results.length; i++) {
          var piece = event.results[i][0].transcript;
          if (event.results[i].isFinal) final += piece;
          else interim += piece;
        }
        if (interim || final) hideListenTip();
        if (transcriptEl) transcriptEl.textContent = final || interim;
        if (final) {
          recognition.stop();
          finishRecognition(final.trim() || finalText);
        }
      };

      recognition.onerror = function () {
        if (voiceState !== "listening") return;
        startSimulation();
      };

      recognition.onend = function () {
        if (voiceState !== "listening") return;
        if (!displayed && transcriptEl && !transcriptEl.textContent.trim()) {
          startSimulation();
        }
      };

      try {
        recognition.start();
      } catch (e) {
        startSimulation();
      }
    }

    function startListening() {
      clearTimers();
      stopRecognition();
      startTimer = window.setTimeout(startWebSpeech, 400);
    }

    function onMicClick() {
      if (voiceState === "done") return;
      if (voiceState === "retry") {
        enterListeningState();
      } else {
        enterRetryState();
      }
    }

    if (closeBtn) {
      closeBtn.href = returnUrl;
    }

    if (pulseEl) {
      pulseEl.addEventListener("click", onMicClick);
    }

    startListening();

    root.addEventListener("keydown", function (e) {
      if (e.key === "Escape") {
        clearTimers();
        stopRecognition();
        window.location.href = returnUrl;
      }
    });
  }

  function boot() {
    initSearchPage();
    initVoiceSearchPage();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }

  window.BSLabSearch = {
    navigateSearch: navigateSearch,
    saveRecent: saveRecent,
  };
})();
