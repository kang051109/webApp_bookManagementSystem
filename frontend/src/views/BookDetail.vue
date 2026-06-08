<template>
  <div>
    <div class="breadcrumb"><router-link to="/books">图书管理</router-link><span>/</span><span>{{ book?.title || '加载中...' }}</span></div>
    <div class="detail-top">
      <button class="btn btn-outline btn-sm" @click="$router.push('/books')">&larr; 返回</button>
      <div v-if="isAdmin && book">
        <button class="btn btn-outline btn-sm" @click="$router.push(`/books/${book.id}/edit`)">编辑</button>
      </div>
    </div>

    <!-- Skeleton -->
    <div v-if="loading" class="detail-layout">
      <div class="detail-main">
        <div class="skeleton" style="width:60%;height:28px;margin-bottom:var(--space-lg);"></div>
        <div class="skeleton" style="width:40%;height:14px;margin-bottom:8px;"></div>
        <div class="skeleton" style="width:30%;height:14px;margin-bottom:8px;"></div>
        <div class="skeleton" style="width:50%;height:14px;"></div>
      </div>
      <div class="detail-side">
        <div class="side-card"><div class="skeleton" style="width:100%;height:100px;background:rgba(250,248,244,0.1);"></div></div>
      </div>
    </div>

    <div v-else-if="error" class="empty-state">{{ error }}</div>

    <div v-else-if="book" class="detail-layout">
      <div class="detail-main">
        <h2 class="detail-title">{{ book.title }}</h2>
        <div class="detail-meta">
          <div class="meta-row"><span class="meta-label">作者</span><span>{{ book.author }}</span></div>
          <div class="meta-row"><span class="meta-label">ISBN</span><span style="font-family:monospace;font-size:0.8125rem;color:var(--warm-gray);">{{ book.isbn }}</span></div>
          <div class="meta-row"><span class="meta-label">出版社</span><span>{{ book.publisher || '-' }}</span></div>
          <div class="meta-row"><span class="meta-label">出版年份</span><span>{{ book.publishYear || '-' }}</span></div>
          <div class="meta-row"><span class="meta-label">分类</span><span>{{ book.categoryName || '-' }}</span></div>
        </div>
        <div class="detail-desc" v-if="book.description">
          <h5>简介</h5>
          <p>{{ book.description }}</p>
        </div>
      </div>
      <div class="detail-side">
        <div class="side-card">
          <div class="side-stat"><span class="side-stat-num">{{ book.totalCopies }}</span><span class="side-stat-label">总库存</span></div>
          <div class="side-stat"><span class="side-stat-num" :class="book.availableCopies > 0 ? 'green' : 'red'">{{ book.availableCopies }}</span><span class="side-stat-label">可借</span></div>
          <button v-if="currentUser" class="btn btn-primary btn-block" :disabled="book.availableCopies <= 0 || borrowLoading" @click="handleBorrow">
            {{ borrowLoading ? '借阅中...' : (book.availableCopies > 0 ? '借阅此书' : '暂无余量') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api.js'
import { showToast } from '../services/toast.js'
export default {
  name: 'BookDetail', inject: ['currentUser', 'isAdmin'],
  data() { return { book: null, loading: false, error: '', borrowLoading: false } },
  async mounted() { await this.loadBook() },
  methods: {
    async loadBook() {
      this.loading = true; this.error = ''
      try { const res = await api.get(`/books/${this.$route.params.id}`); if (res.code === 200 && res.data) this.book = res.data.book; else this.error = '图书不存在' }
      catch (err) { this.error = '获取图书详情失败: ' + err.message } finally { this.loading = false }
    },
    async handleBorrow() {
      this.borrowMsg = ''; this.borrowLoading = true
      try { const res = await api.post('/borrow', { bookId: this.book.id }); if (res.code === 200) { showToast('借阅成功'); await this.loadBook() } else { showToast(res.message || '借阅失败', 'error') } }
      catch (err) { showToast(err.message || '借阅失败', 'error') } finally { this.borrowLoading = false }
    }
  }
}
</script>

<style scoped>
.detail-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.detail-layout { display: flex; gap: var(--space-xl); align-items: flex-start; }
.detail-main { flex: 1; background: var(--surface); border: 1px solid var(--warm-border); padding: var(--space-xl); }
.detail-title { font-family: var(--font-display); font-size: 1.5rem; font-weight: 400; margin-bottom: var(--space-lg); line-height: 1.3; }
.detail-meta { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: var(--space-lg); }
.meta-label { display: block; font-size: 0.6875rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--warm-gray); margin-bottom: 2px; }
.detail-desc { border-top: 1px solid var(--warm-border); padding-top: var(--space-md); }
.detail-desc h5 { font-family: var(--font-display); font-size: 1rem; font-weight: 400; margin-bottom: var(--space-sm); }
.detail-desc p { font-size: 0.875rem; color: var(--warm-gray); line-height: 1.7; }
.detail-side { width: 220px; flex-shrink: 0; }
.side-card { background: var(--warm-black); padding: var(--space-lg); }
.side-stat { margin-bottom: var(--space-md); }
.side-stat-num { display: block; font-family: var(--font-display); font-size: 2rem; color: var(--surface); line-height: 1.1; }
.side-stat-num.green { color: #7AB07A; }
.side-stat-num.red { color: var(--red-earth); }
.side-stat-label { font-size: 0.6875rem; text-transform: uppercase; letter-spacing: 0.1em; color: rgba(250, 248, 244, 0.5); }
.btn-block { width: 100%; margin-top: var(--space-md); }

/* Issue 5: Responsive */
@media (max-width: 768px) {
  .detail-layout { flex-direction: column; }
  .detail-side { width: 100%; }
  .detail-meta { grid-template-columns: 1fr; }
}
</style>
