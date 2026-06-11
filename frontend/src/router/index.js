import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Layout from '../views/Layout.vue'
import CategoryManage from '../views/CategoryManage.vue'
import NProgress from 'nprogress'
import api from '../services/api.js'

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/', redirect: '/login' },
  {
    path: '/dashboard', component: Layout,
    children: [{ path: '', name: 'Dashboard', component: () => import('../views/Dashboard.vue') }]
  },
  {
    path: '/books', component: Layout,
    children: [
      { path: '', name: 'BookList', component: () => import('../views/BookList.vue') },
      { path: 'new', name: 'BookNew', component: () => import('../views/BookForm.vue') },
      { path: ':id', name: 'BookDetail', component: () => import('../views/BookDetail.vue') },
      { path: ':id/edit', name: 'BookEdit', component: () => import('../views/BookForm.vue') }
    ]
  },
  {
    path: '/categories', component: Layout,
    children: [{ path: '', name: 'CategoryManage', component: CategoryManage }]
  },
  {
    path: '/my-borrows', component: Layout,
    children: [{ path: '', name: 'MyBorrows', component: () => import('../views/MyBorrows.vue') }]
  },
  {
    path: '/admin/borrows', component: Layout,
    children: [{ path: '', name: 'AllBorrows', component: () => import('../views/AllBorrows.vue') }]
  },
  {
    path: '/admin/users', component: Layout,
    children: [{ path: '', name: 'UserManage', component: () => import('../views/UserManage.vue') }]
  },
  {
    path: '/change-password', component: Layout,
    children: [{ path: '', name: 'ChangePassword', component: () => import('../views/ChangePassword.vue') }]
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({ history: createWebHistory(), routes })

// Cached loginStatus, avoids re-request per route
let authChecked = false
let authValid = false

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  // Login/Register pages skip auth
  if (to.path === '/login' || to.path === '/register') {
    authChecked = false; next(); return
  }

  // Already verified, pass through
  if (authChecked && authValid) { next(); return }

  // 验证Login
  try {
    const res = await api.get('/auth/me')
    if (res.code === 200) {
      authChecked = true; authValid = true
      // Update doc title
      document.title = (to.name || 'page') + ' - Book Management System'
      next()
    } else {
      authValid = false; authChecked = true; next('/login')
    }
  } catch {
    authValid = false; authChecked = true; next('/login')
  }
})

router.afterEach(() => { NProgress.done() })

export default router
