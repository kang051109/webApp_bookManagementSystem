<template>
  <div>
    <div class="breadcrumb"><router-link to="/">Home</router-link><span>/</span><span>Categories</span></div>
    <div class="page-header">
      <h3>Categories</h3>
      <button v-if="authStore.isAdmin" class="btn btn-primary btn-sm" @click="openAddModal">+ + New</button>
    </div>

    <div v-if="loading" class="skeleton-table">
      <div class="shead"><div class="sc" style="width:80px;"></div></div>
      <div class="skeleton-row" v-for="n in 3" :key="n">
        <div class="skeleton-cell" style="width:25%;"></div>
        <div class="skeleton-cell" style="width:45%;"></div>
      </div>
    </div>

    <div v-else-if="categories.length > 0" class="table-scroll">
      <div class="table-wrap">
        <table class="data-table">
          <thead><tr><th>Category Name</th><th>Description</th><th v-if="authStore.isAdmin">Actions</th></tr></thead>
          <tbody>
            <tr v-for="cat in categories" :key="cat.id">
              <td style="font-weight:600;">{{ cat.name }}</td>
              <td style="color:var(--warm-gray);font-size:0.8125rem;">{{ cat.description || '-' }}</td>
              <td v-if="authStore.isAdmin">
                <button class="btn btn-sm btn-outline" @click="openEditModal(cat)">Edit</button>
                <span class="btn-sep"></span>
                <button class="btn btn-sm btn-danger" @click="confirmDelete(cat)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-else class="empty-state">
      <svg class="empty-icon" width="64" height="64" viewBox="0 0 64 64" fill="none">
        <rect x="10" y="14" width="44" height="36" rx="2" stroke="#C4BAAE" stroke-width="1.5" fill="#FAF8F4"/>
        <line x1="20" y1="26" x2="44" y2="26" stroke="#E2D8CC" stroke-width="1.5"/>
        <line x1="20" y1="34" x2="38" y2="34" stroke="#E2D8CC" stroke-width="1.5"/>
        <line x1="20" y1="42" x2="34" y2="42" stroke="#E2D8CC" stroke-width="1.5"/>
        <circle cx="48" cy="10" r="6" stroke="#C4BAAE" stroke-width="1.5" fill="#FAF8F4"/>
        <line x1="48" y1="8" x2="48" y2="12" stroke="#E2D8CC" stroke-width="1.5"/>
        <line x1="46" y1="10" x2="50" y2="10" stroke="#E2D8CC" stroke-width="1.5"/>
      </svg>
      <div class="empty-title">No categories</div>
      <div class="empty-desc">No categories created yet</div>
      <button v-if="authStore.isAdmin" class="btn btn-primary btn-sm empty-action" @click="openAddModal">+ NewCategory</button>
    </div>

    <!-- Add/Edit modal -->
    <div class="modal-overlay" v-if="showModal" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header"><h4>{{ isEditing ? 'EditCategory' : '+ NewCategory' }}</h4><button class="modal-close" @click="closeModal">&times;</button></div>
        <form @submit.prevent="handleSubmit" class="modal-body">
          <div class="form-group"><label>Category Name</label><input v-model="form.name" type="text" placeholder="Enter Category Name" required /></div>
          <div class="form-group"><label>Description（可选）</label><textarea v-model="form.description" placeholder="Enter CategoryDescription" rows="3"></textarea></div>
          <div v-if="formError" class="error-message">{{ formError }}</div>
          <div class="modal-actions"><button type="button" class="btn btn-outline" @click="closeModal">Cancel</button><button type="submit" class="btn btn-primary" :disabled="formLoading">{{ formLoading ? 'Submitting...' : 'Confirm' }}</button></div>
        </form>
      </div>
    </div>

    <!-- Delete confirm -->
    <div class="modal-overlay" v-if="showDeleteConfirm" @click.self="showDeleteConfirm = false">
      <div class="modal modal-sm">
        <div class="modal-header"><h4>Confirm Delete</h4><button class="modal-close" @click="showDeleteConfirm = false">&times;</button></div>
        <div class="modal-body">
          <p style="font-size:0.875rem;">DeleteCategory<span class="delete-target-name">{{ deleteTarget?.name }}</span>？</p>
          <p style="font-size:0.8125rem;color:var(--warm-gray);margin-top:6px;">有关联图书时将无法Delete。</p>
          <div v-if="formError" class="error-message" style="margin-top:12px;">{{ formError }}</div>
          <div class="modal-actions"><button class="btn btn-outline" @click="showDeleteConfirm = false">Cancel</button><button class="btn btn-danger" @click="handleDelete" :disabled="formLoading">{{ formLoading ? 'Deleting...' : 'Confirm Delete' }}</button></div>
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
  name: 'CategoryManage',
  data() { return { authStore, categories: [], loading: false, showModal: false, isEditing: false, editingId: null, form: { name: '', description: '' }, formError: '', formLoading: false, showDeleteConfirm: false, deleteTarget: null } },
  async mounted() { await this.fetchCategories() },
  methods: {
    async fetchCategories() {
      this.loading = true
      try { const res = await api.get('/categories'); if (res.code === 200 && res.data) this.categories = res.data.categories || [] }
      catch (err) { alert('Failed to load categories: ' + err.message) } finally { this.loading = false }
    },
    openAddModal() { this.isEditing = false; this.editingId = null; this.form = { name: '', description: '' }; this.formError = ''; this.showModal = true },
    openEditModal(cat) { this.isEditing = true; this.editingId = cat.id; this.form = { name: cat.name, description: cat.description || '' }; this.formError = ''; this.showModal = true },
    closeModal() { this.showModal = false; this.formError = '' },
    async handleSubmit() {
      this.formError = ''; if (!this.form.name.trim()) { this.formError = 'Category name is required'; return }
      this.formLoading = true
      try { const res = this.isEditing ? await api.put(`/categories/${this.editingId}`, this.form) : await api.post('/categories', this.form); if (res.code === 200) { this.closeModal(); await this.fetchCategories(); showToast(this.isEditing ? 'Category updated' : 'Category created') } else { showToast(res.message || 'Operation failed', 'error') } }
      catch (err) { showToast(err.message || 'Operation failed', 'error') } finally { this.formLoading = false }
    },
    confirmDelete(cat) { this.deleteTarget = cat; this.formError = ''; this.showDeleteConfirm = true },
    async handleDelete() {
      this.formError = ''; this.formLoading = true
      try { const res = await api.delete(`/categories/${this.deleteTarget.id}`); if (res.code === 200) { this.showDeleteConfirm = false; this.deleteTarget = null; await this.fetchCategories(); showToast('Category已Delete') } else { showToast(res.message || 'DeleteFailed', 'error') } }
      catch (err) { showToast(err.message || 'DeleteFailed', 'error') } finally { this.formLoading = false }
    }
  }
}
</script>
