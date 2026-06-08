<template>
  <div>
    <div class="breadcrumb"><router-link to="/books">图书管理</router-link><span>/</span><span>{{ isEdit ? '编辑' : '新增' }}</span></div>
    <div class="page-header">
      <h3>{{ isEdit ? '编辑图书' : '新增图书' }}</h3>
      <button class="btn btn-outline btn-sm" @click="$router.push('/books')">&larr; 返回</button>
    </div>

    <!-- Issue 6: Consistent decoration -->
    <div class="form-card">
      <form @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="form-group"><label>ISBN</label><input v-model="form.isbn" type="text" placeholder="978-7-xxx" required :disabled="loading" /></div>
          <div class="form-group"><label>分类</label><select v-model="form.categoryId"><option value="">请选择</option><option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option></select></div>
        </div>
        <div class="form-group"><label>书名</label><input v-model="form.title" type="text" placeholder="请输入书名" required :disabled="loading" /></div>
        <div class="form-row">
          <div class="form-group"><label>作者</label><input v-model="form.author" type="text" placeholder="请输入作者" required :disabled="loading" /></div>
          <div class="form-group"><label>出版社</label><input v-model="form.publisher" type="text" placeholder="请输入出版社" :disabled="loading" /></div>
        </div>
        <div class="form-row">
          <div class="form-group"><label>出版年份</label><input v-model="form.publishYear" type="number" placeholder="2024" min="1000" max="2099" :disabled="loading" /></div>
          <div class="form-group"><label>总库存</label><input v-model.number="form.totalCopies" type="number" min="1" placeholder="库存量" required :disabled="loading" /></div>
        </div>
        <div class="form-group"><label>描述</label><textarea v-model="form.description" rows="4" placeholder="图书简介" :disabled="loading"></textarea></div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="success" class="success-message">{{ success }}</div>
        <div class="form-actions">
          <button type="button" class="btn btn-outline" @click="$router.push('/books')">取消</button>
          <button type="submit" class="btn btn-primary" :disabled="loading">{{ loading ? '提交中...' : (isEdit ? '保存修改' : '确认新增') }}</button>
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
      form: { isbn: '', title: '', author: '', publisher: '', publishYear: '', categoryId: '', totalCopies: 1, description: '' },
      loading: false, error: '', success: ''
    }
  },
  async mounted() { this.isEdit = !!this.$route.params.id; await this.loadCategories(); if (this.isEdit) await this.loadBook() },
  methods: {
    async loadCategories() { try { const res = await api.get('/categories'); if (res.code === 200 && res.data) this.categories = res.data.categories || [] } catch {} },
    async loadBook() { try { const res = await api.get(`/books/${this.$route.params.id}`); if (res.code === 200 && res.data?.book) { const b = res.data.book; this.form = { isbn: b.isbn || '', title: b.title || '', author: b.author || '', publisher: b.publisher || '', publishYear: b.publishYear || '', categoryId: b.categoryId || '', totalCopies: b.totalCopies || 1, description: b.description || '' } } } catch { this.error = '加载失败' } },
    async handleSubmit() {
      this.error = ''; this.success = ''; this.loading = true
      const payload = { isbn: this.form.isbn, title: this.form.title, author: this.form.author, publisher: this.form.publisher || null, publishYear: this.form.publishYear ? parseInt(this.form.publishYear) : null, categoryId: this.form.categoryId ? parseInt(this.form.categoryId) : null, totalCopies: parseInt(this.form.totalCopies) || 1, description: this.form.description || null }
      try { const res = this.isEdit ? await api.put(`/books/${this.$route.params.id}`, payload) : await api.post('/books', payload); if (res.code === 200) { showToast(this.isEdit ? '图书已更新' : '图书已创建'); setTimeout(() => { this.$router.push('/books') }, 500) } else { this.error = res.message || '操作失败' } }
      catch (err) { this.error = err.message || '操作失败' } finally { this.loading = false }
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
