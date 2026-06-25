<template>
  <MobilePageShell class="safe-top safe-bottom" data-layout="detail">
    <div class="topbar page-topbar safe-top">
      <PageBackButton fallback="/login" />
      <h1 class="topbar-title">{{ doc.title }}</h1>
    </div>

    <div class="px-4 pb-8 stack-4 legal-doc">
      <p class="text-xs muted">更新日期：{{ doc.updated }}</p>
      <div class="card card-pad stack-4">
        <section v-for="section in doc.sections" :key="section.heading" class="stack-2">
          <h2 class="text-sm font-bold">{{ section.heading }}</h2>
          <p v-for="(para, idx) in section.paragraphs" :key="idx" class="text-sm muted legal-doc__para">{{ para }}</p>
        </section>
      </div>
    </div>
  </MobilePageShell>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'

const route = useRoute()

const DOCS = {
  terms: {
    title: '用户协议',
    updated: '2026-06-22',
    sections: [
      {
        heading: '1. 服务说明',
        paragraphs: [
          '「宝山小实验社区」（以下简称「本平台」）由学校或教育主管部门授权运营，面向学生、教师、家长提供科学实验学习、任务提交、成果展示与家校协同服务。',
          '使用本平台即表示您已阅读并同意本协议。若您不同意，请停止使用并联系学校管理员。'
        ]
      },
      {
        heading: '2. 账号与安全',
        paragraphs: [
          '账号由学校分配或经家长自助注册后由教师审核开通。您应妥善保管登录凭证，不得转借、出租或共享账号。',
          '发现账号异常使用时，请及时联系学校管理员或修改密码。'
        ]
      },
      {
        heading: '3. 用户行为规范',
        paragraphs: [
          '您上传的内容（文字、图片、视频等）应真实、合法，不得包含违法、侵权、暴力、色情或其他不适宜未成年人观看的信息。',
          '教师评价、家长协助上传等行为应出于教育目的，尊重学生隐私与知识产权。'
        ]
      },
      {
        heading: '4. 知识产权',
        paragraphs: [
          '平台内的课程、实验资源、界面设计等知识产权归平台或权利人所有。用户上传的作品，用户保留相应权利，同时授予平台为教学展示所必需的存储与展示许可。'
        ]
      },
      {
        heading: '5. 免责声明',
        paragraphs: [
          '涉及实验操作的安全提示仅供参考，实际实验应在教师或家长指导下进行，并遵守学校实验室安全规范。',
          '因网络、设备或第三方服务导致的中断，平台将尽力恢复但不承担由此产生的间接损失。'
        ]
      },
      {
        heading: '6. 协议变更',
        paragraphs: [
          '平台可根据法律法规或业务需要更新本协议，更新后将通过应用内公告等方式告知。继续使用即视为接受更新后的协议。'
        ]
      }
    ]
  },
  privacy: {
    title: '隐私政策',
    updated: '2026-06-22',
    sections: [
      {
        heading: '1. 我们收集的信息',
        paragraphs: [
          '为提供登录、任务、作品上传与家校绑定等功能，我们可能收集：账号信息（姓名、学号/工号、手机号）、班级与学校信息、您主动上传的学习成果、设备与日志信息（用于保障服务安全）。',
          'AI 助手对话内容用于当次会话回复，会话历史存储于服务端以便您继续对话，您可在应用内清除会话。'
        ]
      },
      {
        heading: '2. 信息的使用',
        paragraphs: [
          '信息用于身份验证、任务分发、教师评价、家长查看孩子学习进度、消息通知及改进产品体验。',
          '未经法律要求或您的同意，我们不会向无关第三方出售您的个人信息。'
        ]
      },
      {
        heading: '3. 未成年人保护',
        paragraphs: [
          '本平台主要面向 K12 教育场景。14 周岁以下学生的账号注册与绑定需经家长同意并完成教师审核。',
          '家长可查看绑定孩子的任务与成长数据；学生可在设置中管理部分可见性偏好（如成长档案）。'
        ]
      },
      {
        heading: '4. 信息存储与安全',
        paragraphs: [
          '数据存储于中华人民共和国境内服务器，并采取访问控制、传输加密等合理安全措施。',
          '尽管已采取保护措施，互联网传输无法保证绝对安全，请您理解相关风险。'
        ]
      },
      {
        heading: '5. 您的权利',
        paragraphs: [
          '您可通过「设置」查看与修改部分个人信息，或联系学校管理员申请更正、删除账号及相关数据（法律法规另有规定的除外）。',
          '绑定钉钉等第三方账号时，仅获取实现登录绑定所必需的信息。'
        ]
      },
      {
        heading: '6. 联系我们',
        paragraphs: [
          '如对本政策有疑问，请通过学校管理员或平台公布的客服渠道联系我们。'
        ]
      }
    ]
  }
}

const doc = computed(() => {
  const kind = route.path.includes('privacy') ? 'privacy' : 'terms'
  return DOCS[kind]
})
</script>

<style scoped>
.legal-doc__para {
  line-height: 1.65;
  white-space: pre-line;
}
</style>
