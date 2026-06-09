/* ============================================================
   宝山区小实验社区 移动端原型 - 共用初始化脚本
   1) Lucide 图标渲染
   2) Twemoji 渲染
   3) 输入框语音/键盘互锁切换 (右侧单图标)
   ============================================================ */
(function () {
  var TWEMOJI_BASE = "https://cdn.jsdelivr.net/gh/jdecked/twemoji@15.1.0/assets/";
  var INPUT_TOOL_TYPES = { text: 1, password: 1, search: 1, "": 1 };
  var ICON_KEYBOARD = "keyboard";
  var ICON_VOICE = "audio-lines";
  var MIC_ICON_PATHS = [
    "M12 14a3 3 0 0 0 3-3V6a3 3 0 1 0-6 0v5a3 3 0 0 0 3 3Z",
    "M17 11a5 5 0 0 1-10 0H5a7 7 0 0 0 6 6.93V21h2v-3.07A7 7 0 0 0 19 11h-2Z",
  ];

  function createMicIconSvg(className) {
    var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("aria-hidden", "true");
    svg.setAttribute("class", className);
    MIC_ICON_PATHS.forEach(function (d) {
      var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
      path.setAttribute("d", d);
      path.setAttribute("fill", "currentColor");
      svg.appendChild(path);
    });
    return svg;
  }

  function renderMicIcons() {
    document.querySelectorAll('[data-lucide="mic"]').forEach(function (el) {
      var classes = ["mic-icon-filled", "icon"];
      if (el.classList.contains("icon-lg") || el.getAttribute("data-mic-size") === "lg") {
        classes.push("mic-icon-filled--lg");
      }
      Array.from(el.classList).forEach(function (c) {
        if (c !== "icon" && classes.indexOf(c) === -1) classes.push(c);
      });
      el.replaceWith(createMicIconSvg(classes.join(" ")));
    });
  }

  function render() {
    renderMicIcons();
    if (window.lucide && typeof window.lucide.createIcons === "function") {
      window.lucide.createIcons();
    }
    if (window.twemoji && typeof window.twemoji.parse === "function") {
      window.twemoji.parse(document.body, {
        folder: "svg", ext: ".svg", base: TWEMOJI_BASE,
      });
    }
  }

  function isTextInput(el) {
    if (el.tagName === "TEXTAREA") return true;
    if (el.tagName !== "INPUT") return false;
    if (!el.classList.contains("input")) return false;
    var type = (el.getAttribute("type") || "text").toLowerCase();
    return !!INPUT_TOOL_TYPES[type];
  }

  function updateToggleIcon(toggleBtn, mode) {
    var icon = toggleBtn.querySelector(".icon");
    var isVoice = mode === "voice";
    icon.setAttribute("data-lucide", isVoice ? ICON_KEYBOARD : ICON_VOICE);
    toggleBtn.setAttribute("aria-label", isVoice ? "切换键盘输入" : "切换语音输入");
    toggleBtn.setAttribute("title", isVoice ? "键盘输入" : "语音输入");
    if (window.lucide && typeof window.lucide.createIcons === "function") {
      window.lucide.createIcons();
    }
  }

  function applyFieldMode(el, mode, options) {
    var isVoice = mode === "voice";
    if (isVoice) {
      if (!el.dataset.origPlaceholder && el.placeholder) {
        el.dataset.origPlaceholder = el.placeholder;
      }
      el.placeholder = el.tagName === "TEXTAREA"
        ? "点击麦克风，开始语音输入…"
        : (el.getAttribute("data-voice-placeholder") || "点击此处开始说话");
      el.setAttribute("readonly", "readonly");
    } else {
      if (el.dataset.origPlaceholder) {
        el.placeholder = el.dataset.origPlaceholder;
      }
      if (el.dataset.wasReadonly === "1") {
        el.setAttribute("readonly", "readonly");
      } else {
        el.removeAttribute("readonly");
      }
      if (options && options.focus && !el.disabled && el.dataset.wasReadonly !== "1") {
        try { el.focus(); } catch (e) { /* noop */ }
      }
    }
  }

  function setInputMode(ctx, mode, options) {
    var isVoice = mode === "voice";
    ctx.el.classList.toggle("input-mode-voice", isVoice);
    ctx.el.classList.toggle("input-mode-keyboard", !isVoice);
    if (ctx.wrap) {
      ctx.wrap.classList.toggle("input-mode-voice", isVoice);
      ctx.wrap.classList.toggle("input-mode-keyboard", !isVoice);
    }
    if (ctx.group) {
      ctx.group.classList.toggle("input-mode-voice", isVoice);
      ctx.group.classList.toggle("input-mode-keyboard", !isVoice);
    }
    updateToggleIcon(ctx.toggleBtn, mode);
    applyFieldMode(ctx.el, mode, options);
  }

  function createToggleButton(ctx) {
    var toggleBtn = document.createElement("button");
    toggleBtn.type = "button";
    toggleBtn.className = "input-tool-toggle";
    toggleBtn.innerHTML = '<i data-lucide="' + ICON_VOICE + '" class="icon"></i>';
    toggleBtn.setAttribute("aria-label", "切换语音输入");
    toggleBtn.setAttribute("title", "语音输入");

    toggleBtn.addEventListener("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      var next = ctx.el.classList.contains("input-mode-voice") ? "keyboard" : "voice";
      setInputMode(ctx, next, { focus: next === "keyboard" });
    });

    return toggleBtn;
  }

  function mountInputTools(el) {
    var group = el.closest(".input-group");
    var parent = el.parentNode;
    var ctx = { el: el, wrap: null, group: group, toggleBtn: null };

    if (el.hasAttribute("readonly")) el.dataset.wasReadonly = "1";

    var wrap = document.createElement("div");
    wrap.className = "input-wrap input-mode-keyboard";
    if (el.classList.contains("flex-1")) {
      wrap.classList.add("flex-1");
      el.classList.remove("flex-1");
    }

    parent.insertBefore(wrap, el);
    wrap.appendChild(el);

    if (group) {
      var leadingIcon = group.querySelector(":scope > .icon");
      if (leadingIcon) wrap.insertBefore(leadingIcon, el);

      var suffixBtn = group.querySelector(":scope > .icon-btn");
      if (suffixBtn) {
        suffixBtn.classList.add("input-suffix-btn");
        suffixBtn.style.right = "";
        suffixBtn.style.top = "";
        suffixBtn.style.transform = "";
      }

      group.classList.add("has-input-tools", "input-mode-keyboard");
    }

    if (el.style.paddingRight) el.style.paddingRight = "";

    ctx.wrap = wrap;
    ctx.toggleBtn = createToggleButton(ctx);
    wrap.appendChild(ctx.toggleBtn);

    setInputMode(ctx, "keyboard", { focus: false });
    el.dataset.inputToolsMounted = "1";
  }

  function resetIframeScroll() {
    if (window.self === window.top) return;
    window.scrollTo(0, 0);
    document.documentElement.scrollTop = 0;
    document.body.scrollTop = 0;
  }

  function markIframePreview() {
    if (window.self === window.top) return;
    document.documentElement.classList.add("in-iframe");
  }

  function wrapWithInputTools(el) {
    if (!isTextInput(el)) return;
    if (el.dataset.inputToolsMounted === "1") return;
    if (el.dataset.inputTools === "off") return;
    if (el.closest("[data-input-tools='off']")) return;
    if (el.disabled) return;
    if (el.closest(".input-wrap")) return;
    mountInputTools(el);
  }

  function initInputTools() {
    document.querySelectorAll("input.input, textarea.textarea").forEach(wrapWithInputTools);
    render();
  }

  function initHomeSearchToggle() {
    document.querySelectorAll("[data-home-search]").forEach(function (root) {
      var toggleBtn = root.querySelector("[data-home-search-toggle]");
      var panel = root.querySelector("[data-home-search-panel]");
      var cancelBtn = root.querySelector("[data-home-search-cancel]");
      var input = panel ? panel.querySelector("input") : null;
      if (!toggleBtn || !panel) return;

      function setOpen(open) {
        root.classList.toggle("is-search-open", open);
        toggleBtn.setAttribute("aria-expanded", open ? "true" : "false");
        panel.hidden = !open;
        if (open && input) {
          window.requestAnimationFrame(function () { input.focus(); });
        }
      }

      toggleBtn.addEventListener("click", function () {
        setOpen(!root.classList.contains("is-search-open"));
      });

      if (cancelBtn) {
        cancelBtn.addEventListener("click", function () { setOpen(false); });
      }

      document.addEventListener("keydown", function (e) {
        if (e.key === "Escape" && root.classList.contains("is-search-open")) {
          setOpen(false);
        }
      });
    });
  }

  function getInputToolsContext(el) {
    if (!el || el.dataset.inputToolsMounted !== "1") return null;
    var wrap = el.closest(".input-wrap");
    if (!wrap) return null;
    return {
      el: el,
      wrap: wrap,
      group: el.closest(".input-group"),
      toggleBtn: wrap.querySelector(".input-tool-toggle"),
    };
  }

  window.BSLabInputTools = {
    setMode: function (input, mode, options) {
      var ctx = getInputToolsContext(input);
      if (ctx) setInputMode(ctx, mode, options);
    },
    getMode: function (input) {
      if (!input) return "keyboard";
      return input.classList.contains("input-mode-voice") ? "voice" : "keyboard";
    },
  };

  function boot() {
    markIframePreview();
    initInputTools();
    initHomeSearchToggle();
    render();
    resetIframeScroll();
    requestAnimationFrame(resetIframeScroll);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }

  window.BSLabRenderIcons = render;
  window.BSLabInitInputTools = initInputTools;
})();
