import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Response interceptor - UnifiedProcessing错误
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response) {
      const data = error.response.data
      return Promise.reject(new Error(data.message || 'Request failed'))
    } else if (error.request) {
      // Request sent but no response
      return Promise.reject(new Error('Cannot connect to backend (check if Tomcat is running)'))
    }
    return Promise.reject(new Error('Request failed'))
  }
)

export default api
