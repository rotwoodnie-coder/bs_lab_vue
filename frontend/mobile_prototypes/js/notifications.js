(function () {
  "use strict";

  var STORAGE_KEY = "bslab-notifications-read";

  var TYPE_META = {
    task: { label: "学习任务", tab: "study", icon: "clipboard-list", tone: "brand" },
    grade: { label: "批阅反馈", tab: "study", icon: "check-circle", tone: "success" },
    achievement: { label: "成就提醒", tab: "study", icon: "award", tone: "warning" },
    social: { label: "互动消息", tab: "social", icon: "message-circle", tone: "violet" },
    system: { label: "系统公告", tab: "system", icon: "megaphone", tone: "slate" }
  };

  var NOTIFICATIONS = [
    {
      id: "grade-rainbow",
      type: "grade",
      title: "李老师批阅了你的作业",
      preview: "「彩虹液体分层」获 A+ · 实验操作规范，分层效果非常明显",
      body: "你的作业「彩虹液体分层实验」已完成批阅，可在作品墙查看完整评语与老师建议。",
      time: "2026-06-03T14:30:00",
      timeLabel: "2 小时前",
      dateGroup: "today",
      unread: true,
      sender: { name: "李老师", avatar: "李", role: "teacher" },
      highlight: "实验操作规范，照片清晰，分层效果非常明显！",
      cta: { label: "查看作业与评语", href: "13-work-detail.html" },
      detailBlocks: [
        { type: "score-card", score: "A+", label: "作业成绩", sub: "彩虹液体分层实验" },
        {
          type: "meta-grid",
          title: "批阅信息",
          rows: [
            { label: "作品名称", value: "彩虹液体分层实验" },
            { label: "批阅老师", value: "李老师 · 科学" },
            { label: "提交时间", value: "2026-05-28 14:30" },
            { label: "批阅时间", value: "今天 14:30" }
          ]
        },
        {
          type: "quote",
          title: "老师评语",
          text: "实验操作规范，照片清晰，分层效果非常明显！如果能标注每种液体的名称就更完整了。继续加油！",
          footer: "— 李老师"
        }
      ]
    },
    {
      id: "task-rainbow",
      type: "task",
      title: "新任务：彩虹液体分层实验",
      preview: "李老师布置 · 截止时间今天 20:00",
      body: "请按步骤完成实验并上传成果。建议先观看关联视频，再动手操作。",
      time: "2026-06-03T11:00:00",
      timeLabel: "5 小时前",
      dateGroup: "today",
      unread: true,
      sender: { name: "李老师", avatar: "李", role: "teacher" },
      highlight: "截止时间今天 20:00，别忘了上传步骤照片。",
      cta: { label: "去完成任务", href: "11-task-detail.html?type=homework" },
      detailBlocks: [
        { type: "deadline", title: "截止提醒", time: "今天 20:00 · 还剩约 5 小时" },
        {
          type: "meta-grid",
          title: "任务信息",
          rows: [
            { label: "任务类型", value: "📚 老师布置作业" },
            { label: "布置老师", value: "李老师" },
            { label: "关联视频", value: "安全实验：彩虹液体分层" },
            { label: "当前状态", value: "待完成" }
          ]
        },
        {
          type: "checklist",
          title: "提交要求",
          items: [
            "至少上传 1 张步骤照片",
            "可附 1 段 30 秒以内视频",
            "需写明实验名称和观察结果"
          ]
        }
      ]
    },
    {
      id: "task-deadline",
      type: "task",
      title: "任务即将截止：气球火箭挑战赛",
      preview: "距离截止还有 3 小时 · 尚未提交成果",
      body: "你的作业「气球火箭挑战赛」尚未提交。请在截止前完成实验并上传飞行距离记录。",
      time: "2026-06-03T17:00:00",
      timeLabel: "1 小时前",
      dateGroup: "today",
      unread: true,
      sender: { name: "李老师", avatar: "李", role: "teacher" },
      highlight: "截止今天 20:00，完成后记得上传数据与照片。",
      cta: { label: "立即去提交", href: "11-task-detail.html?type=homework" },
      detailBlocks: [
        { type: "deadline", title: "紧急 · 即将截止", time: "今天 20:00 · 剩余约 3 小时" },
        {
          type: "meta-grid",
          title: "任务进度",
          rows: [
            { label: "任务名称", value: "气球火箭挑战赛" },
            { label: "完成进度", value: "0% · 未提交" },
            { label: "布置老师", value: "李老师" }
          ]
        },
        {
          type: "checklist",
          title: "还需完成",
          items: ["完成气球火箭实验", "记录飞行距离", "上传照片或短视频"]
        }
      ]
    },
    {
      id: "social-comment",
      type: "social",
      title: "小科学迷 评论了你的作品",
      preview: "「好漂亮的彩虹！我也想试试，请问用的什么容器？」",
      body: "有同学对你的实验感兴趣，回复 TA 可以分享经验，也能获得互动积分。",
      time: "2026-06-02T18:20:00",
      timeLabel: "昨天 18:20",
      dateGroup: "yesterday",
      unread: false,
      sender: { name: "小科学迷", avatar: "小", role: "student" },
      cta: { label: "查看作品与评论", href: "13-work-detail.html" },
      ctaSecondary: { label: "回复评论", href: "13-work-detail.html" },
      detailBlocks: [
        {
          type: "meta-grid",
          title: "互动来源",
          rows: [
            { label: "互动类型", value: "💬 作品评论" },
            { label: "相关作品", value: "彩虹液体分层实验" },
            { label: "评论者", value: "小科学迷 · 同学" }
          ]
        },
        {
          type: "quote",
          title: "评论内容",
          text: "好漂亮的彩虹！我也想试试，请问用的什么容器？",
          footer: "昨天 18:20"
        }
      ]
    },
    {
      id: "social-like",
      type: "social",
      title: "王老师 赞了你的作品",
      preview: "「彩虹液体分层实验」获得老师推荐点赞",
      body: "作品被推荐至班级精选，更多同学可以看到你的实验成果。",
      time: "2026-06-02T10:15:00",
      timeLabel: "昨天 10:15",
      dateGroup: "yesterday",
      unread: false,
      sender: { name: "王老师", avatar: "王", role: "teacher" },
      cta: { label: "查看作品", href: "13-work-detail.html" },
      detailBlocks: [
        { type: "like-row", who: "王老师", action: "赞了你的作品「彩虹液体分层实验」" },
        {
          type: "meta-grid",
          rows: [
            { label: "互动类型", value: "👍 老师点赞" },
            { label: "推荐状态", value: "已入选班级精选" },
            { label: "累计获赞", value: "24 次" }
          ]
        }
      ]
    },
    {
      id: "achievement-badge",
      type: "achievement",
      title: "恭喜你获得勋章「实验小达人」",
      preview: "累计完成 5 次实验 · 积分 +20",
      body: "坚持完成实验是成为小科学家的第一步。继续探索，解锁更多成就吧！",
      time: "2026-05-31T09:15:00",
      timeLabel: "3 天前",
      dateGroup: "earlier",
      unread: false,
      cta: { label: "查看我的勋章", href: "19-badges.html" },
      detailBlocks: [
        { type: "badge-hero", emoji: "🏅", title: "实验小达人", subtitle: "累计完成 5 次实验提交", points: "+20 积分" },
        {
          type: "meta-grid",
          title: "成就详情",
          rows: [
            { label: "解锁条件", value: "累计提交 5 次实验作品" },
            { label: "当前进度", value: "5 / 5 已完成" },
            { label: "下一目标", value: "创意探索家（10 次）" }
          ]
        }
      ]
    },
    {
      id: "achievement-streak",
      type: "achievement",
      title: "每日答题连对 4 天！",
      preview: "继续保持 · 再连对 3 天解锁「答题王者」进度",
      body: "连续答对说明知识点掌握不错，明天记得继续完成每日答题。",
      time: "2026-06-03T09:00:00",
      timeLabel: "今天 09:00",
      dateGroup: "today",
      unread: false,
      cta: { label: "查看答题记录", href: "20-quiz-history.html" },
      detailBlocks: [
        { type: "badge-hero", emoji: "🔥", title: "连对 4 天", subtitle: "每日答题正式提交全对", points: "+10 连对奖励" },
        {
          type: "meta-grid",
          title: "答题统计",
          rows: [
            { label: "今日得分", value: "5 / 5 全对" },
            { label: "连对天数", value: "4 天" },
            { label: "勋章进度", value: "答题王者 12 / 20" }
          ]
        }
      ]
    },
    {
      id: "system-science-week",
      type: "system",
      title: "科学周活动通知",
      preview: "本周起开展「校园科学周」主题活动",
      body: "欢迎参与校园科学周，展示你的探索精神。详情如下：",
      time: "2026-06-03T08:00:00",
      timeLabel: "今天 08:00",
      dateGroup: "today",
      unread: false,
      sender: { name: "宝山实验平台", avatar: "🧪", role: "system" },
      highlight: "完成至少 1 个家庭小实验并上传成果，优秀作品将展示在作品墙。",
      cta: { label: "去上传创意实验", href: "14-upload.html?type=creative" },
      detailBlocks: [
        {
          type: "meta-grid",
          title: "活动概要",
          rows: [
            { label: "活动名称", value: "校园科学周" },
            { label: "活动时间", value: "6 月 3 日 – 6 月 9 日" },
            { label: "发布单位", value: "宝山区小实验社区" }
          ]
        },
        {
          type: "checklist",
          title: "参与方式",
          items: [
            "在「创意实验」中上传家庭小实验作品",
            "或完成老师布置的相关科学周任务",
            "优秀作品展示在作品墙并评选「科学周之星」"
          ]
        }
      ]
    },
    {
      id: "system-holiday",
      type: "system",
      title: "端午节实验室开放安排",
      preview: "6/8–6/10 部分模拟实验维护",
      body: "维护期间部分功能暂不可用，其他学习功能正常使用。",
      time: "2026-05-29T10:00:00",
      timeLabel: "5 天前",
      dateGroup: "earlier",
      unread: false,
      cta: { label: "查看模拟实验", href: "07-virtual-exp-list.html" },
      detailBlocks: [
        {
          type: "meta-grid",
          title: "维护通知",
          rows: [
            { label: "维护时间", value: "6 月 8 日 – 6 月 10 日" },
            { label: "影响范围", value: "3 项在线模拟实验" },
            { label: "不受影响", value: "视频实验、作业提交、作品墙" }
          ]
        },
        {
          type: "checklist",
          title: "暂不可用模拟",
          items: ["化学 · 液体分层", "物理 · 杠杆平衡", "生物 · 细胞观察"]
        }
      ]
    }
  ];

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function qs(sel, root) {
    return (root || document).querySelector(sel);
  }

  function qsa(sel, root) {
    return Array.prototype.slice.call((root || document).querySelectorAll(sel));
  }

  function getReadIds() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    } catch (e) {
      return [];
    }
  }

  function saveReadIds(ids) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(ids));
    } catch (e) { /* ignore */ }
  }

  function isRead(id) {
    return getReadIds().indexOf(id) !== -1;
  }

  function markRead(id) {
    var ids = getReadIds();
    if (ids.indexOf(id) === -1) {
      ids.push(id);
      saveReadIds(ids);
    }
  }

  function markAllRead() {
    saveReadIds(NOTIFICATIONS.map(function (n) { return n.id; }));
  }

  function enrichNotification(item) {
    var meta = TYPE_META[item.type] || TYPE_META.system;
    return Object.assign({}, item, {
      meta: meta,
      isUnread: item.unread !== false && !isRead(item.id)
    });
  }

  function getAllNotifications() {
    return NOTIFICATIONS.map(enrichNotification);
  }

  function getNotificationById(id) {
    var found = NOTIFICATIONS.filter(function (n) { return n.id === id; })[0];
    return found ? enrichNotification(found) : null;
  }

  function getUnreadCount() {
    return getAllNotifications().filter(function (n) { return n.isUnread; }).length;
  }

  function filterNotifications(tab) {
    var list = getAllNotifications();
    if (tab === "unread") return list.filter(function (n) { return n.isUnread; });
    if (tab === "study") return list.filter(function (n) { return n.meta.tab === "study"; });
    if (tab === "social") return list.filter(function (n) { return n.meta.tab === "social"; });
    if (tab === "system") return list.filter(function (n) { return n.meta.tab === "system"; });
    return list;
  }

  function groupLabel(key) {
    if (key === "today") return "今天";
    if (key === "yesterday") return "昨天";
    return "更早";
  }

  function groupNotifications(list) {
    var order = ["today", "yesterday", "earlier"];
    var groups = {};
    list.forEach(function (item) {
      var key = item.dateGroup || "earlier";
      if (!groups[key]) groups[key] = [];
      groups[key].push(item);
    });
    return order.filter(function (key) { return groups[key] && groups[key].length; }).map(function (key) {
      return { key: key, label: groupLabel(key), items: groups[key] };
    });
  }

  function renderIcon(item) {
    var tone = item.meta.tone;
    var avatar = item.sender && item.sender.avatar;
    if (avatar && avatar.length === 1) {
      return '<span class="notif-item__avatar avatar avatar-sm avatar-grad-' + (tone === "brand" ? "cool" : tone === "violet" ? "violet-indigo" : "warm") + '">' + avatar + "</span>";
    }
    return '<span class="notif-item__icon notif-item__icon--' + tone + '"><i data-lucide="' + item.meta.icon + '" class="icon"></i></span>';
  }

  function renderListItem(item) {
    var unreadClass = item.isUnread ? " notif-item--unread" : "";
    return (
      '<a href="27-notification-detail.html?id=' + encodeURIComponent(item.id) + '" class="notif-item card card-pad card-link' + unreadClass + '" data-notif-id="' + item.id + '">' +
        '<div class="notif-item__inner row gap-3">' +
          renderIcon(item) +
          '<div class="notif-item__body min-w-0 flex-1">' +
            '<div class="row items-start justify-between gap-2">' +
              '<span class="notif-type-badge notif-type-badge--' + item.type + '">' + item.meta.label + "</span>" +
              '<span class="text-xs muted shrink-0">' + item.timeLabel + "</span>" +
            "</div>" +
            '<div class="text-sm font-bold mt-1">' + item.title + "</div>" +
            '<p class="text-xs muted mt-1 line-clamp-2">' + item.preview + "</p>" +
          "</div>" +
          (item.isUnread ? '<span class="notif-item__dot" aria-label="未读"></span>' : "") +
        "</div>" +
      "</a>"
    );
  }

  function renderList(groups) {
    if (!groups.length) {
      return (
        '<div class="notif-empty col items-center text-center py-10">' +
          '<div class="notif-empty__icon">🔔</div>' +
          '<p class="text-sm font-bold mt-3">暂无消息</p>' +
          '<p class="text-xs muted mt-1">作业提醒、互动与公告会出现在这里</p>' +
        "</div>"
      );
    }
    return groups.map(function (group) {
      return (
        '<section class="notif-group">' +
          '<h2 class="notif-group__title">' + group.label + "</h2>" +
          '<div class="stack-2">' + group.items.map(renderListItem).join("") + "</div>" +
        "</section>"
      );
    }).join("");
  }

  function refreshIcons() {
    if (window.lucide && typeof window.lucide.createIcons === "function") {
      window.lucide.createIcons();
    }
  }

  function updateBellBadges() {
    var count = getUnreadCount();
    qsa("[data-notifications-bell]").forEach(function (bell) {
      var badge = qs(".pad-home__bell-badge", bell) || qs("[data-notifications-badge]", bell);
      if (!badge) {
        badge = document.createElement("span");
        badge.className = "pad-home__bell-badge";
        badge.setAttribute("data-notifications-badge", "");
        bell.appendChild(badge);
      }
      if (count > 0) {
        badge.textContent = count > 99 ? "99+" : String(count);
        badge.hidden = false;
      } else {
        badge.hidden = true;
      }
    });
  }

  function initListPage() {
    var root = qs("[data-notifications-list]");
    if (!root) return;

    var container = qs("[data-notifications-list-body]", root) || root;
    var summaryEl = qs("[data-notifications-summary]", root);
    var markAllBtn = qs("[data-notifications-mark-all]", root);
    var tabs = qsa("[data-notif-tab]", root);
    var activeTab = "all";

    function paint() {
      var list = filterNotifications(activeTab);
      var unread = getUnreadCount();
      if (summaryEl) {
        summaryEl.innerHTML = unread
          ? '<span class="notif-summary__count">' + unread + ' 条未读</span><span class="notif-summary__hint">点击查看详情，处理后会自动标记已读</span>'
          : '<span class="notif-summary__hint">全部已读 · 保持关注新的学习动态</span>';
      }
      if (markAllBtn) markAllBtn.hidden = unread === 0;
      container.innerHTML = renderList(groupNotifications(list));
      refreshIcons();
      updateBellBadges();
    }

    tabs.forEach(function (tab) {
      tab.addEventListener("click", function () {
        activeTab = tab.getAttribute("data-notif-tab") || "all";
        tabs.forEach(function (t) { t.classList.toggle("active", t === tab); });
        paint();
      });
    });

    if (markAllBtn) {
      markAllBtn.addEventListener("click", function () {
        markAllRead();
        paint();
      });
    }

    paint();
  }

  function renderSenderIcon(sender, tone) {
    if (!sender) return "";
    if (sender.avatar && sender.avatar.length === 1) {
      return '<span class="avatar avatar-sm avatar-grad-cool">' + escapeHtml(sender.avatar) + "</span>";
    }
    if (sender.avatar && sender.avatar.length > 1) {
      return '<span class="notif-detail-hero__emoji">' + sender.avatar + "</span>";
    }
    return '<span class="notif-item__icon notif-item__icon--' + tone + '"><i data-lucide="building-2" class="icon"></i></span>';
  }

  function renderDetailBlock(block) {
    if (!block || !block.type) return "";

    switch (block.type) {
      case "score-card":
        return (
          '<div class="notif-detail-block notif-detail-block--score">' +
            '<div class="notif-detail-score">' + escapeHtml(block.score) + "</div>" +
            '<div class="notif-detail-score__label">' + escapeHtml(block.label) + "</div>" +
            (block.sub ? '<div class="notif-detail-score__sub">' + escapeHtml(block.sub) + "</div>" : "") +
          "</div>"
        );
      case "meta-grid":
        return (
          '<div class="notif-detail-block card card-pad">' +
            (block.title ? '<h2 class="notif-detail-block__title">' + escapeHtml(block.title) + "</h2>" : "") +
            '<dl class="notif-detail-meta">' +
              (block.rows || []).map(function (row) {
                return (
                  '<div class="notif-detail-meta__row">' +
                    "<dt>" + escapeHtml(row.label) + "</dt>" +
                    "<dd>" + escapeHtml(row.value) + "</dd>" +
                  "</div>"
                );
              }).join("") +
            "</dl>" +
          "</div>"
        );
      case "quote":
        return (
          '<div class="notif-detail-block notif-detail-block--quote">' +
            (block.title ? '<div class="notif-detail-block__title">' + escapeHtml(block.title) + "</div>" : "") +
            '<blockquote class="notif-detail-quote">' + escapeHtml(block.text) + "</blockquote>" +
            (block.footer ? '<div class="notif-detail-quote__foot">' + escapeHtml(block.footer) + "</div>" : "") +
          "</div>"
        );
      case "badge-hero":
        return (
          '<div class="notif-detail-block notif-detail-block--badge">' +
            '<div class="notif-detail-badge">' +
              '<span class="notif-detail-badge__emoji">' + (block.emoji || "🏅") + "</span>" +
              '<div class="notif-detail-badge__name">' + escapeHtml(block.title) + "</div>" +
              (block.subtitle ? '<div class="notif-detail-badge__sub">' + escapeHtml(block.subtitle) + "</div>" : "") +
              (block.points ? '<span class="notif-detail-badge__points">' + escapeHtml(block.points) + "</span>" : "") +
            "</div>" +
          "</div>"
        );
      case "checklist":
        return (
          '<div class="notif-detail-block card card-pad">' +
            (block.title ? '<h2 class="notif-detail-block__title">' + escapeHtml(block.title) + "</h2>" : "") +
            '<ul class="notif-detail-checklist">' +
              (block.items || []).map(function (li) { return "<li>" + escapeHtml(li) + "</li>"; }).join("") +
            "</ul>" +
          "</div>"
        );
      case "deadline":
        return (
          '<div class="notif-detail-block notif-detail-block--deadline">' +
            '<span class="notif-detail-deadline__icon"><i data-lucide="clock" class="icon"></i></span>' +
            "<div>" +
              '<div class="notif-detail-deadline__title">' + escapeHtml(block.title) + "</div>" +
              '<div class="notif-detail-deadline__time">' + escapeHtml(block.time) + "</div>" +
            "</div>" +
          "</div>"
        );
      case "like-row":
        return (
          '<div class="notif-detail-block notif-detail-block--like">' +
            '<span class="notif-detail-like__icon">❤️</span>' +
            "<div>" +
              '<div class="notif-detail-like__who">' + escapeHtml(block.who) + "</div>" +
              '<div class="notif-detail-like__action">' + escapeHtml(block.action) + "</div>" +
            "</div>" +
          "</div>"
        );
      default:
        return "";
    }
  }

  function renderDetailBlocks(item) {
    if (!item.detailBlocks || !item.detailBlocks.length) return "";
    return (
      '<div class="notif-detail-blocks stack-3">' +
        item.detailBlocks.map(renderDetailBlock).join("") +
      "</div>"
    );
  }

  function initDetailPage() {
    var mount = qs("[data-notification-detail-body]");
    if (!mount) return;

    var params = new URLSearchParams(window.location.search);
    var id = params.get("id");
    var item = id ? getNotificationById(id) : null;

    if (!item) {
      mount.innerHTML =
        '<div class="px-4 py-10 text-center">' +
          '<p class="text-sm font-bold">消息不存在或已失效</p>' +
          '<a href="27-notifications.html" class="btn btn-primary mt-4">返回消息列表</a>' +
        "</div>";
      return;
    }

    markRead(id);
    updateBellBadges();

    var tone = item.meta.tone;
    var titleEl = qs(".topbar-title", qs("[data-notification-detail]"));
    if (titleEl) titleEl.textContent = item.meta.label;

    document.title = item.meta.label + " · " + item.title + " · 宝山区小实验社区";

    var senderHtml = "";
    if (item.sender) {
      senderHtml =
        '<div class="notif-detail__sender">' +
          renderSenderIcon(item.sender, tone) +
          '<div class="notif-detail__sender-text">' +
            '<div class="text-sm font-bold">' + escapeHtml(item.sender.name) + "</div>" +
            '<div class="text-xs muted">' + escapeHtml(item.timeLabel) + "</div>" +
          "</div>" +
        "</div>";
    }

    var highlightHtml = item.highlight
      ? '<div class="notif-detail__highlight notif-detail__highlight--' + item.type + '">' + escapeHtml(item.highlight) + "</div>"
      : "";

    var bodyHtml = item.body
      ? '<div class="notif-detail__body text-sm leading-relaxed">' + escapeHtml(item.body) + "</div>"
      : "";

    var ctaHtml = item.cta
      ? '<a href="' + item.cta.href + '" class="btn btn-gradient btn-block btn-lg notif-detail__cta notif-detail__cta--' + item.type + '">' + escapeHtml(item.cta.label) + "</a>"
      : "";

    var ctaSecondaryHtml = item.ctaSecondary
      ? '<a href="' + item.ctaSecondary.href + '" class="btn btn-outline btn-block">' + escapeHtml(item.ctaSecondary.label) + "</a>"
      : "";

    mount.innerHTML =
      '<article class="notif-detail notif-detail--' + item.type + '">' +
        '<div class="notif-detail-hero notif-detail-hero--' + item.type + '">' +
          '<span class="notif-detail-hero__icon notif-item__icon notif-item__icon--' + tone + '">' +
            '<i data-lucide="' + item.meta.icon + '" class="icon"></i>' +
          "</span>" +
          '<span class="notif-type-badge notif-type-badge--' + item.type + '">' + item.meta.label + "</span>" +
        "</div>" +
        '<div class="notif-detail__main px-4 pb-28">' +
          '<h1 class="notif-detail__title">' + escapeHtml(item.title) + "</h1>" +
          senderHtml +
          highlightHtml +
          renderDetailBlocks(item) +
          bodyHtml +
          '<div class="notif-detail__actions stack-2 mt-6">' +
            ctaHtml +
            ctaSecondaryHtml +
            '<a href="27-notifications.html" class="btn btn-soft btn-block">返回消息列表</a>' +
          "</div>" +
        "</div>" +
      "</article>";

    refreshIcons();
  }

  function initSettingsPreview() {
    var root = qs("[data-notifications-preview]");
    if (!root) return;

    var list = getAllNotifications().slice(0, 3);
    root.innerHTML = list.map(function (item) {
      return (
        '<a href="27-notification-detail.html?id=' + encodeURIComponent(item.id) + '" class="settings-row settings-row--action notif-preview-row' + (item.isUnread ? " notif-preview-row--unread" : "") + '">' +
          (item.isUnread ? '<span class="notif-dot notif-dot-danger mt-1 shrink-0"></span>' : '<span class="notif-dot mt-1 shrink-0" style="visibility:hidden"></span>') +
          '<div class="settings-row__main">' +
            '<div class="settings-row__label">' + item.title + "</div>" +
            '<div class="settings-row__hint">' + item.preview + " · " + item.timeLabel + "</div>" +
          "</div>" +
          '<i data-lucide="chevron-right" class="icon muted-2"></i>' +
        "</a>"
      );
    }).join("");

    var unread = getUnreadCount();
    var badge = qs("[data-notifications-preview-badge]");
    if (badge) badge.textContent = unread ? unread + " 条未读" : "全部已读";

    refreshIcons();
  }

  window.BsLabNotifications = {
    getUnreadCount: getUnreadCount,
    markRead: markRead,
    markAllRead: markAllRead,
    getAll: getAllNotifications
  };

  function init() {
    initListPage();
    initDetailPage();
    initSettingsPreview();
    updateBellBadges();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
