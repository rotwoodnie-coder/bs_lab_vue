<template>
  <div class="prototype-container pad-shell" data-layout="lab-bench">
    <BottomNav />

    <div class="pad-main lab-watch">
      <div class="lab-watch__main">
        <header class="lab-watch__topbar">
          <PageBackButton />
          <span class="lab-watch__topbar-title">{{ detail?.expName || '实验详情' }}</span>
        </header>

        <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
        <div v-else-if="error" class="px-4 py-12 text-center">
          <p class="muted-2">{{ error }}</p>
          <button type="button" class="btn btn-ghost btn-sm mt-4" @click="goBack">返回上一页</button>
        </div>

        <template v-else-if="detail">
          <div v-if="mediaSlides.length" class="lab-watch__player-wrap" data-media-carousel>
            <div class="lab-watch__media-viewport">
              <div class="lab-watch__media-track" :style="trackStyle">
                <div
                  v-for="(slide, idx) in mediaSlides"
                  :key="idx"
                  class="lab-watch__media-slide"
                  :class="{ 'is-active': idx === activeSlide }"
                  :data-media-type="slide.type"
                >
                  <video
                    v-if="slide.type === 'video'"
                    class="lab-watch__video"
                    :src="slide.url"
                    playsinline
                    controls
                    preload="metadata"
                  ></video>
                  <div v-else class="lab-watch__media-image lab-watch__media-image--photo">
                    <img :src="slide.url" :alt="slide.caption || detail.expName" loading="lazy" />
                    <span v-if="slide.caption" class="lab-watch__media-caption">{{ slide.caption }}</span>
                  </div>
                </div>
              </div>
            </div>
            <button
              v-if="mediaSlides.length > 1"
              type="button"
              class="lab-watch__media-nav lab-watch__media-nav--prev"
              aria-label="上一张"
              @click="prevSlide"
            >
              <i data-lucide="chevron-left" class="icon"></i>
            </button>
            <button
              v-if="mediaSlides.length > 1"
              type="button"
              class="lab-watch__media-nav lab-watch__media-nav--next"
              aria-label="下一张"
              @click="nextSlide"
            >
              <i data-lucide="chevron-right" class="icon"></i>
            </button>
            <div v-if="mediaSlides.length > 1" class="lab-watch__media-counter">
              {{ activeSlide + 1 }} / {{ mediaSlides.length }}
            </div>
          </div>
          <div v-else class="lab-detail__media-empty px-4 py-8 text-center muted-2">
            <i data-lucide="image-off" class="icon icon-xl"></i>
            <p class="mt-2 text-sm">暂无视频</p>
          </div>

          <div class="lab-watch__summary">
            <div class="lab-watch__meta">
              <h1 class="lab-watch__title">{{ detail.expName }}</h1>

              <div v-if="metaChips.length" class="lab-detail__chips">
                <span v-for="chip in metaChips" :key="chip.key" class="lab-detail__chip">{{ chip.label }}</span>
              </div>

              <div class="lab-watch__toolbar">
                <div class="lab-watch__uploader">
                  <UserAvatar
                    v-if="authorInitial"
                    size="sm"
                    :name="detail.createUserName"
                    role="teacher"
                  />
                  <div class="min-w-0">
                    <div v-if="detail.createUserName" class="text-sm font-bold">{{ detail.createUserName }}老师</div>
                    <div v-if="detail.createUserSchoolName" class="text-xs muted">{{ detail.createUserSchoolName }}</div>
                  </div>
                </div>
                <div class="lab-watch__actions row gap-2">
                  <button
                    type="button"
                    class="btn btn-sm lab-watch__action lab-watch__action--remix"
                    :disabled="remixLoading"
                    @click="onRemixClick"
                  ><i data-lucide="camera" class="icon"></i>{{ remixLoading ? '处理中…' : '拍同款' }}</button>
                  <router-link
                    v-if="detail.simulatorId || detail.simulatorUrl"
                    :to="simLink"
                    class="btn btn-sm lab-watch__action lab-watch__action--sim"
                  >模拟实验</router-link>
                </div>
              </div>
            </div>

            <div class="lab-watch__social" aria-label="互动">
              <button
                type="button"
                class="social-btn"
                :class="{ 'social-btn--active': liked }"
                @click="toggleLike"
              >
                <i data-lucide="thumbs-up" class="icon"></i><span>{{ displayLikeNum }}</span>
              </button>
              <button
                type="button"
                class="social-btn"
                :class="{ 'social-btn--active': starred }"
                @click="toggleCollect"
              >
                <i data-lucide="star" class="icon"></i><span>{{ starred ? '已收藏' : displayCollectNum }}</span>
              </button>
              <button type="button" class="social-btn" @click="onShare">
                <i data-lucide="share-2" class="icon"></i><span>分享</span>
              </button>
              <button type="button" class="social-btn" @click="scrollToComments">
                <i data-lucide="message-circle" class="icon"></i><span>留言</span>
              </button>
            </div>

            <section ref="commentsSection" class="lab-watch__comments" aria-label="留言">
              <div class="lab-comments__head row items-center justify-between">
                <h2 class="text-sm font-bold">留言 <span class="muted font-normal">{{ commentList.length }}</span></h2>
                <button type="button" class="text-xs text-brand font-medium">最热</button>
              </div>
              <form class="comment-compose" @submit.prevent="submitComment">
                <UserAvatar size="sm" shrink />
                <input v-model="commentText" type="text" class="comment-compose__input" placeholder="写下你的实验心得或疑问…" aria-label="发表留言">
                <button type="submit" class="btn btn-sm btn-gradient shrink-0">发送</button>
              </form>
              <ul v-if="commentList.length" class="comment-list">
                <li v-for="c in commentList" :key="c.id || c.text" class="comment-item">
                  <UserAvatar
                    size="sm"
                    shrink
                    :name="c.author"
                    :src="c.avatarUrl"
                    :role="c.isTeacher ? 'teacher' : 'student'"
                  />
                  <div class="comment-item__body">
                    <div class="comment-item__head">
                      <span class="text-xs font-bold">{{ c.author }}</span>
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
              <p v-else class="text-xs muted text-center py-4">暂无留言</p>
            </section>
          </div>
        </template>
      </div>

      <aside v-if="detail && !loading && !error" class="lab-watch__rail" aria-label="实验资料">
        <div class="lab-rail__head">
          <h2 class="lab-rail__head-title">实验资料</h2>
          <p v-if="railHeadSub" class="lab-rail__head-sub">{{ railHeadSub }}</p>
        </div>

        <div class="lab-rail__scroll">
          <section v-if="curriculumRows.length" class="lab-detail__section lab-detail__curriculum-block">
            <h2 class="lab-detail__heading">
              <i data-lucide="book-open" class="icon lab-detail__heading-icon"></i>
              对照教材
            </h2>
            <ol class="lab-detail__curriculum-list">
              <li
                v-for="(row, idx) in curriculumRows"
                :key="row.key"
                class="lab-detail__curriculum-item"
                :class="{ 'is-last': idx === curriculumRows.length - 1 }"
              >
                <span class="lab-detail__curriculum-step" aria-hidden="true">{{ idx + 1 }}</span>
                <div class="lab-detail__curriculum-body">
                  <span class="lab-detail__curriculum-label">{{ row.label }}</span>
                  <p class="lab-detail__curriculum-value">{{ row.value }}</p>
                </div>
              </li>
            </ol>
          </section>

          <section v-if="hasPrinciple" class="lab-detail__section">
            <h2 class="lab-detail__heading">实验原理</h2>
            <FormattedText :value="detail.expPrinciple" :options="FORMAT_EXP_LONG" />
          </section>

          <section v-if="materials.length" id="section-materials" class="lab-detail__section">
            <h2 class="lab-detail__heading">实验材料</h2>
            <div class="lab-detail__material-grid">
              <div
                v-for="(item, idx) in materials"
                :key="item.expMaterialId || idx"
                class="lab-detail__material-card"
              >
                <div class="lab-detail__material-thumb">
                  <img
                    v-if="materialPic(item)"
                    :src="materialPic(item)"
                    :alt="item.materialName"
                    loading="lazy"
                  />
                  <i v-else data-lucide="package" class="icon"></i>
                </div>
                <div class="lab-detail__material-body">
                  <div class="lab-detail__material-name">{{ item.materialName || '未命名材料' }}</div>
                  <div v-if="item.materialNum" class="lab-detail__material-meta">用量：{{ item.materialNum }}</div>
                  <div v-if="item.expPurpose" class="lab-detail__material-desc">
                    <FormattedText :value="item.expPurpose" :options="FORMAT_EXP_LONG" />
                  </div>
                </div>
              </div>
            </div>
          </section>

          <section v-if="steps.length" id="section-steps" class="lab-detail__section">
            <h2 class="lab-detail__heading">实验步骤</h2>
            <div class="lab-detail__card-list">
              <article
                v-for="(step, idx) in steps"
                :key="step.stepId || idx"
                class="lab-detail__card"
              >
                <h3 class="lab-detail__card-title">步骤 {{ idx + 1 }}：{{ step.stepName || '未命名步骤' }}</h3>
                <FormattedText
                  v-if="hasRichContent(step.stepComments)"
                  :value="step.stepComments"
                  :options="FORMAT_EXP_STEP"
                />
                <p v-else class="text-sm muted-2">暂无步骤说明</p>
              </article>
            </div>
          </section>

          <section v-if="results.length" id="section-results" class="lab-detail__section">
            <h2 class="lab-detail__heading">实验结果</h2>
            <div class="lab-detail__card-list">
              <article v-for="(item, idx) in results" :key="item.resultId || idx" class="lab-detail__card">
                <h3 class="lab-detail__card-title">结果 {{ idx + 1 }}：{{ item.resultName || '未命名结果' }}</h3>
                <FormattedText
                  v-if="hasRichContent(item.resultComments)"
                  :value="item.resultComments"
                  :options="FORMAT_EXP_STEP"
                />
              </article>
            </div>
          </section>

          <section v-if="hasCaution || hasDanger" id="section-safety" class="lab-detail__section">
            <h2 class="lab-detail__heading">安全提示</h2>
            <div v-if="hasCaution" class="lab-detail__safety lab-detail__safety--caution">
              <p class="lab-detail__safety-label">注意事项</p>
              <FormattedText :value="detail.expCaution" :options="FORMAT_EXP_LONG" />
            </div>
            <div v-if="hasDanger" class="lab-detail__safety lab-detail__safety--danger">
              <p class="lab-detail__safety-label">危险提示</p>
              <FormattedText :value="detail.expDanger" :options="FORMAT_EXP_LONG" />
            </div>
          </section>

          <section v-if="references.length" id="section-references" class="lab-detail__section">
            <h2 class="lab-detail__heading">参考资料</h2>
            <div class="lab-detail__card-list">
              <article v-for="(item, idx) in references" :key="item.referenceId || idx" class="lab-detail__card">
                <h3 class="lab-detail__card-title">{{ item.referenceName || '参考资料' }}</h3>
                <p v-if="item.referenceSource" class="text-xs muted lab-detail__card-sub">出处：{{ item.referenceSource }}</p>
                <FormattedText
                  v-if="hasRichContent(item.referenceComments)"
                  :value="item.referenceComments"
                  :options="FORMAT_EXP_LONG"
                />
              </article>
            </div>
          </section>

          <section v-if="scientists.length" id="section-scientists" class="lab-detail__section lab-detail__section--last">
            <h2 class="lab-detail__heading">科学家故事</h2>
            <div class="lab-detail__card-list">
              <article v-for="(item, idx) in scientists" :key="item.scientistId || idx" class="lab-detail__card">
                <h3 class="lab-detail__card-title">{{ item.storyName || '科学家故事' }}</h3>
                <p v-if="item.scientistName" class="text-xs muted lab-detail__card-sub">科学家：{{ item.scientistName }}</p>
                <FormattedText
                  v-if="hasRichContent(item.storyComments)"
                  :value="item.storyComments"
                  :options="FORMAT_EXP_LONG"
                />
              </article>
            </div>
          </section>

          <section
            v-if="!hasContentSections"
            class="lab-detail__section lab-detail__empty text-center muted-2 text-sm"
          >
            暂无更多实验内容
          </section>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import {
  fetchExpDetail,
  fetchExpVideos,
  fetchExpSteps,
  fetchExpMaterials,
  fetchExpResults,
  fetchExpReferences,
  fetchExpScientists
} from '@/api/experiment'
import { startRemix } from '@/api/remix'
import { fetchSocialSummary, fetchComments, createComment, toggleReaction, toggleCommentLike as apiToggleCommentLike } from '@/api/social'
import { sharePage } from '@/utils/share'
import { ensureSocialOk, parseSocialSummary, parseCommentReaction } from '@/utils/socialFeedback'
import FormattedText from '@/components/FormattedText.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { FORMAT_EXP_LONG, FORMAT_EXP_STEP, hasRichContent } from '@/utils/richText'
import { metaChipItems, curriculumRows as buildCurriculumRows } from '@/utils/expDisplay'
import { resolveMediaUrl } from '@/utils/fileUrl'
import { useLucideIcons } from '@/composables/useLucideIcons'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const detail = ref(null)
const videos = ref([])
const steps = ref([])
const materials = ref([])
const results = ref([])
const references = ref([])
const scientists = ref([])
const activeSlide = ref(0)
const remixLoading = ref(false)

