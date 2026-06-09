<template>
  <div>
    <div class="breadcrumb"><router-link to="/">首页</router-link><span>/</span><span>我的借阅</span></div>
    <div class="page-header">
      <h3>我的借阅</h3>
      <button v-if="authStore.isAdmin" class="btn btn-outline btn-sm" @click="$router.push('/admin/borrows')">全部记录</button>
    </div>

    <div v-if="loading" class="skeleton-table">
      <div class="shead"><div class="sc" style="width:100px;"></div></div>
      <div class="skeleton-row" v-for="n in 4" :key="n">
        <div class="skeleton-cell" style="width:20%;"></div>
        <div class="skeleton-cell" style="width:15%;"></div>
        <div class="skeleton-cell" style="width:15%;"></div>
        <div class="skeleton-cell" style="width:15%;"></div>
        <div class="skeleton-cell" style="width:12%;"></div>
        <div class="skeleton-cell" style="width:10%;"></div>
      </div>
    </div>

    <div v-else-if="records.length > 0" class="table-scroll">
      <div class="table-wrap">
        <table class="data-table">
          <thead><tr><th>图书</th><th>借阅日期</th><th>应还日期</th><th>归还日期</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="r in records" :key="r.id">
              <td><router-link :to="`/books/${r.bookId}`" class="book-link">{{ r.bookTitle }}</router-link></td>
              <td style="color:var(--warm-gray);font-size:0.8125rem;">{{ formatDate(r.borrowDate) }}</td>
              <td :class="isOverdue(r) ? 'overdue-text' : ''" style="font-size:0.8125rem;">{{ formatDate(r.dueDate) }}</td>
              <td style="color:var(--warm-gray);font-size:0.8125rem;">{{ r.returnDate ? formatDate(r.returnDate) : '-' }}</td>
              <td><span :class="'status-tag ' + r.status">{{ statusLabel(r.status) }}</span></td>
              <td>
                <button v-if="r.status === 'borrowed'" class="btn btn-sm btn-primary" @click="handleReturn(r)" :disabled="returnLoading === r.id">{{ returnLoading === r.id ? '归还中...' : '归还' }}</button>
                <span v-else style="color:var(--warm-gray-light);font-size:0.8125rem;">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-else class="empty-state">
      <svg class="empty-icon" width="64" height="64" viewBox="0 0 64 64" fill="none">
        <rect x="12" y="16" width="40" height="32" rx="2" stroke="#C4BAAE" stroke-width="1.5" fill="#FAF8F4"/>
        <path d="M20 28 L28 36 L44 24" stroke="#C4BAAE" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <div class="empty-title">暂无借阅记录</div>
      <div class="empty-desc">你还没有借阅任何图书</div>
      <button class="btn btn-primary btn-sm empty-action" @click="$router.push('/books')">去浏览图书</button>
    </div>

    <div v-if="msg" :class="msgType" style="margin-top:var(--space-md);">{{ msg }}</div>
  </div>
</template>

<script>
import { authStore } from '../services/authStore.js'
import api from '../services/api.js'
import { showToast } from '../services/toast.js'
export default {
  name: 'MyBorrows',
  data() { return { authStore, records: [], loading: false, returnLoading: null, msg: '', msgType: '' } },
  async mounted() { await this.loadRecords() },
  methods: {
    async loadRecords() {
      this.loading = true
      try { const res = await api.get('/borrow/my'); if (res.code === 200 && res.data) this.records = res.data.records || [] }
      catch (err) { this.msg = err.message; this.msgType = 'error-message' } finally { this.loading = false }
    },
    async handleReturn(record) {
      this.msg = ''; this.returnLoading = record.id
      try { const res = await api.post(`/borrow/${record.id}/return`); if (res.code === 200) { showToast('归还成功'); await this.loadRecords() } else { showToast(res.message || '归还失败', 'error') } }
      catch (err) { showToast(err.message || '归还失败', 'error') } finally { this.returnLoading = null }
    },
    isOverdue(r) { return r.status === 'borrowed' && new Date(r.dueDate) < new Date() },
    statusLabel(s) { return { borrowed: '借阅中', returned: '已归还', overdue: '已逾期' }[s] || s },
    formatDate(dt) { if (!dt) return '-'; return new Date(dt).toLocaleDateString('zh-CN') }
  }
}
</script>

<style scoped>
.book-link { color: var(--copper); font-weight: 600; }
.book-link:hover { color: var(--copper-dark); text-decoration: underline; }
</style>
