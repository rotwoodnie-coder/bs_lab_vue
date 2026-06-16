import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'

/**
 * 元素进入视口后触发（P2：延迟加载 feed 视频）。
 */
export function useInViewport(options = {}) {
  const targetRef = ref(null)
  const inView = ref(false)
  let observer = null

  function disconnect() {
    observer?.disconnect()
    observer = null
  }

  onMounted(() => {
    if (typeof IntersectionObserver === 'undefined') {
      inView.value = true
      return
    }
    nextTick(() => {
      if (!targetRef.value) {
        inView.value = true
        return
      }
      observer = new IntersectionObserver(
        ([entry]) => {
          if (entry?.isIntersecting) {
            inView.value = true
            disconnect()
          }
        },
        {
          root: null,
          rootMargin: options.rootMargin || '120px 0px',
          threshold: options.threshold ?? 0.01
        }
      )
      observer.observe(targetRef.value)
    })
  })

  onBeforeUnmount(disconnect)

  return { targetRef, inView }
}
