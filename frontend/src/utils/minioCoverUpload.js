import { uploadMinioFile } from '../api/system'

/** 上传封面图到 MinIO，返回 { fileUrl, previewUrl, fileName } */
export async function uploadCoverToMinio(fileOrBlob, fileName = 'cover.jpg') {
  const file = fileOrBlob instanceof File
    ? fileOrBlob
    : new File([fileOrBlob], fileName, { type: fileOrBlob.type || 'image/jpeg' })
  const res = await uploadMinioFile(file)
  if (res.data?.code !== 200) {
    throw new Error(res.data?.message || '封面上传失败')
  }
  const data = res.data.data || {}
  return {
    fileUrl: data.fileUrl || '',
    previewUrl: data.previewUrl || data.fileUrl || '',
    fileName: data.fileName || file.name
  }
}
