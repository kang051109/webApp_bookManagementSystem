<template>
  <div>
    <div class="breadcrumb"><router-link to="/">Home</router-link><span>/</span><span>User Management</span></div>
    <div class="page-header"><h3>User Management</h3></div>
    <div v-if="loading" class="skeleton-table"><div class="shead"><div class="sc" style="width:80px"></div></div><div class="skeleton-row" v-for="n in 3"><div class="skeleton-cell" style="width:25%"></div><div class="skeleton-cell" style="width:25%"></div><div class="skeleton-cell" style="width:20%"></div><div class="skeleton-cell" style="width:10%"></div></div></div>
    <div v-else-if="users.length>0" class="table-scroll"><div class="table-wrap"><table class="data-table"><thead><tr><th>ID</th><th>UserUsername</th><th>姓Username</th><th>Email</th><th>Role</th><th>Actions</th></tr></thead><tbody>
      <tr v-for="u in users" :key="u.id"><td>{{u.id}}</td><td style="font-weight:600">{{u.username}}</td><td>{{u.fullName}}</td><td style="font-size:0.8125rem;color:var(--warm-gray)">{{u.email}}</td><td><span :class="u.role==='admin'?'admin-tag':''">{{u.role==='admin'?'Admin':'User'}}</span></td>
        <td><button v-if="u.role!=='admin'" class="btn btn-sm btn-danger" @click="confirmDel(u)">Delete</button><span v-else style="color:var(--warm-gray-light);font-size:0.75rem">-</span></td></tr></tbody></table></div></div>
    <div v-else class="empty-state"><div class="empty-title">暂无User</div></div>
    <div class="pagination" v-if="totalPages>1">
      <button class="page-btn" :disabled="page<=1" @click="goPage(page-1)">Prev</button>
      <span class="page-info">{{page}}/{{totalPages}} ({{total}}人)</span>
      <button class="page-btn" :disabled="page>=totalPages" @click="goPage(page+1)">Next</button>
    </div>
    <div class="modal-overlay" v-if="showDel" @click.self="showDel=false"><div class="modal modal-sm"><div class="modal-header"><h4>Confirm Delete</h4><button class="modal-close" @click="showDel=false">&times;</button></div><div class="modal-body"><p>DeleteUser <span class="delete-target-name">{{delTarget?.username}}</span>？</p><div class="modal-actions"><button class="btn btn-outline" @click="showDel=false">Cancel</button><button class="btn btn-danger" @click="handleDel" :disabled="delLoading">{{delLoading?'Deleting...':'Confirm'}}</button></div></div></div></div>
  </div>
</template>

<script>
import api from '../services/api.js'
import { showToast } from '../services/toast.js'
export default {
  name:'UserManage',
  data(){return{users:[],page:1,size:10,total:0,totalPages:0,loading:false,showDel:false,delTarget:null,delLoading:false}},
  async mounted(){await this.fetch()},
  methods:{
    async fetch(){this.loading=true;try{const r=await api.get('/admin/users',{params:{page:this.page,size:this.size}});if(r.code===200&&r.data){this.users=r.data.users||[];this.total=r.data.total;this.totalPages=r.data.totalPages}}catch{}finally{this.loading=false}},
    goPage(p){this.page=p;this.fetch()},
    confirmDel(u){this.delTarget=u;this.showDel=true},
    async handleDel(){this.delLoading=true;try{const r=await api.delete(`/admin/users/${this.delTarget.id}`);if(r.code===200){this.showDel=false;this.delTarget=null;await this.fetch();showToast('User deleted')}else showToast(r.message,'error')}catch(e){showToast(e.message,'error')}finally{this.delLoading=false}}
  }
}
</script>
<style scoped>
.admin-tag{background:var(--ochre);color:var(--warm-black);padding:2px 8px;font-size:0.6875rem;font-weight:700;letter-spacing:0.05em}
</style>
