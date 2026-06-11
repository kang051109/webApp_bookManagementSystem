<template>
  <div>
    <div class="breadcrumb"><router-link to="/books">Books</router-link><span>/</span><span>{{ isEdit ? 'Edit' : '+ New' }}</span></div>
    <div class="page-header">
      <h3>{{ isEdit ? 'Edit图书' : 'New Book' }}</h3>
      <button class="btn btn-outline btn-sm" @click="$router.push('/books')">&larr; Back</button>
    </div>

    <!-- Issue 6: Consistent decoration -->
    <div class="form-card">
      <form @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="form-group"><label>ISBN</label><input v-model="form.isbn" type="text" placeholder="978-7-xxx" required :disabled="loading" /></div>
          <div class="form-group"><label>Category</label><select v-model="form.categoryId"><option value="">Select</option><option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option></select></div>
        </div>
        <div class="form-group"><label>Title</label><input v-model="form.title" type="text" placeholder="Enter Title" required :disabled="loading" /></div>
        <div class="form-row">
          <div class="form-group"><label>Author</label><input v-model="form.author" type="text" placeholder="Enter Author" required :disabled="loading" /></div>
          <div class="form-group"><label>Publisher</label><input v-model="form.publisher" type="text" placeholder="Enter Publisher" :disabled="loading" /></div>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Publish Year</label><input v-model="form.publishYear" type="number" placeholder="2024" min="1000" max="2099" :disabled="loading" /></div>
        </div>
        <div class="form-row" v-if="isEdit">
          <div class="form-group"><label>Total Copies</label><input v-model.number="form.totalCopies" type="number" min="1" placeholder="Stock量" required :disabled="loading" /></div>
          <div class="form-group"><label>Available数量</label><input v-model.number="form.availableCopies" type="number" min="0" placeholder="Available数量" :disabled="loading" /><small style="color:var(--warm-gray);font-size:0.6875rem;">已借出 {{ borrowedHint }} copies</small></div>
        </div>
        <div class="form-row" v-else>
          <div class="form-group"><label>Total Copies</label><input v-model.number="form.totalCopies" type="number" min="1" placeholder="Stock量" required :disabled="loading" /></div>
        </div>
        <div class="form-group"><label>Description</label><textarea v-model="form.description" rows="4" placeholder="图书Description" :disabled="loading"></textarea></div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="success" class="success-message">{{ success }}</div>
        <div class="form-actions">
          <button type="button" class="btn btn-outline" @click="$router.push('/books')">Cancel</button>
          <button type="submit" class="btn btn-primary" :disabled="loading">{{ loading ? 'Submitting...' : (isEdit ? 'Save' : 'Confirm') }}</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import api from '../services/api.js'
import { showToast } from '../services/toast.js'
export default {
  name: 'BookForm',
  data() {
    return {
      isEdit: false, categories: [],
      form: { isbn: '', title: '', author: '', publisher: '', publishYear: '', categoryId: '', totalCopies: 1, availableCopies: null, description: '' },
      borrowedHint: 0,
      loading: false, error: '', success: ''
    }
  },
  async mounted() { this.isEdit = !!this.$route.params.id; await this.loadCategories(); if (this.isEdit) await this.loadBook() },
  methods: {
    async loadCategories() { try { const res = await api.get('/categories'); if (res.code === 200 && res.data) this.categories = res.data.categories || [] } catch {} },
    async loadBook() { try { const res = await api.get(`/books/${this.$route.params.id}`); if (res.code === 200 && res.data?.book) { const b = res.data.book; const av = b.availableCopies != null ? b.availableCopies : b.totalCopies; const tot = b.totalCopies || 1; this.form = { isbn: b.isbn || '', title: b.title || '', author: b.author || '', publisher: b.publisher || '', publishYear: b.publishYear || '', categoryId: b.categoryId || '', totalCopies: tot, availableCopies: av, description: b.description || '' }; this.borrowedHint = tot - av; } } catch { this.error = 'Load failed' } },
    async handleSubmit() {
      this.error = ''; this.success = ''; this.loading = true
      const payload = { isbn: this.form.isbn, title: this.form.title, author: this.form.author, publisher: this.form.publisher || null, publishYear: this.form.publishYear ? parseInt(this.form.publishYear) : null, categoryId: this.form.categoryId ? parseInt(this.form.categoryId) : null, totalCopies: parseInt(this.form.totalCopies) || 1, availableCopies: this.isEdit && this.form.availableCopies != null ? parseInt(this.form.availableCopies) : null, description: this.form.description || null }
      try { const res = this.isEdit ? await api.put(`/books/${this.$route.params.id}`, payload) : await api.post('/books', payload); if (res.code === 200) { showToast(this.isEdit ? 'Book updated' : 'Book created'); setTimeout(() => { this.$router.push('/books') }, 500) } else { this.error = res.message || 'Operation failed' } }
      catch (err) { this.error = err.message || 'Operation failed' } finally { this.loading = false }
    }
  }
}
</script>

<style scoped>
/* Issue 6: Consistent decorative line */
.form-card {
  background: var(--surface); border: 1px solid var(--warm-border);
  padding: var(--space-xl); max-width: 680px;
  position: relative;
}
.form-card::before {
  content: ''; position: absolute; top: 0; left: 0;
  width: 3px; height: 48px; background: var(--copper);
}
.form-row { display: flex; gap: 16px; }
.form-row .form-group { flex: 1; }
.form-actions { display: flex; justify-content: flex-end; gap: 10px; padding-top: 8px; }

@media (max-width: 768px) { .form-row { flex-direction: column; gap: 0; } }
</style>
