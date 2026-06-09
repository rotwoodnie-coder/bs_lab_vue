import { ref } from 'vue'
import { searchStudents } from '@/api/org'
import { apiMessage } from '@/utils/apiError'

/** 按班级 + 姓名查询学生，供注册/绑定确认步使用 */
export function useStudentMatch() {
  const searching = ref(false)
  const searchError = ref('')
  const candidates = ref([])
  const selectedStudent = ref(null)
  const studentNo = ref('')

  function resetStudentMatch() {
    searching.value = false
    searchError.value = ''
    candidates.value = []
    selectedStudent.value = null
    studentNo.value = ''
  }

  function pickStudent(item) {
    selectedStudent.value = item
    searchError.value = ''
  }

  async function lookupStudent(classOrgId, childName) {
    const name = (childName || '').trim()
    if (!classOrgId || !name) {
      searchError.value = '请先选择班级并填写孩子姓名'
      return false
    }

    searching.value = true
    searchError.value = ''
    candidates.value = []
    selectedStudent.value = null

    try {
      const res = await searchStudents({
        classOrgId,
        name,
        studentNo: studentNo.value.trim() || undefined
      })
      if (res?.code !== 200) {
        searchError.value = res?.message || '查询失败'
        return false
      }

      const list = res.data || []
      if (list.length === 0) {
        searchError.value = '未找到该学生，请核对姓名与班级'
        return false
      }

      candidates.value = list
      if (list.length === 1) {
        selectedStudent.value = list[0]
      }
      return true
    } catch (e) {
      searchError.value = apiMessage(e, '查询失败')
      return false
    } finally {
      searching.value = false
    }
  }

  return {
    searching,
    searchError,
    candidates,
    selectedStudent,
    studentNo,
    resetStudentMatch,
    pickStudent,
    lookupStudent
  }
}
