<template>
  <MobilePageShell data-layout="lab-bench">

    <div class="pad-main lab-watch">
      <div class="lab-watch__main">
        <header class="lab-watch__topbar">
          <PageBackButton fallback="/works" />
          <span class="lab-watch__topbar-title">{{ work.title || '作品详情' }}</span>
        </header>

        <WorkMediaViewer :files="work.files || []" />

        <div class="lab-watch__summary">
          <div class="lab-watch__meta">
            <div class="lab-watch__meta-head">
              <h1 class="lab-watch__title flex-1 min-w-0">{{ work.title }}</h1>
              <span v-if="gradeBadge" class="badge shrink-0" :class="gradeBadge.class">{{ gradeBadge.label }}</span>
            </div>
            <p v-if="work.time" class="lab-watch__meta-time">{{ work.time }}</p>

            <div class="lab-watch__toolbar">
              <div class="lab-watch__uploader">
                <UserAvatar
                  size="sm"
                  role="student"
                  :name="work.author"
                  :src="work.authorAvatarUrl"
                />
                <div class="min-w-0">
                  <div v-if="authorLine" class="text-sm font-bold">{{ authorLine }}</div>
                </div>
              </div>
              <div class="lab-watch__actions row gap-2">
                <button
                  type="button"
                  class="btn btn-sm lab-watch__action lab-watch__action--remix"
                  :disabled="remixLoading"
                  @click="onRemixClick"
                >{{ remixLoading ? '处理中…' : '拍同款' }}</button>
                <button
                  v-if="isAuthor"
                  type="button"
                  class="btn btn-sm btn-outline lab-watch__action lab-watch__action--edit"
                  @click="onEditClick"
                >编辑</button>
              </div>
            </div>

            <p v-if="work.desc" class="text-sm leading-snug mt-3">{{ work.desc }}</p>
          </div>

          <div class="lab-watch__social" aria-label="互动">
            <button
              type="button"
              class="social-btn"
              :class="{ 'social-btn--active': liked }"
              @click="toggleLike"
            >
              <i data-lucide="thumbs-up" class="icon"></i>
              <span>{{ likeCount }}</span>
            </button>
            <button
              type="button"
              class="social-btn"
              :class="{ 'social-btn--active': starred }"
              @click="toggleCollect"
            >
              <i data-lucide="star" class="icon"></i>
              <span>{{ starred ? '已收藏' : displayCollectNum }}</span>
            </button>
            <button type="button" class="social-btn" @click="onShare">
              <i data-lucide="share-2" class="icon"></i>
              <span>分享</span>
            </button>
            <button type="button" class="social-btn" @click="scrollToComments">
              <i data-lucide="message-circle" class="icon"></i>
              <span>{{ commentCount }}</span>
            </button>
          </div>

          <div v-if="work.teacherReview" class="lab-watch__teacher-review tint-blue">
            <div class="flex items-start gap-2">
              <span class="text-lg shrink-0">👩‍🏫</span>
              <div>
                <p class="text-sm font-bold text-brand">{{ work.teacherReview.name }}评语：</p>
                <p class="text-sm mt-1">{{ work.teacherReview.text }}</p>
              </div>
            </div>
          </div>

          <section ref="commentsSection" class="lab-watch__comments" aria-label="评论">
            <div class="lab-comments__head row items-center justify-between">
              <h2 class="text-sm font-bold">评论 <span class="muted font-normal">{{ commentCount }}</span></h2>
              <button type="button" class="text-xs font-medium" :class="commentSortOrder === 'hot' ? 'text-brand' : 'muted'" @click="toggleCommentSort">
                {{ commentSortOrder === 'hot' ? '最热' : '最新' }}
              </button>
            </div>

            <form class="comment-compose" @submit.prevent="submitComment">
              <UserAvatar size="sm" shrink />
              <input v-model="commentText" type="text" class="comment-compose__input" placeholder="写下你的看法或提问…" aria-label="发表评论">
              <button type="submit" class="btn btn-sm btn-gradient shrink-0">发送</button>
            </form>

            <ul v-if="displayedComments.length" class="comment-list">
              <li v-for="c in displayedComments" :key="c.id || c.text" class="comment-item">
                <UserAvatar
                  size="sm"
                  shrink
                  :name="c.author"
                  :src="c.avatarUrl"
                  :role="c.isTeacher ? 'teacher' : 'student'"
                />
                <div class="comment-item__body">
                  <div class="comment-item__head">
                    <span class="text-xs font-bold" :class="{ 'text-brand': c.isAuthor }">{{ c.author }}</span>
                    <span v-if="c.isAuthor" class="pill text-xs" style="padding:1px 6px">作者</span>
                    <span v-if="c.isTeacher" class="pill text-xs" style="padding:1px 6px;margin-left:4px">老师</span>
                    <span class="text-xs muted">{{ c.time }}</span>
                  </div>
                  <p class="comment-item__text">{{ c.text }}</p>
                  <div class="comment-item__actions">
                    <button
                      type="button"
                      class="comment-action"
                      :class="{ 'comment-action--active': c.liked }"
                      @click="toggleCommentLike(c)"
                    >
                      <i data-lucide="thumbs-up" class="icon"></i> {{ c.likes }}
                    </button>
                    <button type="button" class="comment-action">回复</button>
                  </div>
                </div>
              </li>
            </ul>
            <p v-else class="text-xs muted text-center py-4">暂无评论</p>
          </section>
        </div>
      </div>

      <ExpDetailRail
        :detail="linkedExp"
        :exp-id="work.sourceExpId"
        :steps="steps"
        :materials="materials"
        :results="results"
        :references="references"
        :scientists="scientists"
        :recommendations="showRecommendations ? recommendations : []"
        :head-sub="railHeadSub"
      />
    </div>
  </MobilePageShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import WorkMediaViewer from '@/components/works/WorkMediaViewer.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import ExpDetailRail from '@/components/content/ExpDetailRail.vue'
