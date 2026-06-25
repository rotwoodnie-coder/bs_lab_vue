/** 解析上传接口错误（含 502/504 网关、超时） */
export function formatUploadError(error) {
  const status = error?.response?.status
  const serverMsg = error?.response?.data?.message
  if (serverMsg) {
    if (/request method 'get' not supported/i.test(serverMsg)) {
      return '上传请求方法错误：请确认未手动访问上传接口，并重新部署最新前端后重试'
    }
    if (/required request part 'file' is not present/i.test(serverMsg)) {
      return '上传失败：未收到文件数据，请检查 Nginx 是否放开了 multipart 上传'
    }
    return serverMsg
  }
  if (status === 502) {
    const size = error?.config?.data instanceof FormData
      ? '(请确认 HTML 文件大小与 Nginx client_max_body_size、Tomcat maxPostSize 已放开)'
      : ''
    return `网关错误(502)：上传请求被网关或 Tomcat 拒绝${size}。请检查 Nginx proxy_request_buffering off、client_max_body_size 1024m，并重启后端使 Tomcat 限制生效`
  }
  if (status === 504) {
    return '上传超时(504)：文件较大时请调大 Nginx proxy_read_timeout 后重试'
  }
  if (status === 413) {
    return '文件超过服务器大小限制'
  }
  const msg = error?.message || ''
  if (error?.code === 'ECONNABORTED' || /timeout/i.test(msg)) {
    return '上传超时，请检查网络或联系管理员调大网关超时时间'
  }
  if (msg === 'Network Error') {
    return '网络中断或上传被网关拦截（html/htm 文件名常被 WAF 拒绝），请重试或联系管理员在 Nginx 上传接口关闭安全拦截'
  }
  return msg || '上传失败'
}
