<template>
  <div>
    <div class="breadcrumb"><router-link to="/">首页</router-link><span>/</span><span>分类管理</span></div>
    <div class="page-header">
      <h3>分类管理</h3>
      <button v-if="authStore.isAdmin" class="btn btn-primary btn-sm" @click="openAddModal">+ 新增</button>
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
          <thead><tr><th>分类名称</th><th>描述</th><th v-if="authStore.isAdmin">操作</th></tr></thead>
          <tbody>
            <tr v-for="cat in categories" :key="cat.id">
              <td style="font-weight:600;">{{ cat.name }}</td>
              <td style="color:var(--warm-gray);font-size:0.8125rem;">{{ cat.description || '-' }}</td>
              <td v-if="authStore.isAdmin">
                <button class="btn btn-sm btn-outline" @click="openEditModal(cat)">编辑</button>
                <span class="btn-sep"></span>
                <button class="btn btn-sm btn-danger" @click="confirmDelete(cat)">删除</button>
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
      <div class="empty-title">暂无分类</div>
      <div class="empty-desc">还没有创建任何图书分类</div>
      <button v-if="authStore.isAdmin" class="btn btn-primary btn-sm empty-action" @click="openAddModal">新增分类</button>
    </div>

    <!-- Add/Edit modal -->
    <div class="modal-overlay" v-if="showModal" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header"><h4>{{ isEditing ? '编辑分类' : '新增分类' }}</h4><button class="modal-close" @click="closeModal">&times;</button></div>
        <form @submit.prevent="handleSubmit" class="modal-body">
          <div class="form-group"><label>分类名称</label><input v-model="form.name" type="text" placeholder="请输入分类名称" required /></div>
          <div class="form-group"><label>描述（可选）</label><textarea v-model="form.description" placeholder="请输入分类描述" rows="3"></textarea></div>
          <div v-if="formError" class="error-message">{{ formError }}</div>
          <div class="modal-actions"><button type="button" class="btn btn-outline" @click="closeModal">取消</button><button type="submit" class="btn btn-primary" :disabled="formLoading">{{ formLoading ? '提交中...' : '确认' }}</button></div>
        </form>
      </div>
    </div>

    <!-- Delete confirm -->
    <div class="modal-overlay" v-if="showDeleteConfirm" @click.self="showDeleteConfirm = false">
      <div class="modal modal-sm">
        <div class="modal-header"><h4>确认删除</h4><button class="modal-close" @click="showDeleteConfirm = false">&times;</button></div>
        <div class="modal-body">
          <p style="font-size:0.875rem;">删除分类<span class="delete-target-name">{{ deleteTarget?.name }}</span>？</p>
          <p style="font-size:0.8125rem;color:var(--warm-gray);margin-top:6px;">有关联图书时将无法删除。</p>
          <div v-if="formError" class="error-message" style="margin-top:12px;">{{ formError }}</div>
          <div class="modal-actions"><button class="btn btn-outline" @click="showDeleteConfirm = false">取消</button><button class="btn btn-danger" @click="handleDelete" :disabled="formLoading">{{ formLoading ? '删除中...' : '确认删除' }}</button></div>
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
      catch (err) { alert('获取分类列表失败: ' + err.message) } finally { this.loading = false }
    },
    openAddModal() { this.isEditing = false; this.editingId = null; this.form = { name: '', description: '' }; this.formError = ''; this.showModal = true },
    openEditModal(cat) { this.isEditing = true; this.editingId = cat.id; this.form = { name: cat.name, description: cat.description || '' }; this.formError = ''; this.showModal = true },
    closeModal() { this.showModal = false; this.formError = '' },
    async handleSubmit() {
      this.formError = ''; if (!this.form.name.trim()) { this.formError = '分类名称不能为空'; return }
      this.formLoading = true
      try { const res = this.isEditing ? await api.put(`/categories/${this.editingId}`, this.form) : await api.post('/categories', this.form); if (res.code === 200) { this.closeModal(); await this.fetchCategories(); showToast(this.isEditing ? '分类已更新' : '分类已创建') } else { showToast(res.message || '操作失败', 'error') } }
      catch (err) { showToast(err.message || '操作失败', 'error') } finally { this.formLoading = false }
    },
    confirmDelete(cat) { this.deleteTarget = cat; this.formError = ''; this.showDeleteConfirm = true },
    async handleDelete() {
      this.formError = ''; this.formLoading = true
      try { const res = await api.delete(`/categories/${this.deleteTarget.id}`); if (res.code === 200) { this.showDeleteConfirm = false; this.deleteTarget = null; await this.fetchCategories(); showToast('分类已删除') } else { showToast(res.message || '删除失败', 'error') } }
      catch (err) { showToast(err.message || '删除失败', 'error') } finally { this.formLoading = false }
    }
  }
}
</script>