import { fetchWorkDetail, fetchWorkRecommendations } from '@/api/work'
import { startRemix } from '@/api/remix'
import {
  fetchExpDetail,
  fetchExpSteps,
  fetchExpMaterials,
  fetchExpResults,
  fetchExpReferences,
  fetchExpScientists
} from '@/api/experiment'
import { fetchSocialSummary, fetchComments, createComment, toggleReaction, toggleCommentLike as apiToggleCommentLike } from '@/api/social'
import { sharePage } from '@/utils/share'
import { sortComments } from '@/utils/commentSort'
import { ensureSocialOk, parseSocialSummary, parseCommentReaction } from '@/utils/socialFeedback'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { getUserInfo } from '@/utils/authStorage'
import { authorLineParts } from '@/utils/feedDisplay'

const route = useRoute()
const router = useRouter()
const work = ref({})
const remixLoading = ref(false)

const liked = ref(false)
const starred = ref(false)
const commentText = ref('')
const comments = ref([])
const commentSortOrder = ref('hot')
const commentsSection = ref(null)
const socialLikeNum = ref(0)
const socialCommentNum = ref(0)
const socialCollectNum = ref(0)
const socialLoaded = ref(false)
const likeBusy = ref(false)
const collectBusy = ref(false)

const linkedExp = ref(null)
const steps = ref([])
const materials = ref([])
const results = ref([])
const references = ref([])
const scientists = ref([])
const recommendations = ref([])

const likeCount = computed(() => {
  if (socialLoaded.value) return socialLikeNum.value
  return Number(work.value?.likes || 0)
})
const displayCollectNum = computed(() => socialCollectNum.value || 0)
const commentCount = computed(() => socialCommentNum.value || work.value?.comments || comments.value.length)

const displayedComments = computed(() => sortComments(comments.value, commentSortOrder.value))

function toggleCommentSort() {
  commentSortOrder.value = commentSortOrder.value === 'hot' ? 'latest' : 'hot'
}

