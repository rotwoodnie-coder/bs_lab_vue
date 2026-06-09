<template>
  <el-badge :value="count" :hidden="!count" :max="99" class="message-badge">
    <el-button link class="message-btn" @click="openDialog">
      <el-icon :size="18"><BellFilled /></el-icon>
    </el-button>
  </el-badge>

  <el-dialog v-model="dialogVisible" title="消息" width="920px" append-to-body destroy-on-close class="message-dialog">
    <SysMsgView compact />
  </el-dialog>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { BellFilled } from '@element-plus/icons-vue'
import { fetchUnreadSystemMessageCount } from '../api/index'
import SysMsgView from '../views/system/SysMsgView.vue'

const count = ref(0)
const dialogVisible = ref(false)

const loadCount = async () => {
  try {
    const res = await fetchUnreadSystemMessageCount()
    if (res.data.code === 200) {
      count.value = Number(res.data.data || 0)
    }
  } catch (error) {
    count.value = 0
  }
}

const openDialog = () => {
  dialogVisible.value = true
}

onMounted(loadCount)
</script>
