import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Layout from '../views/Layout.vue'
import CategoryManage from '../views/CategoryManage.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/dashboard',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      }
    ]
  },
  {
    path: '/books',
    component: Layout,
    children: [
      { path: '', name: 'BookList', component: () => import('../views/BookList.vue') },
      { path: 'new', name: 'BookNew', component: () => import('../views/BookForm.vue') },
      { path: ':id', name: 'BookDetail', component: () => import('../views/BookDetail.vue') },
      { path: ':id/edit', name: 'BookEdit', component: () => import('../views/BookForm.vue') }
    ]
  },
  {
    path: '/categories',
    component: Layout,
    children: [
      { path: '', name: 'CategoryManage', component: CategoryManage }
    ]
  },
  {
    path: '/my-borrows',
    component: Layout,
    children: [
      { path: '', name: 'MyBorrows', component: () => import('../views/MyBorrows.vue') }
    ]
  },
  {
    path: '/admin/borrows',
    component: Layout,
    children: [
      { path: '', name: 'AllBorrows', component: () => import('../views/AllBorrows.vue') }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
