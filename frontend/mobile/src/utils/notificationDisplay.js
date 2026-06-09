import { formatDateTime } from './feedDisplay'

const TYPE_META = {
  task: { label: '学习任务', tab: 'study', icon: 'clipboard-list', tone: 'brand' },
  bind: { label: '绑定审核', tab: 'system', icon: 'user-check', tone: 'warning' },
  grade: { label: '批阅反馈', tab: 'study', icon: 'check-circle', tone: 'success' },
  achievement: { label: '成就提醒', tab: 'study', icon: 'award', tone: 'warning' },
  social: { label: '互动消息', tab: 'social', icon: 'message-circle', tone: 'violet' },
  system: { label: '系统公告', tab: 'system', icon: 'megaphone', tone: 'slate' }
}

export const NOTICE_READ_KEY = 'bslab-home-notices-read'

export { isNoticeRead, markNoticeRead, ensureNoticeReadState, resetNoticeReadState } from './noticeRead'

export function getTypeMeta(msgTypeId) {
  const key = (msgTypeId || 'system').toLowerCase()
  return TYPE_META[key] || TYPE_META.system
}

export function parseMsgContent(content) {
  if (!content) return { title: '系统消息', preview: '', body: '', linkRoute: '' }
  try {
    const obj = JSON.parse(content)
    return {
      title: obj.title || obj.msgTitle || '系统消息',
      preview: obj.preview || obj.summary || obj.body || content,
      body: obj.body || obj.content || content,
      linkRoute: obj.linkRoute || ''
    }
  } catch {
    const lines = String(content).split('\n').filter(Boolean)
    return {
      title: lines[0] || '系统消息',
      preview: lines.slice(0, 2).join(' · ') || content,
      body: content,
      linkRoute: ''
    }
  }
}

export function mapMessageItem(raw) {
  const parsed = parseMsgContent(raw.msgContent)
  const meta = getTypeMeta(raw.msgTypeId)
  const unread = raw.readTag !== '1'
  return {
    id: raw.msgId,
    type: raw.msgTypeId || 'system',
    title: parsed.title,
    preview: parsed.preview,
    body: parsed.body,
    time: raw.sendTime,
    timeLabel: formatDateTime(raw.sendTime),
    unread,
    tab: meta.tab,
    meta,
    linkId: raw.linkId || '',
    linkRoute: parsed.linkRoute || ''
  }
}