const liked = ref(false)
const starred = ref(false)
const commentText = ref('')
const commentsSection = ref(null)
const commentList = ref([])
const socialLikeNum = ref(0)
const socialCollectNum = ref(0)
const socialLoaded = ref(false)
const likeBusy = ref(false)
const collectBusy = ref(false)

const displayLikeNum = computed(() => {
  if (socialLoaded.value) return socialLikeNum.value
  return Number(detail.value?.likeNum || 0)
})
const displayCollectNum = computed(() => socialCollectNum.value || detail.value?.collectionNum || 0)

function mapComment(c) {
  return {
    id: c.id,
    initial: c.userInitial || (c.userName || '用').charAt(0),
    author: c.userName || '用户',
    text: c.content,
    time: c.timeLabel || '',
    isTeacher: c.userRoleTag === 'teacher',
    avatarUrl: c.userAvatarUrl || '',
    likes: c.likeCount || 0,
    liked: !!c.liked || !!c.isLiked
  }
}

async function loadSocial() {
  if (!expId.value) return
  try {
    const [summaryRes, commentsRes] = await Promise.all([
      fetchSocialSummary('exp_msg', expId.value),
      fetchComments('exp_msg', expId.value)
    ])
    if (summaryRes?.code === 200 && summaryRes.data) {
      const summary = parseSocialSummary(summaryRes.data)
      liked.value = summary.liked
      starred.value = summary.collected
      socialLikeNum.value = summary.likeCount
      socialCollectNum.value = summary.collectCount
      socialLoaded.value = true
    }
    if (commentsRes?.code === 200) {
      commentList.value = (commentsRes.data || []).map(mapComment)
    }
  } catch (e) {
    console.warn('加载互动数据失败', e)
  }
}

