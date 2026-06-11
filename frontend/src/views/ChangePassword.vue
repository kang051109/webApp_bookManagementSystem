<template>
  <div>
    <div class="breadcrumb"><router-link to="/">Home</router-link><span>/</span><span>Change Password</span></div>
    <div class="page-header"><h3>Change Password</h3></div>
    <div class="form-card">
      <form @submit.prevent="handleSubmit">
        <div class="form-group"><label>Old Password</label><input v-model="oldPwd" type="password" required /></div>
        <div class="form-group"><label>New Password</label><input v-model="newPwd" type="password" required /></div>
        <div class="form-group"><label>ConfirmNew Password</label><input v-model="confirmPwd" type="password" required /></div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <button class="btn btn-primary" :disabled="loading">{{ loading?'Submitting...':'Confirm' }}</button>
      </form>
    </div>
  </div>
</template>

<script>
import api from '../services/api.js'
import { showToast } from '../services/toast.js'
export default {
  name:'ChangePassword',
  data(){return{oldPwd:'',newPwd:'',confirmPwd:'',error:'',loading:false}},
  methods:{
    async handleSubmit(){
      this.error=''
      if(this.newPwd !== this.confirmPwd){this.error='两次Password不一致';return}
      this.loading=true
      try{
        const r=await api.post('/auth/change-password',{oldPassword:this.oldPwd,newPassword:this.newPwd})
        if(r.code===200){showToast('Password已修改');this.$router.push('/dashboard')}
        else this.error=r.message
      }catch(e){this.error=e.message}finally{this.loading=false}
    }
  }
}
</script>
<style scoped>
.form-card{background:var(--surface);border:1px solid var(--warm-border);padding:var(--space-xl);max-width:440px;position:relative}
.form-card::before{content:'';position:absolute;top:0;left:0;width:3px;height:48px;background:var(--copper)}
</style>
