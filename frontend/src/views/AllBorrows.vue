<template>
  <div>
    <div class="breadcrumb"><router-link to="/">Home</router-link><span>/</span><span>All Borrows</span></div>
    <div class="page-header">
      <h3>All Borrows记录</h3>
      <button class="btn btn-outline btn-sm" @click="$router.push('/my-borrows')">My Borrows</button>
    </div>

    <div v-if="loading" class="skeleton-table">
      <div class="shead"><div class="sc" style="width:100px;"></div></div>
      <div class="skeleton-row" v-for="n in 4" :key="n">
        <div class="skeleton-cell" style="width:15%;"></div>
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
          <thead><tr><th>Borrow人</th><th>图书</th><th>Borrow日期</th><th>Due Date</th><th>Return日期</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>
            <tr v-for="r in records" :key="r.id">
              <td><div style="font-weight:600;">{{ r.userFullName || r.username }}</div><div style="font-size:0.75rem;color:var(--warm-gray-light);">@{{ r.username }}</div></td>
              <td><router-link :to="`/books/${r.bookId}`" class="book-link">{{ r.bookTitle }}</router-link></td>
              <td style="color:var(--warm-gray);font-size:0.8125rem;">{{ formatDate(r.borrowDate) }}</td>
              <td :class="isOverdue(r) ? 'overdue-text' : ''" style="font-size:0.8125rem;">{{ formatDate(r.dueDate) }}</td>
              <td style="color:var(--warm-gray);font-size:0.8125rem;">{{ r.returnDate ? formatDate(r.returnDate) : '-' }}</td>
              <td><span :class="'status-tag ' + r.status">{{ statusLabel(r.status) }}</span></td>
              <td>
                <button v-if="r.status === 'borrowed'" class="btn btn-sm btn-primary" @click="handleReturn(r)" :disabled="returnLoading === r.id">{{ returnLoading === r.id ? 'Returning...' : '代为Return' }}</button>
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
        <circle cx="32" cy="32" r="6" stroke="#C4BAAE" stroke-width="1.5"/>
        <line x1="36" y1="35" x2="42" y2="41" stroke="#C4BAAE" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <div class="empty-title">暂无Borrow记录</div>
      <div class="empty-desc">系统中还没有任何Borrow记录</div>
    </div>

    <div v-if="msg" :class="msgType" style="margin-top:var(--space-md);">{{ msg }}</div>
  </div>
</template>

<script>
import api from '../services/api.js'
import { showToast } from '../services/toast.js'
export default {
  name: 'AllBorrows',
  data() { return { records: [], loading: false, returnLoading: null, msg: '', msgType: '' } },
  async mounted() { await this.loadRecords() },
  methods: {
    async loadRecords() {
      this.loading = true
      try { const res = await api.get('/borrow'); if (res.code === 200 && res.data) this.records = res.data.records || [] }
      catch (err) { this.msg = err.message; this.msgType = 'error-message' } finally { this.loading = false }
    },
    async handleReturn(record) {
      this.msg = ''; this.returnLoading = record.id
      try { const res = await api.post(`/borrow/${record.id}/return`); if (res.code === 200) { showToast('Return successful'); await this.loadRecords() } else { showToast(res.message || 'ReturnFailed', 'error') } }
      catch (err) { showToast(err.message || 'ReturnFailed', 'error') } finally { this.returnLoading = null }
    },
    isOverdue(r) { return r.status === 'borrowed' && new Date(r.dueDate) < new Date() },
    statusLabel(s) { return { borrowed: 'Borrow中', returned: '已Return', overdue: 'Overdue' }[s] || s },
    formatDate(dt) { if (!dt) return '-'; return new Date(dt).toLocaleDateString('en-US') }
  }
}
</script>

<style scoped>
.book-link { color: var(--copper); font-weight: 600; }
.book-link:hover { color: var(--copper-dark); text-decoration: underline; }
</style>