const authorLine = computed(() => {
  const parts = authorLineParts({
    type: 'work',
    author: work.value?.author,
    classLabel: work.value?.className,
    authorSchool: work.value?.schoolName,
    authorRole: work.value?.authorRole || 'student'
  })
  return parts.join(' · ')
})

const showRecommendations = computed(() => !work.value?.sourceExpId)

const railHeadSub = computed(() => {
  if (showRecommendations.value) {
    return recommendations.value.length ? `${recommendations.value.length} 个推荐` : ''
  }
  const parts = []
  if (steps.value.length) parts.push(`${steps.value.length} 步`)
  if (materials.value.length) parts.push(`${materials.value.length} 项材料`)
  return parts.join(' · ')
})

const gradeBadge = computed(() => {
  const grade = work.value?.grade
  if (grade) {
    const map = {
      excellent: { label: '优秀', class: 'badge-success' },
      good: { label: '良好', class: 'badge-success' },
      pass: { label: '合格', class: 'badge-info' },
      fail: { label: '不合格', class: 'badge-danger' }
    }
    return map[grade] || { label: grade, class: 'badge-info' }
  }
  if (work.value?.reviewStatusLabel) {
    const cls = work.value.reviewStatus === 'reviewed' || work.value.reviewStatus === 'approved'
      ? 'badge-success'
      : work.value.reviewStatus === 'rejected'
        ? 'badge-danger'
        : 'badge-warning'
    return { label: work.value.reviewStatusLabel, class: cls }
  }
  return null
})

function mapComment(c) {
  return {
    id: c.id,
    initial: c.userInitial || (c.userName || '用').charAt(0),
    author: c.userName || '用户',
    text: c.content,
    time: c.timeLabel || '',
    isAuthor: !!c.mine,
    isTeacher: c.userRoleTag === 'teacher',
    avatarUrl: c.userAvatarUrl || '',
    likes: c.likeCount || 0,
    liked: !!c.liked || !!c.isLiked
  }
}

async function loadLinkedExp() {
  const expId = work.value?.sourceExpId
  if (!expId) return
  try {
    const [detailRes, stepRes, matRes, resultRes, refRes, sciRes] = await Promise.all([
      fetchExpDetail(expId),
      fetchExpSteps(expId),
      fetchExpMaterials(expId),
      fetchExpResults(expId),
      fetchExpReferences(expId),
      fetchExpScientists(expId)
    ])
    if (detailRes?.code === 200 && detailRes.data) {
      linkedExp.value = detailRes.data
    }
    if (stepRes?.code === 200) steps.value = stepRes.data || []
    if (matRes?.code === 200) materials.value = matRes.data || []
    if (resultRes?.code === 200) results.value = resultRes.data || []
    if (refRes?.code === 200) references.value = refRes.data || []
    if (sciRes?.code === 200) scientists.value = sciRes.data || []
  } catch (e) {
    console.warn('加载关联实验详情失败', e)
  }
}

async function loadRecommendations() {
  if (work.value?.sourceExpId) return
  const workId = route.params.id
  if (!workId) return
  try {
    const res = await fetchWorkRecommendations(workId)
    if (res?.code === 200) {
      recommendations.value = res.data || []
    }
  } catch (e) {
    console.warn('加载推荐实验失败', e)
  }
}

async function loadSocial() {
  const workId = route.params.id
  if (!workId) return
  try {
    const [summaryRes, commentsRes] = await Promise.all([
      fetchSocialSummary('work', workId),
      fetchComments('work', workId)
    ])
    if (summaryRes?.code === 200 && summaryRes.data) {
      const summary = parseSocialSummary(summaryRes.data)
      liked.value = summary.liked
      starred.value = summary.collected
      socialLikeNum.value = summary.likeCount
      socialCollectNum.value = summary.collectCount
      socialCommentNum.value = summary.commentCount
      socialLoaded.value = true
    }
    if (commentsRes?.code === 200) {
      comments.value = (commentsRes.data || []).map(mapComment)
    }
  } catch (e) {
    console.warn('加载互动数据失败', e)
  }
}

