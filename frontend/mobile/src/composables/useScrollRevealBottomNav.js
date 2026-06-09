import { onMounted, onUnmounted, ref } from 'vue'

const IMMERSIVE_LAYOUTS = new Set(['detail', 'lab-bench', 'upload-split', 'sim-embed'])

function findScrollRoot(shell) {
  const selectors = [
    '.lab-watch__below',
    '.lab-watch__main',
    '.watch-layout',
    '.pad-workbench__main',
    '.pad-workbench__body',
    '.pad-main',
  ]
  for (const selector of selectors) {
    const el = shell.querySelector(selector)
    if (el && el.scrollHeight > el.clientHeight + 12) return el
  }
  return null
}

export function useScrollRevealBottomNav(navRef) {
  let scrollTarget = null
  let lastScrollY = 0
  let removeListeners = null
  const revealed = ref(false)

  onMounted(() => {
    const nav = navRef.value
    if (!nav) return

    const shell = nav.closest('.pad-shell, .prototype-container')
    if (!shell) return

    const layout = shell.getAttribute('data-layout')
    if (!IMMERSIVE_LAYOUTS.has(layout)) return

    nav.classList.add('bottom-nav--immersive')

    const bind = (target) => {
      scrollTarget = target
      lastScrollY = target === window ? window.scrollY : target.scrollTop

      const onScroll = () => {
        const y = target === window ? window.scrollY : target.scrollTop
        const delta = y - lastScrollY

        if (y > 72 && delta > 2) {
          revealed.value = true
        } else if (delta < -2 && y < 48) {
          revealed.value = false
        }

        nav.classList.toggle('bottom-nav--revealed', revealed.value)
        lastScrollY = y
      }

      target.addEventListener('scroll', onScroll, { passive: true })
      return () => target.removeEventListener('scroll', onScroll)
    }

    const root = findScrollRoot(shell)
    const unbindRoot = root ? bind(root) : null
    const unbindWindow = bind(window)

    removeListeners = () => {
      unbindRoot?.()
      unbindWindow()
    }
  })

  onUnmounted(() => {
    removeListeners?.()
  })

  return { revealed }
}
