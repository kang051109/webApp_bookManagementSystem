<template>
  <div>
    <div class="breadcrumb"><router-link to="/">Home</router-link><span>/</span><span>Books</span></div>
    <div class="page-header">
      <h3>Books</h3>
      <button v-if="authStore.isAdmin" class="btn btn-primary btn-sm" @click="$router.push('/books/new')">+ + New</button>
    </div>

    <div class="filter-bar">
      <input v-model="keyword" type="text" placeholder="Search by title or author..." class="filter-input" @keyup.enter="search" />
      <select v-model="categoryId" class="filter-select" @change="search">
        <option value="">All Categories</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>
      <button class="btn btn-outline btn-sm" @click="search">Search</button>
      <button class="btn btn-outline btn-sm" @click="cardView=!cardView" :title="cardView?'List View':'Card View'">
        {{ cardView ? '☰' : '▦' }}
      </button>
    </div>

    <!-- Issue 2: Skeleton -->
    <div v-if="loading" class="skeleton-table">
      <div class="shead"><div class="sc" style="width:80px;"></div></div>
      <div class="skeleton-row" v-for="n in 5" :key="n">
        <div class="skeleton-cell" style="width:30%;"></div>
        <div class="skeleton-cell" style="width:15%;"></div>
        <div class="skeleton-cell" style="width:20%;"></div>
        <div class="skeleton-cell" style="width:10%;"></div>
        <div class="skeleton-cell" style="width:8%;"></div>
        <div class="skeleton-cell" style="width:8%;"></div>
      </div>
    </div>

    <!-- Card view -->
    <div v-if="!loading && books.length > 0 && cardView" class="card-grid">
      <div v-for="book in books" :key="book.id" class="book-card" @click="$router.push(`/books/${book.id}`)">
        <div class="bc-cover">{{ book.title.charAt(0) }}</div>
        <span v-if="book.availableCopies === 0" class="bc-soldout">Sold Out</span>
        <div class="bc-body">
          <div class="bc-title">{{ book.title }}</div>
          <div class="bc-author">{{ book.author }}</div>
          <div class="bc-meta">{{ book.categoryName||'-' }} · {{ book.publishYear||'-' }}</div>
          <div class="bc-stock">
            <span class="bc-avail" :class="book.availableCopies>0?'green':'red'">{{ book.availableCopies }} Available</span>
            <span style="color:var(--warm-gray-light);font-size:0.6875rem">/ {{ book.totalCopies }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Table view -->
    <div v-else-if="books.length > 0 && !cardView" class="table-scroll">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>Title</th><th>Author</th><th>ISBN</th><th>Category</th><th style="text-align:center;">Stock</th><th style="text-align:center;">Available</th>
              <th v-if="authStore.isAdmin">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="book in books" :key="book.id">
              <td><router-link :to="`/books/${book.id}`" class="book-link">{{ book.title }}</router-link></td>
              <td style="color:var(--warm-gray);">{{ book.author }}</td>
              <td style="font-size:0.8125rem;color:var(--warm-gray-light);font-family:monospace;">{{ book.isbn }}</td>
              <td>{{ book.categoryName || '-' }}</td>
              <td style="text-align:center;">{{ book.totalCopies }}</td>
              <td style="text-align:center;"><span :class="book.availableCopies > 0 ? 'avail' : 'unavail'">{{ book.availableCopies }}</span></td>
              <td v-if="authStore.isAdmin">
                <button class="btn btn-sm btn-outline" @click="$router.push(`/books/${book.id}/edit`)">Edit</button>
                <span class="btn-sep"></span>
                <button class="btn btn-sm btn-danger" @click="confirmDelete(book)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Issue 3: Empty state -->
    <div v-else class="empty-state">
      <svg class="empty-icon" width="64" height="64" viewBox="0 0 64 64" fill="none">
        <rect x="8" y="20" width="48" height="36" rx="2" stroke="#C4BAAE" stroke-width="1.5" fill="#FAF8F4"/>
        <line x1="16" y1="30" x2="48" y2="30" stroke="#E2D8CC" stroke-width="1.5"/>
        <line x1="16" y1="36" x2="38" y2="36" stroke="#E2D8CC" stroke-width="1.5"/>
        <line x1="16" y1="42" x2="42" y2="42" stroke="#E2D8CC" stroke-width="1.5"/>
        <rect x="24" y="6" width="16" height="14" rx="1" stroke="#C4BAAE" stroke-width="1.5" fill="#FAF8F4"/>
        <line x1="28" y1="50" x2="36" y2="50" stroke="#E2D8CC" stroke-width="1.5"/>
      </svg>
      <div class="empty-title">No books yet</div>
      <div class="empty-desc">No books in inventory</div>
      <button v-if="authStore.isAdmin" class="btn btn-primary btn-sm empty-action" @click="$router.push('/books/new')">New Book</button>
    </div>

    <div class="pagination" v-if="totalPages > 1">
      <button class="page-btn" :disabled="page <= 1" @click="goPage(page - 1)">Prev</button>
      <button class="page-btn" v-for="p in pageRange" :key="p" :class="{ active: p === page, 'active-page': p === page }" @click="goPage(p)">{{ p }}</button>
      <button class="page-btn" :disabled="page >= totalPages" @click="goPage(page + 1)">Next</button>
      <span class="page-info">Total {{ total }} items</span>
    </div>

    <!-- Delete confirm -->
    <div class="modal-overlay" v-if="showDeleteConfirm" @click.self="showDeleteConfirm = false">
      <div class="modal modal-sm">
        <div class="modal-header"><h4>Confirm Delete</h4><button class="modal-close" @click="showDeleteConfirm = false">&times;</button></div>
        <div class="modal-body">
          <p style="font-size:0.875rem;">Delete<span class="delete-target-name">{{ deleteTarget?.title }}</span>？</p>
          <p style="font-size:0.8125rem;color:var(--warm-gray);margin-top:6px;">Cannot delete if there are active borrows。</p>
          <div v-if="formError" class="error-message" style="margin-top:12px;">{{ formError }}</div>
          <div class="modal-actions">
            <button class="btn btn-outline" @click="showDeleteConfirm = false">Cancel</button>
            <button class="btn btn-danger" @click="handleDelete" :disabled="deleteLoading">{{ deleteLoading ? 'Deleting...' : 'Confirm Delete' }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { authStore } from '../services/authStore.js'
import api from '../services/api.js'
import { showToast } from '../services/toast.js'
export default {
  name: 'BookList',
  data() {
    return {
      authStore,
      books: [], categories: [], keyword: '', categoryId: '',
      page: 1, size: 10, total: 0, totalPages: 0, loading: false, cardView: false,
      showDeleteConfirm: false, deleteTarget: null, formError: '', deleteLoading: false
    }
  },
  computed: {
    pageRange() {
      const p = this.page, t = this.totalPages;
      if (t <= 5) return Array.from({length: t}, (_,i) => i+1);
      if (p <= 3) return [1,2,3,4,'...',t];
      if (p >= t-2) return [1,'...',t-3,t-2,t-1,t];
      return [1,'...',p-1,p,p+1,'...',t];
    }
  },
  async mounted() { await this.loadCategories(); await this.search() },
  methods: {
    async loadCategories() { try { const res = await api.get('/categories'); if (res.code === 200 && res.data) this.categories = res.data.categories || [] } catch {} },
    async search() { this.page = 1; await this.fetchBooks() },
    async fetchBooks() {
      this.loading = true
      try {
        const params = { page: this.page, size: this.size }
        if (this.keyword.trim()) params.keyword = this.keyword.trim()
        if (this.categoryId) params.categoryId = this.categoryId
        const res = await api.get('/books', { params })
        if (res.code === 200 && res.data) { this.books = res.data.books || []; this.total = res.data.total || 0; this.totalPages = res.data.totalPages || 0 }
      } catch (err) { alert('Failed to load book list: ' + err.message) } finally { this.loading = false }
    },
    goPage(p) { if (typeof p === 'number') { this.page = p; this.fetchBooks() } },
    confirmDelete(book) { this.deleteTarget = book; this.formError = ''; this.showDeleteConfirm = true },
    async handleDelete() {
      this.formError = ''; this.deleteLoading = true
      try { const res = await api.delete(`/books/${this.deleteTarget.id}`); if (res.code === 200) { this.showDeleteConfirm = false; this.deleteTarget = null; await this.fetchBooks(); showToast('Book Delete') } else { showToast(res.message || 'DeleteFailed', 'error') } }
      catch (err) { showToast(err.message || 'DeleteFailed', 'error') } finally { this.deleteLoading = false }
    }
  }
}
</script>

<style scoped>
.filter-bar { display: flex; gap: 10px; margin-bottom: var(--space-lg); align-items: center; flex-wrap: wrap; }
.filter-input { flex: 1; min-width: 180px; max-width: 260px; padding: 8px 12px; border: 1px solid var(--warm-border); font-family: var(--font-body); font-size: 0.8125rem; background: #fff; outline: none; transition: border-color 0.2s; }
.filter-input:focus { border-color: var(--copper); box-shadow: inset 0 0 0 1px var(--copper); }
.filter-select { padding: 8px 12px; border: 1px solid var(--warm-border); font-family: var(--font-body); font-size: 0.8125rem; background: #fff; outline: none; }
.book-link { color: var(--copper); font-weight: 600; }
.book-link:hover { color: var(--copper-dark); text-decoration: underline; }
.avail { color: var(--green-muted); font-weight: 700; }
.unavail { color: var(--red-earth); font-weight: 700; }

/* Card view */
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.book-card { background: var(--surface); border: 1px solid var(--warm-border); cursor: pointer; transition: all 0.15s; position: relative; }
.book-card:hover { border-color: var(--copper); transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.bc-cover { height: 100px; background: linear-gradient(135deg, var(--copper) 0%, var(--copper-dark) 100%); display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-size: 2.5rem; color: #fff; }
.bc-body { padding: 16px; }
.bc-title { font-weight: 700; font-size: 0.9375rem; margin-bottom: 4px; line-height: 1.3; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.bc-author { color: var(--warm-gray); font-size: 0.8125rem; margin-bottom: 4px; }
.bc-meta { color: var(--warm-gray-light); font-size: 0.6875rem; margin-bottom: 8px; }
.bc-avail { font-weight: 700; font-size: 0.8125rem; }
.bc-avail.green { color: var(--green-muted); }
.bc-avail.red { color: var(--red-earth); }
</style>
