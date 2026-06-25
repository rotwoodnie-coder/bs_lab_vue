<template>
  <aside class="lab-watch__rail" aria-label="实验资料">
    <div class="lab-rail__head">
      <h2 class="lab-rail__head-title">{{ headTitle }}</h2>
      <p v-if="headSub" class="lab-rail__head-sub">{{ headSub }}</p>
    </div>

    <div class="lab-rail__scroll">
      <template v-if="recommendations.length">
        <section class="lab-detail__section">
          <h2 class="lab-detail__heading">
            <span class="lab-detail__heading-label">
              <i data-lucide="sparkles" class="icon lab-detail__heading-icon"></i>
              推荐实验
            </span>
          </h2>
          <p class="text-xs muted mb-3">该作品未关联实验，为你推荐以下实验</p>
          <div class="lab-rail__feed-list">
            <FeedCard
              v-for="(item, idx) in recommendations"
              :key="item.id"
              :item="item"
              :index="idx"
              compact
            />
          </div>
        </section>
      </template>

      <template v-else-if="detail">
        <div v-if="expId" class="lab-rail__link-row">
          <router-link :to="`/exp/${expId}`" class="text-xs text-brand">查看完整实验</router-link>
        </div>

        <section v-if="detail.expName" class="lab-detail__section">
          <h2 class="lab-detail__heading">
            <span class="lab-detail__heading-label">
              <i data-lucide="flask-conical" class="icon lab-detail__heading-icon"></i>
              {{ detail.expName }}
            </span>
          </h2>
          <div v-if="metaChips.length" class="lab-detail__chips">
            <span v-for="chip in metaChips" :key="chip.key" class="lab-detail__chip">{{ chip.label }}</span>
          </div>
        </section>

        <section v-if="hasPrinciple" class="lab-detail__section lab-detail__section--collapsible">
          <h2 class="lab-detail__heading lab-detail__heading--clickable" @click="toggleSection('principle')">
            <span class="lab-detail__heading-label">
              <i data-lucide="lightbulb" class="icon lab-detail__heading-icon"></i>
              实验原理
            </span>
            <i
              :data-lucide="collapsed.principle ? 'chevron-down' : 'chevron-up'"
              class="icon lab-detail__collapse-icon"
            ></i>
          </h2>
          <transition name="collapse">
            <div v-show="!collapsed.principle">
              <FormattedText :value="detail.expPrinciple" :options="FORMAT_EXP_LONG" />
            </div>
          </transition>
        </section>

        <section v-if="materials.length" class="lab-detail__section lab-detail__section--collapsible">
          <h2 class="lab-detail__heading lab-detail__heading--clickable" @click="toggleSection('materials')">
            <span class="lab-detail__heading-label">
              <i data-lucide="package" class="icon lab-detail__heading-icon"></i>
              实验材料
            </span>
            <i
              :data-lucide="collapsed.materials ? 'chevron-down' : 'chevron-up'"
              class="icon lab-detail__collapse-icon"
            ></i>
          </h2>
          <transition name="collapse">
            <div v-show="!collapsed.materials" class="lab-detail__material-grid">
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
              </div>
            </div>
            </div>
            </transition>
        </section>

        <section v-if="steps.length" class="lab-detail__section lab-detail__section--collapsible">
          <h2 class="lab-detail__heading lab-detail__heading--clickable" @click="toggleSection('steps')">
            <span class="lab-detail__heading-label">
              <i data-lucide="list-ordered" class="icon lab-detail__heading-icon"></i>
              实验步骤
            </span>
            <i
              :data-lucide="collapsed.steps ? 'chevron-down' : 'chevron-up'"
              class="icon lab-detail__collapse-icon"
            ></i>
          </h2>
          <transition name="collapse">
            <div v-show="!collapsed.steps" class="lab-detail__card-list">
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
            </article>
          </div>
            </transition>
          </section>

          <section v-if="results.length" class="lab-detail__section lab-detail__section--collapsible">
            <h2 class="lab-detail__heading lab-detail__heading--clickable" @click="toggleSection('results')">
              <span class="lab-detail__heading-label">
                <i data-lucide="clipboard-check" class="icon lab-detail__heading-icon"></i>
                实验结果
              </span>
              <i
                :data-lucide="collapsed.results ? 'chevron-down' : 'chevron-up'"
                class="icon lab-detail__collapse-icon"
              ></i>
            </h2>
            <transition name="collapse">
              <div v-show="!collapsed.results" class="lab-detail__card-list">
            <article v-for="(item, idx) in results" :key="item.resultId || idx" class="lab-detail__card">
              <h3 class="lab-detail__card-title">结果 {{ idx + 1 }}：{{ item.resultName || '未命名结果' }}</h3>
              <FormattedText
                v-if="hasRichContent(item.resultComments)"
                :value="item.resultComments"
                :options="FORMAT_EXP_STEP"
              />
            </article>
          </div>
        </transition>
      </section>

      <section v-if="hasCaution || hasDanger" class="lab-detail__section lab-detail__section--collapsible">
        <h2 class="lab-detail__heading lab-detail__heading--clickable" @click="toggleSection('safety')">
          <span class="lab-detail__heading-label">
            <i data-lucide="shield-alert" class="icon lab-detail__heading-icon"></i>
            安全提示
          </span>
          <i
            :data-lucide="collapsed.safety ? 'chevron-down' : 'chevron-up'"
            class="icon lab-detail__collapse-icon"
          ></i>
        </h2>
        <transition name="collapse">
          <div v-show="!collapsed.safety">
            <div v-if="hasCaution" class="lab-detail__safety lab-detail__safety--caution">
              <p class="lab-detail__safety-label">注意事项</p>
              <FormattedText :value="detail.expCaution" :options="FORMAT_EXP_LONG" />
            </div>
            <div v-if="hasDanger" class="lab-detail__safety lab-detail__safety--danger">
              <p class="lab-detail__safety-label">危险提示</p>
              <FormattedText :value="detail.expDanger" :options="FORMAT_EXP_LONG" />
            </div>
          </div>
        </transition>
      </section>

      <section v-if="references.length" class="lab-detail__section lab-detail__section--collapsible">
        <h2 class="lab-detail__heading lab-detail__heading--clickable" @click="toggleSection('references')">
          <span class="lab-detail__heading-label">
            <i data-lucide="library" class="icon lab-detail__heading-icon"></i>
            参考资料
          </span>
          <i
            :data-lucide="collapsed.references ? 'chevron-down' : 'chevron-up'"
            class="icon lab-detail__collapse-icon"
          ></i>
        </h2>
        <transition name="collapse">
          <div v-show="!collapsed.references" class="lab-detail__card-list">
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
            </transition>
        </section>

        <section v-if="scientists.length" class="lab-detail__section lab-detail__section--collapsible lab-detail__section--last">
          <h2 class="lab-detail__heading lab-detail__heading--clickable" @click="toggleSection('scientists')">
            <span class="lab-detail__heading-label">
              <i data-lucide="users" class="icon lab-detail__heading-icon"></i>
              科学家故事
            </span>
            <i
              :data-lucide="collapsed.scientists ? 'chevron-down' : 'chevron-up'"
              class="icon lab-detail__collapse-icon"
            ></i>
          </h2>
          <transition name="collapse">
            <div v-show="!collapsed.scientists" class="lab-detail__card-list">
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
            </transition>
        </section>

        <section
          v-if="!hasContentSections"
          class="lab-detail__section lab-detail__empty text-center muted-2 text-sm"
        >
          暂无更多实验内容
        </section>
      </template>

      <section v-else class="lab-detail__section lab-detail__empty text-center muted-2 text-sm">
        暂无实验资料
      </section>
    </div>
  </aside>
