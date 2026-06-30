import { nextTick } from 'vue'

/**
 * 富文本编辑器 ref 注册表。
 * Vue 3 在 v-for 中使用 function ref 时，每次父组件更新会先以 null 调用再重新绑定；
 * 若在 null 时立刻 delete，会导致第 N 条（尤其新增后）ref 丢失、sync 失败。
 */
export function createRichEditorRegistry() {
  const editors = Object.create(null)
  const callbacks = Object.create(null)

  const getCallback = (uid) => {
    const key = String(uid)
    if (!callbacks[key]) {
      callbacks[key] = (el) => {
        if (el) editors[key] = el
      }
    }
    return callbacks[key]
  }

  const remove = (uid) => {
    const key = String(uid)
    delete editors[key]
    delete callbacks[key]
  }

  const get = (uid) => editors[String(uid)] ?? null

  /** 尽力从 Quill 同步 HTML 到父级 model，带重试 */
  const sync = async (uid, { retries = 6, delayMs = 20 } = {}) => {
    for (let attempt = 0; attempt < retries; attempt += 1) {
      await nextTick()
      const editor = get(uid)
      if (editor?.syncFromEditor?.()) return true
      if (delayMs > 0 && attempt < retries - 1) {
        await new Promise((resolve) => setTimeout(resolve, delayMs))
      }
    }
    return get(uid)?.syncFromEditor?.() ?? false
  }

  return { getCallback, remove, get, sync }
}

/** 保存前：尽力 sync，但不因 sync 失败阻断（v-model 可能已有内容） */
export async function prepareRichTextForSave(registry, uid) {
  if (!registry || !uid) return
  try {
    await registry.sync(uid)
  } catch {
    // sync 为 best-effort
  }
}
