<template>
  <div>
    <div class="breadcrumb"><router-link to="/dashboard">仪表板</router-link></div>
    <div class="page-header"><h3>仪表板</h3></div>

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
        <span class="stat-label">藏书总量</span>
      </div>
      <div class="stat-block">
        <span class="stat-figure">{{ stats.totalUsers }}</span>
        <span class="stat-label">注册用户</span>
      </div>
      <div class="stat-block">
        <span class="stat-figure">{{ stats.activeBorrows }}</span>
        <span class="stat-label">借阅中</span>
      </div>
      <div class="stat-block">
        <span class="stat-figure">{{ stats.totalCategories }}</span>
        <span class="stat-label">分类数</span>
      </div>
      <div class="stat-block overdue-block">
        <span class="stat-figure">{{ stats.overdueBorrows }}</span>
        <span class="stat-label">逾期未还</span>
      </div>
    </div>

    <div class="quick-section">
      <h4 class="quick-heading">快速操作</h4>
      <div class="quick-row">
        <button class="quick-btn" @click="$router.push('/books')">浏览图书</button>
        <button class="quick-btn" @click="$router.push('/my-borrows')">我的借阅</button>
        <button v-if="isAdmin" class="quick-btn" @click="$router.push('/books/new')">新增图书</button>
        <button v-if="isAdmin" class="quick-btn" @click="$router.push('/categories')">管理分类</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api.js'
export default {
  name: 'Dashboard',
  inject: ['isAdmin'],
  data() { return { stats: { totalBooks: 0, totalUsers: 0, totalCategories: 0, activeBorrows: 0, overdueBorrows: 0 }, loading: false } },
  async mounted() { await this.loadStats() },
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
