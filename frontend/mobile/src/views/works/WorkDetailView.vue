<template>

  <div class="prototype-container pad-shell safe-top" data-layout="detail">

    <div class="topbar page-topbar">

      <PageBackButton fallback="/works" />

      <h1 class="topbar-title">{{ work.title }}</h1>

    </div>



    <div class="watch-layout pb-28">

      <div class="watch-main">

        <div class="card-media media-wide card-media-grad-amber-rose">

          <i data-lucide="image" class="icon-xl"></i>

          <div class="pill anim-pulse">

            <i data-lucide="play" class="icon-lg"></i>

          </div>

        </div>



        <div class="px-4 py-4 stack-2">

          <div class="flex items-center justify-between">

            <h2 class="text-lg font-bold">{{ work.title }}</h2>

            <span class="badge badge-success">{{ work.grade }}</span>

          </div>

          <div class="flex flex-wrap items-center gap-4 text-sm muted">

            <span>{{ work.author }}</span>

            <span>{{ work.className }}</span>

            <span>{{ work.time }}</span>

          </div>

          <p class="text-sm leading-tight">{{ work.desc }}</p>

          <div class="lab-watch__actions row gap-2">

            <button
              type="button"
              class="btn btn-sm lab-watch__action lab-watch__action--remix"
              :disabled="remixLoading"
              @click="onRemixClick"
            >{{ remixLoading ? '处理中…' : '拍同款' }}</button>

          </div>

        </div>



        <div class="px-4">

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

            <button type="button" class="social-btn social-btn--disabled" disabled title="暂不支持">

              <i data-lucide="thumbs-down" class="icon"></i>

              <span>{{ dislikeCount }}</span>

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

        </div>



        <div v-if="work.teacherReview" class="mx-4 p-3 tint-blue rounded">

          <div class="flex items-start gap-2">

            <span class="text-lg shrink-0">👩‍🏫</span>

            <div>

              <p class="text-sm font-bold text-brand">{{ work.teacherReview.name }}评语：</p>

              <p class="text-sm mt-1">{{ work.teacherReview.text }}</p>

            </div>

          </div>

        </div>



        <section ref="commentsSection" class="lab-watch__comments px-4 pb-4" aria-label="评论">

          <div class="lab-comments__head row items-center justify-between">

            <h2 class="text-sm font-bold">评论 <span class="muted font-normal">{{ commentCount }}</span></h2>

            <button type="button" class="text-xs text-brand font-medium">最热</button>

          </div>

          <form class="comment-compose" @submit.prevent="submitComment">

            <span class="avatar avatar-sm avatar-grad-warm shrink-0">{{ userInitial }}</span>

            <input v-model="commentText" type="text" class="comment-compose__input" placeholder="写下你的看法或提问…" aria-label="发表评论">

            <button type="submit" class="btn btn-sm btn-gradient shrink-0">发送</button>

          </form>

          <ul v-if="comments.length" class="comment-list">
            <li v-for="c in comments" :key="c.id || c.text" class="comment-item">

              <span class="avatar avatar-sm shrink-0" :class="c.avatarClass">{{ c.initial }}</span>

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



    <BottomNav />

  </div>

</template>



<script setup>

import { ref, computed, onMounted, nextTick } from 'vue'

import { useRoute, useRouter } from 'vue-router'

import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'

import { fetchWorkDetail } from '@/api/work'
import { fetchProfile } from '@/api/profile'
import { startRemix } from '@/api/remix'
import { fetchSocialSummary, fetchComments, createComment, toggleReaction, toggleCommentLike as apiToggleCommentLike } from '@/api/social'
import { sharePage } from '@/utils/share'
import { ensureSocialOk, parseSocialSummary, parseCommentReaction } from '@/utils/socialFeedback'

const route = useRoute()
const router = useRouter()
const work = ref({})
const remixLoading = ref(false)



const liked = ref(false)
const starred = ref(false)
const dislikeCount = ref(0)
const commentText = ref('')
const comments = ref([])
const commentsSection = ref(null)
const userInitial = ref('我')
const socialLikeNum = ref(0)
const socialCommentNum = ref(0)
const socialCollectNum = ref(0)
const socialLoaded = ref(false)
const likeBusy = ref(false)
const collectBusy = ref(false)

const likeCount = computed(() => {
  if (socialLoaded.value) return socialLikeNum.value
  return Number(work.value?.likes || 0)
})
const displayCollectNum = computed(() => socialCollectNum.value || 0)
const commentCount = computed(() => socialCommentNum.value || work.value?.comments || comments.value.length)

function mapComment(c) {
  return {
    id: c.id,
    initial: c.userInitial || (c.userName || '用').charAt(0),
    author: c.userName || '用户',
    text: c.content,
    time: c.timeLabel || '',
    isAuthor: !!c.mine,
    isTeacher: c.userRoleTag === 'teacher',
    avatarClass: c.userRoleTag === 'teacher' ? 'avatar-grad-ocean' : 'avatar-grad-warm',
    likes: c.likeCount || 0,
    liked: !!c.liked || !!c.isLiked
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

function initIcons() {

  nextTick(() => {

    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})

  })

}



onMounted(async () => {
  try {
    const res = await fetchWorkDetail(route.params.id)
    if (res?.code === 200 && res.data) {
      work.value = res.data || {}
    }
  } catch { /* ignore */ }
  try {
    const pRes = await fetchProfile()
    if (pRes?.code === 200 && pRes.data) {
      const name = pRes.data.userNickName || pRes.data.userName || '我'
      userInitial.value = name.charAt(0) || '我'
    }
  } catch { /* ignore */ }
  await loadSocial()
  initIcons()
})

</script>