</template>

<script setup>
import { computed, reactive, watch, onMounted, nextTick } from 'vue'
import FormattedText from '@/components/FormattedText.vue'
import FeedCard from '@/components/FeedCard.vue'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { FORMAT_EXP_LONG, FORMAT_EXP_STEP, hasRichContent } from '@/utils/richText'
import { metaChipItems } from '@/utils/expDisplay'
import { resolveMaterialPicUrl } from '@/utils/fileUrl'

const props = defineProps({
  detail: { type: Object, default: null },
  expId: { type: String, default: '' },
  steps: { type: Array, default: () => [] },
  materials: { type: Array, default: () => [] },
  results: { type: Array, default: () => [] },
  references: { type: Array, default: () => [] },
  scientists: { type: Array, default: () => [] },
  recommendations: { type: Array, default: () => [] },
  headTitle: { type: String, default: '实验资料' },
  headSub: { type: String, default: '' }
})

const metaChips = computed(() => metaChipItems(props.detail))

const hasPrinciple = computed(() => hasRichContent(props.detail?.expPrinciple))
const hasCaution = computed(() => hasRichContent(props.detail?.expCaution))
const hasDanger = computed(() => hasRichContent(props.detail?.expDanger))

const hasContentSections = computed(() =>
  hasPrinciple.value ||
  props.materials.length > 0 ||
  props.steps.length > 0 ||
  props.results.length > 0 ||
  hasCaution.value ||
  hasDanger.value ||
  props.references.length > 0 ||
  props.scientists.length > 0
)

function materialPic(item) {
  return resolveMaterialPicUrl(item)
}

const { initIcons } = useLucideIcons()

// 折叠状态
const collapsed = reactive({
  principle: true,
  materials: true,
  steps: true,
  results: true,
  safety: true,
  references: true,
  scientists: true
})
function toggleSection(key) {
  collapsed[key] = !collapsed[key]
  nextTick(initIcons)
}

watch(
  () => props.recommendations?.length,
  () => initIcons()
)

onMounted(initIcons)
</script>
