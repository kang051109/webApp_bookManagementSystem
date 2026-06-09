import { reactive } from 'vue'

/**
 * 全局认证状态存储（响应式）
 * Layout 组件更新此对象，所有子组件直接导入使用
 */
export const authStore = reactive({
  currentUser: null,
  isAdmin: false
})
