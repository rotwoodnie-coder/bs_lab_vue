/**
 * index-pad.html — 平板横屏平铺预览（1024×768 缩放 + 角色筛选）
 * iframe 内注入 in-iframe-pad，强制走 layout-pad 横屏样式（不依赖手机竖屏）
 */
(function () {
  const PAD_W = 1024;
  const PAD_H = 768;

  function markPadIframe(iframe) {
    try {
      var doc = iframe.contentDocument;
      if (!doc || !doc.documentElement) return;
      doc.documentElement.classList.add('in-iframe-pad');
      var win = iframe.contentWindow;
      if (win) {
        win.scrollTo(0, 0);
        if (doc.body) doc.body.scrollTop = 0;
      }
    } catch (e) { /* 跨域时忽略 */ }
  }

  function initPadIframes() {
    document.querySelectorAll('.tile-pad iframe').forEach(function (iframe) {
      iframe.setAttribute('width', String(PAD_W));
      iframe.setAttribute('height', String(PAD_H));
      if (iframe.dataset.padPreviewBound === '1') return;
      iframe.dataset.padPreviewBound = '1';
      iframe.addEventListener('load', function () {
        markPadIframe(iframe);
        scalePadTiles();
      });
      if (iframe.contentDocument && iframe.contentDocument.readyState === 'complete') {
        markPadIframe(iframe);
      }
    });
  }

  function scalePadTiles() {
    document.querySelectorAll('.tile-pad').forEach(function (pad) {
      var iframe = pad.querySelector('iframe');
      if (!iframe) return;
      var scale = pad.clientWidth / PAD_W;
      iframe.style.transform = 'scale(' + scale + ')';
      pad.style.height = Math.ceil(PAD_H * scale) + 'px';
    });
  }

  window.filterPages = function (filter) {
    var tiles = document.querySelectorAll('.tile');
    var buttons = document.querySelectorAll('.filter-btn');
    var count = 0;
    buttons.forEach(function (b) { b.classList.remove('active'); });
    var activeBtn = document.querySelector('.filter-btn[data-filter="' + filter + '"]');
    if (activeBtn) activeBtn.classList.add('active');
    tiles.forEach(function (tile) {
      var tags = tile.dataset.tags || '';
      if (filter === 'all' || tags.split(' ').includes(filter)) {
        tile.classList.remove('hidden');
        count++;
      } else {
        tile.classList.add('hidden');
      }
    });
    var counter = document.getElementById('counter');
    if (counter) counter.innerHTML = '显示 <strong>' + count + '</strong> / 26 个页面';
    requestAnimationFrame(scalePadTiles);
  };

  var rt;
  window.addEventListener('load', function () {
    initPadIframes();
    scalePadTiles();
  });
  window.addEventListener('resize', function () {
    clearTimeout(rt);
    rt = setTimeout(function () {
      scalePadTiles();
    }, 120);
  });
})();