async function toggleLike() {
  const workId = route.params.id
  if (!workId || likeBusy.value) return
  likeBusy.value = true
  try {
    const res = await toggleReaction({ targetType: 'work', targetId: workId, reactionType: 'like' })
    if (!ensureSocialOk(res, '点赞失败')) return
    const summary = parseSocialSummary(res.data)
    liked.value = summary.liked
    socialLikeNum.value = summary.likeCount
    socialLoaded.value = true
  } catch (e) {
    console.warn('点赞失败', e)
    alert('点赞失败，请稍后重试')
  } finally {
    likeBusy.value = false
  }
}

async function toggleCollect() {
  const workId = route.params.id
  if (!workId || collectBusy.value) return
  collectBusy.value = true
  try {
    const res = await toggleReaction({ targetType: 'work', targetId: workId, reactionType: 'collect' })
    if (!ensureSocialOk(res, '收藏失败')) return
    const summary = parseSocialSummary(res.data)
    starred.value = summary.collected
    socialCollectNum.value = summary.collectCount
    socialLoaded.value = true
  } catch (e) {
    console.warn('收藏失败', e)
    alert('收藏失败，请稍后重试')
  } finally {
    collectBusy.value = false
  }
}

async function onShare() {
  await sharePage({
    title: work.value?.title,
    text: work.value?.desc
  })
}

async function toggleCommentLike(comment) {
  if (!comment?.id) return
  try {
    const res = await apiToggleCommentLike(comment.id)
    if (!ensureSocialOk(res, '点赞失败')) return
    const reaction = parseCommentReaction(res.data)
    comment.likes = reaction.likeCount
    comment.liked = reaction.liked
  } catch (e) {
    console.warn('评论点赞失败', e)
    alert('点赞失败，请稍后重试')
  }
}

async function submitComment() {
  const text = commentText.value.trim()
  const workId = route.params.id
  if (!text || !workId) return
  try {
    const res = await createComment({ targetType: 'work', targetId: workId, content: text })
    if (!ensureSocialOk(res, '发表失败')) return
    comments.value.unshift(mapComment(res.data))
    socialCommentNum.value += 1
    commentText.value = ''
  } catch (e) {
    console.warn('发表评论失败', e)
    alert('发表失败，请稍后重试')
  }
}

function scrollToComments() {
  commentsSection.value?.scrollIntoView({ behavior: 'smooth' })
}

async function onRemixClick() {
  if (remixLoading.value) return
  remixLoading.value = true
  try {
    const payload = {
      expId: work.value?.sourceExpId || undefined,
      workId: route.params.id
    }
    const res = await startRemix(payload)
    if ((res?.code === 200 || res?.code === 409) && res.data?.id) {
      if (res.code === 409) {
        alert(res.message || '该实验已有进行中的拍同款任务')
      }
      router.push(`/tasks/${res.data.id}`)
      return
    }
    alert(res?.message || '无法发起拍同款')
  } catch (e) {
    console.warn('发起拍同款失败', e)
    alert('发起拍同款失败，请稍后重试')
  } finally {
    remixLoading.value = false
    initIcons()
  }
}

const { initIcons } = useLucideIcons()

const isAuthor = computed(() => {
  const user = getUserInfo()
  return user?.userId && work.value?.authorUserId && user.userId === work.value.authorUserId
})

function onEditClick() {
  const workId = route.params.id
  if (workId) {
    router.push(`/upload?edit=${workId}`)
  }
}

onMounted(async () => {
  try {
    const res = await fetchWorkDetail(route.params.id)
    if (res?.code === 200 && res.data) {
      work.value = res.data || {}
    }
  } catch { /* ignore */ }
  await Promise.all([loadLinkedExp(), loadRecommendations(), loadSocial()])
  initIcons()
})
</script>
