(function () {
  "use strict";

  var STUDENT_NAME = "张小明";

  var VOICE_DEMO_PHRASES = [
    "我想做一个简单的风向标",
    "彩虹液体分层实验为什么不会混在一起",
    "气球火箭总是飞不远怎么办",
    "在家做科学小实验要注意什么",
    "帮我设计一个观察种子发芽的实验",
  ];

  var SCENARIOS = {
    wind: {
      label: "🧭 自制风向标",
      user: "我想做一个简单的风向标，该怎么做？",
      reply:
        "很好的探究方向！自制风向标可以分 4 步完成：\n\n" +
        "1️⃣ **准备材料**：硬纸板、吸管、铅笔、轻量箭头（卡纸）\n" +
        "2️⃣ **制作箭头**：剪出箭头与尾翼，确保两端重量接近平衡\n" +
        "3️⃣ **组装旋转轴**：把箭头固定在吸管上，吸管套在铅笔尖上\n" +
        "4️⃣ **测试记录**：到户外观察箭头指向，记录「北风 / 东风」等\n\n" +
        "💡 小提示：箭头要能在轴上自由转动，别装太紧哦。需要我帮你写实验记录表吗？"
    },
    rainbow: {
      label: "🌈 彩虹液体分层",
      user: "彩虹液体分层实验，为什么不同颜色的液体不会混在一起？",
      reply:
        "这是因为不同液体的**密度**不同！\n\n" +
        "密度大的液体会沉在下面，密度小的浮在上面，所以会形成漂亮的分层效果。\n\n" +
        "🧪 **常见顺序**（从下到上）：\n" +
        "• 蜂蜜 / 糖浆（最重）\n" +
        "• 洗洁精\n" +
        "• 清水\n" +
        "• 食用油（最轻）\n\n" +
        "倒液体时动作要慢，沿杯壁缓缓倒入，分层会更清晰。你作业里的「彩虹液体分层实验」就可以用这个原理来解释哦。"
    },
    balloon: {
      label: "🚀 气球火箭",
      user: "气球火箭总是飞不远，有什么改进办法？",
      reply:
        "别灰心，这说明你已经在认真做「控制变量」的思考了！试试这些改进：\n\n" +
        "✅ **减轻重量**：换更细的吸管、减少胶带用量\n" +
        "✅ **减少摩擦**：确保绳子拉直、固定点足够光滑\n" +
        "✅ **增加动力**：气球吹得饱满一些（注意安全，别过满）\n" +
        "✅ **调整角度**：发射端略低一点，减少与绳子的摩擦\n\n" +
        "建议做 3 次对比实验，记录飞行距离，把数据填进任务里，老师会很喜欢的！"
    },
    safety: {
      label: "⚠️ 实验安全",
      user: "在家做科学小实验，要注意哪些安全事项？",
      reply:
        "安全意识超棒！做家庭实验前请记住：\n\n" +
        "🛡️ **必须有大人陪同**，尤其使用剪刀、热水时\n" +
        "🧤 **戴护目镜**（或防护眼镜），防止液体溅入眼睛\n" +
        "🚫 **禁止品尝**实验材料，即使看起来像我彩虹糖\n" +
        "♻️ 实验结束及时**清理台面**，分类丢弃废弃物\n" +
        "📞 发生意外先告诉家长，必要时拨打急救电话\n\n" +
        "学校平台上传的成果照片里，记得展示你的安全防护装备哦。"
    },
    seed: {
      label: "🌱 种子发芽",
      user: "帮我设计一个观察种子发芽的实验方案",
      reply:
        "好的，给你一份适合小学阶段的探究方案：\n\n" +
        "📋 **课题**：种子发芽需要哪些条件？\n" +
        "🔬 **对照组设计**：\n" +
        "• A 组：有阳光 + 每天浇水\n" +
        "• B 组：无阳光 + 每天浇水\n" +
        "• C 组：有阳光 + 不浇水\n\n" +
        "📓 **记录内容**：每天拍照、记录高度、描述叶片变化\n" +
        "⏱ **周期建议**：7～10 天\n\n" +
        "你可以把记录表拍照上传，或问我怎么写「实验结论」部分。"
    }
  };

  var FALLBACK_REPLY =
    "收到你的想法啦！我建议先把问题拆成三步：\n\n" +
    "1. 你想观察或验证什么现象？\n" +
    "2. 需要准备哪些材料、怎么做对照？\n" +
    "3. 打算记录哪些数据或现象？\n\n" +
    "也可以点下面的快捷话题，我们按场景一步步聊～";

  function qs(sel, root) { return (root || document).querySelector(sel); }
  function qsa(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  function escapeHtml(text) {
    return text
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function formatReply(text) {
    return escapeHtml(text)
      .replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>")
      .replace(/\n/g, "<br>");
  }

  function scrollToBottom(container) {
    container.scrollTop = container.scrollHeight;
  }

  function createAiRow(html) {
    var row = document.createElement("div");
    row.className = "assistant-msg assistant-msg--ai row items-start gap-3 anim-fade-up";
    row.innerHTML =
      '<div class="avatar avatar-sm avatar-grad-warm shrink-0" aria-hidden="true">🪨</div>' +
      '<div class="msg-bubble msg-ai">' +
      '<div class="text-xs font-bold text-brand mb-1">🪨 石头老师</div>' +
      html +
      "</div>";
    return row;
  }

  function createUserRow(text) {
    var row = document.createElement("div");
    row.className = "assistant-msg assistant-msg--user row items-start gap-3 justify-end anim-fade-up";
    row.innerHTML =
      '<div class="msg-bubble msg-user">' + escapeHtml(text) + "</div>" +
      '<div class="avatar avatar-sm avatar-grad-warm shrink-0" aria-hidden="true">张</div>';
    return row;
  }

  function createTypingRow() {
    var row = document.createElement("div");
    row.className = "assistant-msg assistant-msg--ai row items-start gap-3";
    row.setAttribute("data-assistant-typing", "");
    row.innerHTML =
      '<div class="avatar avatar-sm avatar-grad-warm shrink-0" aria-hidden="true">🪨</div>' +
      '<div class="msg-bubble msg-ai assistant-typing">' +
      '<span class="assistant-typing__dot"></span>' +
      '<span class="assistant-typing__dot"></span>' +
      '<span class="assistant-typing__dot"></span>' +
      "</div>";
    return row;
  }

  function findScenarioReply(text) {
    var trimmed = (text || "").trim();
    var key;
    for (key in SCENARIOS) {
      if (!SCENARIOS.hasOwnProperty(key)) continue;
      var scenario = SCENARIOS[key];
      if (trimmed === scenario.user) return scenario.reply;
    }
    for (key in SCENARIOS) {
      if (!SCENARIOS.hasOwnProperty(key)) continue;
      scenario = SCENARIOS[key];
      if (trimmed.indexOf(scenario.label.replace(/^[^\s]+\s/, "")) !== -1) return scenario.reply;
    }
    return FALLBACK_REPLY;
  }

  function hidePrompts(root) {
    var prompts = qs("[data-assistant-prompts]", root);
    if (prompts) prompts.hidden = true;
  }

  function initAssistantChat() {
    var root = qs("[data-assistant-chat]");
    if (!root) return;

    var input = qs("[data-assistant-input]", root);
    var sendBtn = qs("[data-assistant-send]", root);
    var messages = qs("[data-assistant-messages]", root);
    var voicePanel = qs("[data-assistant-voice-panel]", root);
    var voiceStatus = qs("[data-assistant-voice-status]", root);
    var voicePreview = qs("[data-assistant-voice-preview]", root);

    var isBusy = false;
    var isListening = false;
    var recognition = null;
    var voiceTimers = [];
    var demoPhraseIndex = 0;

    function setInputToolsMode(mode, options) {
      if (window.BSLabInputTools && input) {
        window.BSLabInputTools.setMode(input, mode, options);
      }
    }

    function isVoiceMode() {
      return window.BSLabInputTools
        ? window.BSLabInputTools.getMode(input) === "voice"
        : false;
    }

    function updateComposerState() {
      var hasText = input && input.value.trim().length > 0;
      if (sendBtn) sendBtn.disabled = isBusy || isListening || !hasText;
      if (input) input.disabled = isBusy;
      var toggleBtn = input && input.closest(".input-wrap")
        ? input.closest(".input-wrap").querySelector(".input-tool-toggle")
        : null;
      if (toggleBtn) toggleBtn.disabled = isBusy || isListening;
    }

    function clearVoiceTimers() {
      voiceTimers.forEach(function (id) { window.clearTimeout(id); });
      voiceTimers = [];
    }

    function stopRecognition() {
      if (!recognition) return;
      try {
        recognition.abort();
      } catch (e) {
        try { recognition.stop(); } catch (e2) { /* noop */ }
      }
      recognition = null;
    }

    function setVoicePanel(open) {
      if (!voicePanel) return;
      voicePanel.hidden = !open;
      if (!open && voicePreview) voicePreview.textContent = "";
      if (!open && voiceStatus) voiceStatus.textContent = "正在听，请说话…";
    }

    function setListening(active) {
      isListening = active;
      setVoicePanel(active);
      updateComposerState();
    }

    function pickDemoPhrase() {
      var phrase = VOICE_DEMO_PHRASES[demoPhraseIndex % VOICE_DEMO_PHRASES.length];
      demoPhraseIndex += 1;
      return phrase;
    }

    function typeIntoInput(text, onDone) {
      if (!input) return;
      input.value = "";
      updateComposerState();

      var index = 0;
      function tick() {
        if (index >= text.length) {
          updateComposerState();
          if (onDone) onDone();
          return;
        }
        input.value += text.charAt(index);
        index += 1;
        input.dispatchEvent(new Event("input", { bubbles: true }));
        updateComposerState();
        var delay = 28 + Math.floor(Math.random() * 32);
        voiceTimers.push(window.setTimeout(tick, delay));
      }
      tick();
    }

    function finishVoiceInput(text) {
      if (!isListening) return;
      var finalText = (text || "").trim();
      stopRecognition();
      clearVoiceTimers();
      if (voiceStatus) voiceStatus.textContent = "识别完成，正在填入…";

      window.setTimeout(function () {
        setListening(false);
        if (!finalText) {
          setInputToolsMode("voice", { focus: false });
          updateComposerState();
          return;
        }
        setInputToolsMode("keyboard", { focus: false });
        typeIntoInput(finalText, function () {
          try {
            input.focus();
            input.setSelectionRange(input.value.length, input.value.length);
          } catch (e) { /* noop */ }
          updateComposerState();
        });
      }, 350);
    }

    function simulateVoiceCapture() {
      var phrase = pickDemoPhrase();
      var index = 0;
      if (voiceStatus) voiceStatus.textContent = "正在听，请说话…";

      function tick() {
        if (!isListening) return;
        index += 1;
        var partial = phrase.slice(0, index);
        if (voicePreview) voicePreview.textContent = partial;
        if (index < phrase.length) {
          voiceTimers.push(window.setTimeout(tick, 70 + Math.floor(Math.random() * 50)));
        } else {
          voiceTimers.push(window.setTimeout(function () {
            finishVoiceInput(phrase);
          }, 450));
        }
      }

      voiceTimers.push(window.setTimeout(tick, 500));
    }

    function startWebSpeech() {
      var SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      if (!SpeechRecognition) {
        simulateVoiceCapture();
        return;
      }

      stopRecognition();
      recognition = new SpeechRecognition();
      recognition.lang = "zh-CN";
      recognition.interimResults = true;
      recognition.continuous = false;

      recognition.onresult = function (event) {
        if (!isListening) return;
        var interim = "";
        var finalText = "";
        for (var i = event.resultIndex; i < event.results.length; i++) {
          var piece = event.results[i][0].transcript;
          if (event.results[i].isFinal) finalText += piece;
          else interim += piece;
        }
        if (voicePreview) voicePreview.textContent = finalText || interim;
        if (finalText) {
          finishVoiceInput(finalText.trim());
        }
      };

      recognition.onerror = function () {
        if (!isListening) return;
        simulateVoiceCapture();
      };

      recognition.onend = function () {
        if (!isListening) return;
        if (voicePreview && !voicePreview.textContent.trim()) {
          simulateVoiceCapture();
        }
      };

      try {
        recognition.start();
      } catch (e) {
        simulateVoiceCapture();
      }
    }

    function startVoiceCapture() {
      if (isBusy || isListening || !input) return;
      if (!isVoiceMode()) setInputToolsMode("voice", { focus: false });
      setListening(true);
      startWebSpeech();
    }

    function cancelVoiceCapture() {
      if (!isListening) return;
      stopRecognition();
      clearVoiceTimers();
      setListening(false);
      updateComposerState();
    }

    function sendTurn(userText, forcedReply) {
      if (!messages) return;

      var text = (userText || "").trim();
      if (!text || isBusy) return;

      cancelVoiceCapture();
      isBusy = true;
      updateComposerState();

      if (input) input.value = "";

      messages.appendChild(createUserRow(text));
      hidePrompts(root);
      scrollToBottom(messages);

      var typing = createTypingRow();
      messages.appendChild(typing);
      scrollToBottom(messages);

      window.setTimeout(function () {
        if (typing.parentNode) typing.parentNode.removeChild(typing);
        var reply = forcedReply || findScenarioReply(text);
        messages.appendChild(createAiRow(formatReply(reply)));
        scrollToBottom(messages);
        isBusy = false;
        updateComposerState();
        if (window.lucide && typeof window.lucide.createIcons === "function") {
          window.lucide.createIcons();
        }
        if (input && !input.disabled) {
          try { input.focus(); } catch (e) { /* noop */ }
        }
      }, 700);
    }

    function playScenario(key) {
      var scenario = SCENARIOS[key];
      if (!scenario) return;
      sendTurn(scenario.user, scenario.reply);
    }

    function handleSend() {
      if (!input || isBusy || isListening) return;
      sendTurn(input.value);
    }

    qsa("[data-assistant-scenario]", root).forEach(function (btn) {
      btn.addEventListener("click", function (e) {
        e.preventDefault();
        if (isBusy || isListening) return;
        playScenario(btn.getAttribute("data-assistant-scenario"));
      });
    });

    if (sendBtn) {
      sendBtn.addEventListener("click", function (e) {
        e.preventDefault();
        handleSend();
      });
    }

    if (input) {
      input.setAttribute("data-voice-placeholder", "点击此处开始说话");

      input.addEventListener("input", updateComposerState);

      input.addEventListener("keydown", function (e) {
        if (e.key === "Enter" && !e.shiftKey) {
          e.preventDefault();
          handleSend();
        }
        if (e.key === "Escape" && isListening) {
          e.preventDefault();
          cancelVoiceCapture();
        }
      });

      input.addEventListener("click", function () {
        if (isVoiceMode() && !isBusy && !isListening) {
          startVoiceCapture();
        }
      });
    }

    function hookInputToolsToggle() {
      if (!input) return;
      var toggleBtn = input.closest(".input-wrap")
        ? input.closest(".input-wrap").querySelector(".input-tool-toggle")
        : null;
      if (!toggleBtn || toggleBtn.dataset.assistantHooked === "1") return;
      toggleBtn.dataset.assistantHooked = "1";
      toggleBtn.addEventListener("click", function () {
        window.setTimeout(function () {
          if (isListening && !isVoiceMode()) {
            cancelVoiceCapture();
          }
          updateComposerState();
        }, 0);
      });
    }

    function ensureInputTools() {
      if (window.BSLabInitInputTools) window.BSLabInitInputTools();
      hookInputToolsToggle();
      updateComposerState();
    }

    ensureInputTools();
    window.setTimeout(ensureInputTools, 0);

    var scene = new URLSearchParams(window.location.search).get("scene");
    if (scene && SCENARIOS[scene]) {
      window.setTimeout(function () {
        playScenario(scene);
      }, 400);
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initAssistantChat);
  } else {
    initAssistantChat();
  }
})();
