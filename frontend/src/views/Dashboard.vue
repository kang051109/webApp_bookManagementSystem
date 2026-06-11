<template>
  <div>
    <div class="breadcrumb"><router-link to="/dashboard">Dashboard</router-link></div>
    <div class="page-header"><h3>Dashboard</h3></div>

    <!-- Issue 2: Skeleton loading -->
    <div v-if="loading" class="dash-grid">
      <div class="stat-block" v-for="n in 5" :key="n" style="padding:24px 20px;">
        <div class="skeleton" style="width:60%;height:36px;margin-bottom:8px;"></div>
        <div class="skeleton" style="width:40%;height:12px;"></div>
      </div>
    </div>

    <div v-else class="dash-grid">
      <div class="stat-block total-books">
        <span class="stat-figure">{{ stats.totalBooks }}</span>
        <span class="stat-label">Total Books <span class="trend">{{ stats.trends?.books }}</span></span>
      </div>
      <div class="stat-block">
        <span class="stat-figure">{{ stats.totalUsers }}</span>
        <span class="stat-label">RegisterUser <span class="trend">{{ stats.trends?.users }}</span></span>
      </div>
      <div class="stat-block">
        <span class="stat-figure">{{ stats.activeBorrows }}</span>
        <span class="stat-label">Borrow中 <span class="trend">{{ stats.trends?.active }}</span></span>
      </div>
      <div class="stat-block">
        <span class="stat-figure">{{ stats.totalCategories }}</span>
        <span class="stat-label">Categories</span>
      </div>
      <div class="stat-block overdue-block">
        <span class="stat-figure">{{ stats.overdueBorrows }}</span>
        <span class="stat-label">Overdue <span class="trend">{{ stats.trends?.overdue }}</span></span>
      </div>
    </div>

    <div class="quick-section">
      <h4 class="quick-heading">QuickActions</h4>
      <div class="quick-row">
        <button class="quick-btn" @click="$router.push('/books')">Browse Books</button>
        <button class="quick-btn" @click="$router.push('/my-borrows')">My Borrows</button>
        <button v-if="authStore.isAdmin" class="quick-btn" @click="$router.push('/books/new')">New Book</button>
        <button v-if="authStore.isAdmin" class="quick-btn" @click="$router.push('/categories')">Manage Categories</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api.js'
import { authStore } from '../services/authStore.js'
export default {
  name: 'Dashboard',
  data() { return { authStore, stats: { totalBooks: 0, totalUsers: 0, totalCategories: 0, activeBorrows: 0, overdueBorrows: 0, trends:{} }, loading: false } },
  async mounted() { document.title = 'Dashboard - Book Management System'; await this.loadStats() },
  methods: {
    async loadStats() {
      this.loading = true
      try { const res = await api.get('/dashboard/stats'); if (res.code === 200 && res.data) this.stats = res.data } catch {} finally { this.loading = false }
    }
  }
}
</script>

<style scoped>
.dash-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1px; background: var(--warm-border); border: 1px solid var(--warm-border); margin-bottom: var(--space-xl); }
.stat-block { background: var(--surface); padding: 24px 20px; display: flex; flex-direction: column; gap: 6px; }
.total-books { grid-column: 1 / 2; background: var(--warm-black); padding: 32px 24px; }
.total-books .stat-figure { color: var(--surface); }
.total-books .stat-label { color: rgba(250, 248, 244, 0.6); }
.stat-figure { font-family: var(--font-display); font-size: 2.5rem; line-height: 1; color: var(--warm-black); font-weight: 400; }
.stat-label { font-family: var(--font-body); font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--warm-gray); }
.trend { font-size: 0.625rem; margin-left: 4px; opacity: 0.7; }
.overdue-block { background: var(--red-earth-light); border-left: 3px solid var(--red-earth); }
.overdue-block .stat-figure { color: var(--red-earth); }
.overdue-block .stat-label { color: var(--red-earth); }
.quick-section { margin-top: var(--space-lg); }
.quick-heading { font-family: var(--font-display); font-size: 1rem; font-weight: 400; color: var(--warm-gray); margin-bottom: var(--space-md); letter-spacing: 0.03em; }
.quick-row { display: flex; gap: 10px; flex-wrap: wrap; }
.quick-btn { padding: 10px 22px; border: 1px solid var(--warm-border); background: var(--surface); font-family: var(--font-body); font-size: 0.8125rem; color: var(--warm-gray); cursor: pointer; transition: all 0.15s; }
.quick-btn:hover { border-color: var(--copper); color: var(--copper); transform: translateY(-1px); }

/* Issue 5: Responsive */
@media (max-width: 768px) {
  .dash-grid { grid-template-columns: 1fr; }
  .total-books { grid-column: 1; }
  .stat-figure { font-size: 2rem; }
}
</style>
