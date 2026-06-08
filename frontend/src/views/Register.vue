<template>
  <div class="auth-page">
    <div class="auth-panel">
      <div class="auth-form-wrap">
        <h2 class="form-heading">创建账号</h2>
        <form @submit.prevent="handleRegister">
          <div class="field"><input v-model="username" type="text" placeholder="用户名 (3-50个字符)" required :disabled="loading" /></div>
          <div class="field"><input v-model="password" type="password" placeholder="密码 (至少6位)" required :disabled="loading" /></div>
          <div class="field"><input v-model="email" type="email" placeholder="邮箱" required :disabled="loading" /></div>
          <div class="field"><input v-model="fullName" type="text" placeholder="姓名" required :disabled="loading" /></div>
          <div v-if="error" class="error-message">{{ error }}</div>
          <button type="submit" class="btn btn-primary btn-block" :disabled="loading">{{ loading ? '注册中...' : '注 册' }}</button>
        </form>
        <p class="auth-switch">已有账号？<router-link to="/login">返回登录</router-link></p>
      </div>
      <div class="auth-brand">
        <div class="brand-block">
          <span class="brand-label">Join Us</span>
          <h1 class="brand-title">创建<br/>你的<br/>账号</h1>
          <p class="brand-desc">注册后即可浏览图书、借阅与管理</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api.js'
import { showToast } from '../services/toast.js'

export default {
  name: 'Register',
  data() { return { username: '', password: '', email: '', fullName: '', error: '', loading: false } },
  methods: {
    async handleRegister() {
      this.error = ''; this.loading = true
      try {
        const res = await api.post('/auth/register', { username: this.username, password: this.password, email: this.email, fullName: this.fullName })
        if (res.code === 200) { showToast('注册成功'); setTimeout(() => { this.$router.push('/login') }, 1000) }
        else { this.error = res.message || '注册失败' }
      } catch (err) { this.error = err.message || '注册失败，请重试' } finally { this.loading = false }
    }
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: stretch; background: var(--paper); }
.auth-panel { display: flex; width: 100%; min-height: 100vh; }

.auth-form-wrap {
  width: 420px; padding: var(--space-2xl); display: flex; flex-direction: column; justify-content: center;
  background: var(--surface); margin-right: -30px; position: relative; z-index: 1;
  box-shadow: 4px 0 20px rgba(44, 36, 22, 0.06);
}
.form-heading { font-family: var(--font-display); font-size: 1.75rem; font-weight: 400; color: var(--warm-black); margin-bottom: var(--space-xl); letter-spacing: 0.02em; }
.field { margin-bottom: 14px; }
.field input { width: 100%; padding: 11px 14px; border: 1px solid var(--warm-border); font-family: var(--font-body); font-size: 0.875rem; background: #fff; outline: none; transition: border-color 0.2s; color: var(--warm-black); }
.field input:focus { border-color: var(--copper); box-shadow: inset 0 0 0 1px var(--copper); }
.field input::placeholder { color: var(--warm-gray-light); }
.btn-block { width: 100%; padding: 12px; margin-top: 4px; }
.auth-switch { text-align: center; margin-top: var(--space-lg); font-size: 0.8125rem; color: var(--warm-gray); }
.auth-switch a { color: var(--copper); font-weight: 600; }

.auth-brand {
  flex: 1; background: var(--warm-black); display: flex; align-items: center;
  padding: var(--space-2xl); position: relative; overflow: hidden;
}
.auth-brand::after { content: ''; position: absolute; bottom: -30px; left: -30px; width: 180px; height: 180px; border: 1px solid rgba(250, 248, 244, 0.05); border-radius: 50%; }
.brand-block { max-width: 280px; margin-left: 60px; position: relative; }
.brand-label { font-family: var(--font-body); font-size: 0.6875rem; text-transform: uppercase; letter-spacing: 0.15em; color: var(--copper); display: block; margin-bottom: var(--space-md); }
.brand-title { font-family: var(--font-display); font-size: 3rem; line-height: 1.1; color: var(--surface); font-weight: 400; margin-bottom: var(--space-lg); }
.brand-desc { font-family: var(--font-body); font-size: 0.8125rem; color: rgba(250, 248, 244, 0.5); line-height: 1.6; }

@media (max-width: 768px) {
  .auth-panel { flex-direction: column; }
  .auth-brand { display: none; }
  .auth-form-wrap { width: 100%; margin-right: 0; padding: var(--space-xl); }
}
</style>
