(function () {
  "use strict";

  var QUESTIONS = [
    { id: 1, tag: "物理 · 三年级", text: "下列哪种物质在常温下导电性最强？", options: ["铜丝", "塑料尺", "玻璃棒", "橡皮擦"], answer: 0 },
    { id: 2, tag: "科学 · 三年级", text: "植物进行光合作用主要需要什么？", options: ["阳光", "声音", "震动", "磁力"], answer: 0 },
    { id: 3, tag: "化学 · 三年级", text: "小苏打与白醋混合会产生什么气体？", options: ["二氧化碳", "氧气", "氮气", "氢气"], answer: 0 },
    { id: 4, tag: "生物 · 三年级", text: "种子发芽最先突破种皮的是？", options: ["根", "叶", "花", "果实"], answer: 0 },
    { id: 5, tag: "地理 · 三年级", text: "风向标用来测量什么？", options: ["风向", "温度", "湿度", "气压"], answer: 0 },
  ];

  var answers = [];
  var current = 0;
  var isPractice = false;

  function qs(sel) { return document.querySelector(sel); }
  function qsa(sel) { return Array.prototype.slice.call(document.querySelectorAll(sel)); }

  function renderDots() {
    var wrap = qs("[data-quiz-dots]");
    if (!wrap) return;
    wrap.innerHTML = "";
    QUESTIONS.forEach(function (_, i) {
      var dot = document.createElement("span");
      dot.className = "quiz-progress-dot";
      if (answers[i] !== undefined) dot.classList.add("is-done");
      if (i === current) dot.classList.add("is-current");
      dot.textContent = String(i + 1);
      wrap.appendChild(dot);
    });
  }

  function renderQuestion() {
    var q = QUESTIONS[current];
    if (qs("[data-quiz-title]")) qs("[data-quiz-title]").textContent = q.text;
    if (qs("[data-quiz-meta]")) qs("[data-quiz-meta]").textContent = q.tag;

    var answeredCount = answers.filter(function (a) { return a !== undefined; }).length;
    if (qs("[data-quiz-progress-text]")) {
      qs("[data-quiz-progress-text]").textContent = answeredCount + " / " + QUESTIONS.length + " 题已选";
    }
    if (qs("[data-quiz-progress-fill]")) {
      qs("[data-quiz-progress-fill]").style.width = ((answeredCount / QUESTIONS.length) * 100) + "%";
    }
    if (qs("#quizCurrent")) qs("#quizCurrent").textContent = String(current + 1);

    var optionsWrap = qs("[data-quiz-options]");
    if (optionsWrap) {
      optionsWrap.innerHTML = "";
      q.options.forEach(function (opt, idx) {
        var btn = document.createElement("div");
        btn.className = "option-btn card card-pad row items-center gap-3";
        btn.innerHTML = '<span class="tile-icon tile-sm tint-slate">' + String.fromCharCode(65 + idx) + '</span><span class="text-sm">' + opt + '</span>';
        if (answers[current] === idx) btn.classList.add("selected");
        btn.addEventListener("click", function () { selectOption(idx); });
        optionsWrap.appendChild(btn);
      });
    }

    var primaryBtn = qs("[data-quiz-primary-btn]");
    if (primaryBtn) {
      var isLast = current === QUESTIONS.length - 1;
      primaryBtn.textContent = isLast ? (isPractice ? "完成练习" : "提交答卷") : "下一题";
      primaryBtn.disabled = answers[current] === undefined;
    }

    var tip = qs("[data-quiz-tip]");
    if (tip) {
      if (isPractice) {
        tip.textContent = "练习模式：提交后不计积分、不影响连对天数，记录可在「答题记录」查看。";
      } else {
        tip.textContent = current === QUESTIONS.length - 1
          ? "全部题目作答完成后，点击「提交答卷」统一计分（每日仅计分一次）。"
          : "答题过程中不显示对错，请完成全部题目后再提交。";
      }
    }

    renderDots();
  }

  function selectOption(idx) {
    answers[current] = idx;
    qsa("[data-quiz-options] .option-btn").forEach(function (el, i) {
      el.classList.toggle("selected", i === idx);
    });
    if (qs("[data-quiz-primary-btn]")) qs("[data-quiz-primary-btn]").disabled = false;
    renderQuestion();
  }

  function allAnswered() {
    return QUESTIONS.every(function (_, i) { return answers[i] !== undefined; });
  }

  function scoreAnswers() {
    var correct = 0;
    QUESTIONS.forEach(function (q, i) {
      if (answers[i] === q.answer) correct += 1;
    });
    return correct;
  }

  function goNextOrSubmit() {
    if (answers[current] === undefined) return;

    if (current < QUESTIONS.length - 1) {
      current += 1;
      renderQuestion();
      return;
    }

    if (!allAnswered()) {
      alert("请先完成全部 " + QUESTIONS.length + " 道题再提交");
      return;
    }

    var correct = scoreAnswers();
    if (isPractice) {
      window.location.href = "20-quiz-practice-result.html?correct=" + correct;
      return;
    }

    if (correct === QUESTIONS.length) {
      window.location.href = "20-quiz-result-perfect.html";
    } else if (correct >= 3) {
      window.location.href = "20-quiz-result-partial.html?correct=" + correct;
    } else {
      window.location.href = "20-quiz-result-low.html?correct=" + correct;
    }
  }

  function initPracticeUI() {
    var banner = qs("[data-quiz-practice-banner]");
    var title = qs("[data-quiz-page-title]");
    var progressLabel = qs("[data-quiz-progress-label]");
    if (banner) banner.hidden = false;
    if (title) title.textContent = "🧠 练习模式";
    if (progressLabel) progressLabel.textContent = "练习进度";
    document.title = "每日答题 · 练习模式 · 宝山区小实验社区";
  }

  function initQuizFlow() {
    var root = qs("[data-quiz-flow]");
    if (!root) return;

    var param = new URLSearchParams(window.location.search);
    isPractice = param.get("mode") === "practice";
    if (isPractice) initPracticeUI();

    answers = new Array(QUESTIONS.length);
    var start = parseInt(param.get("q") || "1", 10);
    current = Math.min(Math.max(start - 1, 0), QUESTIONS.length - 1);
    for (var i = 0; i < current; i++) answers[i] = 0;

    renderQuestion();

    var primaryBtn = qs("[data-quiz-primary-btn]");
    if (primaryBtn) primaryBtn.addEventListener("click", goNextOrSubmit);

    var prevBtn = qs("[data-quiz-prev-btn]");
    if (prevBtn) {
      prevBtn.addEventListener("click", function () {
        if (current <= 0) return;
        current -= 1;
        renderQuestion();
      });
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initQuizFlow);
  } else {
    initQuizFlow();
  }
})();
