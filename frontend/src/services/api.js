import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 响应拦截器 - 统一处理错误
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response) {
      const data = error.response.data
      return Promise.reject(new Error(data.message || '请求失败'))
    } else if (error.request) {
      // 请求已发出但没有收到响应
      return Promise.reject(new Error('无法连接后端服务（请检查 Tomcat 是否已启动）'))
    }
    return Promise.reject(new Error('请求发送失败'))
  }
)

export default api
