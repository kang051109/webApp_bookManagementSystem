import { reactive } from 'vue'

/**
 * Global authStatusstore (reactive)
 * Layout 组件Update此对象，所有子组件直接导入Using
 */
export const authStore = reactive({
  currentUser: null,
  isAdmin: false
})