async function toggleLike() {
  if (!expId.value || likeBusy.value) return
  likeBusy.value = true
  try {
    const res = await toggleReaction({ targetType: 'exp_msg', targetId: expId.value, reactionType: 'like' })
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
  if (!expId.value || collectBusy.value) return
  collectBusy.value = true
  try {
    const res = await toggleReaction({ targetType: 'exp_msg', targetId: expId.value, reactionType: 'collect' })
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
    title: detail.value?.expName || detail.value?.title,
    text: detail.value?.expBrief || detail.value?.brief
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
    console.warn('留言点赞失败', e)
    alert('点赞失败，请稍后重试')
  }
}

async function submitComment() {
  const text = commentText.value.trim()
  if (!text || !expId.value) return
  try {
    const res = await createComment({ targetType: 'exp_msg', targetId: expId.value, content: text })
    if (!ensureSocialOk(res, '发表失败')) return
    commentList.value.unshift(mapComment(res.data))
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
  if (remixLoading.value || !expId.value) return
  remixLoading.value = true
  try {
    const res = await startRemix({ expId: expId.value })
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

const expId = computed(() => route.params.id)

const authorInitial = computed(() => {
  const name = detail.value?.createUserName
  return name ? name.charAt(0) : ''
})

const metaChips = computed(() => metaChipItems(detail.value))
const curriculumRows = computed(() => buildCurriculumRows(detail.value))

const simLink = computed(() => {
  const id = detail.value?.simulatorId
  return id ? `/sim/${id}` : '/experiments'
})

const hasPrinciple = computed(() => hasRichContent(detail.value?.expPrinciple))
const hasCaution = computed(() => hasRichContent(detail.value?.expCaution))
const hasDanger = computed(() => hasRichContent(detail.value?.expDanger))

const hasContentSections = computed(() =>
  curriculumRows.value.length > 0 ||
  hasPrinciple.value ||
  materials.value.length > 0 ||
  steps.value.length > 0 ||
  results.value.length > 0 ||
  hasCaution.value ||
  hasDanger.value ||
  references.value.length > 0 ||
  scientists.value.length > 0
)

const railHeadSub = computed(() => {
  const parts = []
  if (steps.value.length) parts.push(`${steps.value.length} 步`)
  if (materials.value.length) parts.push(`${materials.value.length} 项材料`)
  return parts.join(' · ')
})

const mediaSlides = computed(() => {
  const slides = []
  videos.value.forEach((v) => {
    const url = resolveMediaUrl(v, 'videoUrl')
    if (url) slides.push({ type: 'video', url })
  })
  return slides
})

const trackStyle = computed(() => ({
  transform: `translateX(-${activeSlide.value * 100}%)`
}))

function materialPic(item) {
  return resolveMediaUrl(item, 'mainPicUrl')
}

const { initIcons } = useLucideIcons()

function formatDate(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleDateString('zh-CN')
}

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/home')
}

function prevSlide() {
  activeSlide.value = (activeSlide.value - 1 + mediaSlides.value.length) % mediaSlides.value.length
  initIcons()
}

function nextSlide() {
  activeSlide.value = (activeSlide.value + 1) % mediaSlides.value.length
  initIcons()
}

function pickList(res) {
  if (!res || res.code !== 200) return []
  return Array.isArray(res.data) ? res.data : []
}

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    const [detailRes, videoRes, stepRes, matRes, resultRes, refRes, sciRes] = await Promise.all([
      fetchExpDetail(expId.value),
      fetchExpVideos(expId.value),
      fetchExpSteps(expId.value),
      fetchExpMaterials(expId.value),
      fetchExpResults(expId.value),
      fetchExpReferences(expId.value),
      fetchExpScientists(expId.value)
    ])

    if (!detailRes || detailRes.code !== 200 || !detailRes.data) {
      error.value = detailRes?.message || '未找到该实验'
      return
    }

    detail.value = detailRes.data
    videos.value = pickList(videoRes)
    steps.value = pickList(stepRes)
    materials.value = pickList(matRes)
    results.value = pickList(resultRes)
    references.value = pickList(refRes)
    scientists.value = pickList(sciRes)
    document.title = `${detail.value.expName} · 宝山小实验社区`
    await loadSocial()
  } catch (e) {
    console.warn('加载实验详情失败', e)
    error.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
    initIcons()
  }
}

onMounted(loadDetail)
</script>
