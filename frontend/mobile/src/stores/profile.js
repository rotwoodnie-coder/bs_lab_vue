import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { fetchProfile, updateProfile } from '@/api/profile'
import { uploadFile, parseUploadResponse } from '@/api/work'
import { resolveFileUrl } from '@/utils/fileUrl'
import { avatarGradByRole } from '@/utils/avatar'
import { useUserStore } from './user'

export const useProfileStore = defineStore('profile', () => {
  const profile = ref({})
  const avatarPreviewUrl = ref('')
  const avatarUploading = ref(false)

  let loadPromise = null

  const roleLower = computed(() =>
    (profile.value.userRoleId || 'student').toLowerCase()
  )

  const isStudent = computed(() => roleLower.value === 'student')
  const isParent = computed(() => roleLower.value === 'parent')
  const isTeacher = computed(() => roleLower.value === 'teacher')

  const displayName = computed(() =>
    profile.value.userNickName ||
    profile.value.userName ||
    useUserStore().userInfo?.username ||
    '用户'
  )

  const userInitial = computed(() => displayName.value.charAt(0) || '用')

  const avatarUrl = computed(() => {
    if (avatarPreviewUrl.value) return avatarPreviewUrl.value
    const logo = profile.value?.userLogo
    return logo ? resolveFileUrl(logo) : ''
  })

  const hasAvatar = computed(() => !!avatarUrl.value)

  const avatarGradClass = computed(() => avatarGradByRole(roleLower.value))

  function applyProfile(data) {
    if (!data) return
    profile.value = data
    syncAvatarPreview()
    const userStore = useUserStore()
    if (data.userName) userStore.userInfo.username = data.userName
    if (data.rootOrgName) userStore.userInfo.rootOrgName = data.rootOrgName
    if (data.userRoleId) userStore.userInfo.userRoleId = data.userRoleId
  }

  function syncAvatarPreview() {
    const logo = profile.value?.userLogo
    avatarPreviewUrl.value = logo ? resolveFileUrl(logo) : ''
  }

  function setAvatarPreview(url) {
    avatarPreviewUrl.value = url || ''
  }

  async function loadProfile(force = false) {
    if (loadPromise && !force) return loadPromise

    loadPromise = (async () => {
      try {
        const res = await fetchProfile()
        if (res?.code === 200 && res.data) {
          applyProfile(res.data)
        }
        return res
      } catch (e) {
        return null
      } finally {
        loadPromise = null
      }
    })()

    return loadPromise
  }

  async function patchProfile(data) {
    const res = await updateProfile(data)
    if (res?.code === 200) {
      await loadProfile(true)
    }
    return res
  }

  async function uploadAvatar(file) {
    if (!file || avatarUploading.value) {
      throw new Error('无效的文件')
    }

    avatarUploading.value = true
    const localPreview = URL.createObjectURL(file)
    setAvatarPreview(localPreview)

    try {
      const up = await uploadFile(file)
      if (up?.code !== 200) {
        throw new Error(up?.message || '上传失败')
      }

      const { fileUrl, previewUrl } = parseUploadResponse(up)
      if (!fileUrl) throw new Error('未返回文件地址')

      if (previewUrl) setAvatarPreview(previewUrl)

      const res = await patchProfile({ userLogo: fileUrl })
      if (res?.code !== 200) {
        throw new Error(res?.message || '头像更新失败')
      }
      return res
    } catch (e) {
      syncAvatarPreview()
      throw e
    } finally {
      URL.revokeObjectURL(localPreview)
      avatarUploading.value = false
    }
  }

  function reset() {
    profile.value = {}
    avatarPreviewUrl.value = ''
    avatarUploading.value = false
    loadPromise = null
  }

  return {
    profile,
    avatarPreviewUrl,
    avatarUploading,
    roleLower,
    isStudent,
    isParent,
    isTeacher,
    displayName,
    userInitial,
    avatarUrl,
    hasAvatar,
    avatarGradClass,
    applyProfile,
    syncAvatarPreview,
    setAvatarPreview,
    loadProfile,
    patchProfile,
    uploadAvatar,
    reset
  }
})
