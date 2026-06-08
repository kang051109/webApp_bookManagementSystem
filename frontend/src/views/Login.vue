<template>
  <div class="auth-page">
    <div class="auth-panel">
      <div class="auth-brand">
        <img :src="bgImg" alt="" class="brand-bg-img" />
        <div class="brand-overlay"></div>
        <div class="brand-block">
          <span class="brand-label">Book Management</span>
          <h1 class="brand-title">图书<br/>管理<br/>系统</h1>
          <p class="brand-desc">登录以管理图书库存与借阅</p>
        </div>
      </div>
      <div class="auth-form-wrap">
        <h2 class="form-heading">登录</h2>
        <form @submit.prevent="handleLogin">
          <div class="field"><input v-model="username" type="text" placeholder="用户名" required :disabled="loading" /></div>
          <div class="field"><input v-model="password" type="password" placeholder="密码" required :disabled="loading" /></div>
          <div v-if="error" class="error-message">{{ error }}</div>
          <div v-if="success" class="success-message">{{ success }}</div>
          <button type="submit" class="btn btn-primary btn-block" :disabled="loading">{{ loading ? '登录中...' : '登 录' }}</button>
        </form>
        <p class="auth-switch">还没有账号？<router-link to="/register">注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api.js'
import bgImg from '../assets/library-bg.png'

export default {
  name: 'Login',
  data() { return { username: '', password: '', error: '', success: '', loading: false, bgImg } },
  methods: {
    async handleLogin() {
      this.error = ''; this.success = ''; this.loading = true
      try { const res = await api.post('/auth/login', { username: this.username, password: this.password }); if (res.code === 200) { this.success = '登录成功！'; setTimeout(() => { this.$router.push('/dashboard') }, 500) } else { this.error = res.message || '登录失败' } }
      catch (err) { this.error = err.message || '登录失败，请重试' } finally { this.loading = false }
    }
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: stretch; background: var(--paper); }
.auth-panel { display: flex; width: 100%; min-height: 100vh; }

/* ─── Left brand panel ─── */
.auth-brand {
  flex: 1; min-width: 0;
  display: flex; align-items: center; justify-content: flex-end;
  padding: var(--space-2xl); position: relative; overflow: hidden;
  background: #3E2C1B;
}

/* Image fills the entire panel */
.brand-bg-img {
  position: absolute; inset: 0;
  width: 100%; height: 100%;
  object-fit: cover;
  object-position: center right;
  z-index: 0;
}

/* Dark overlay */
.brand-overlay {
  position: absolute; inset: 0;
  background: rgba(40, 25, 15, 0.5);
  z-index: 1;
}

/* Text sits above everything */
.brand-block {
  max-width: 280px; margin-right: 40px;
  position: relative; z-index: 2;
}
.brand-label { font-family: var(--font-body); font-size: 0.6875rem; text-transform: uppercase; letter-spacing: 0.15em; color: var(--copper); display: block; margin-bottom: var(--space-md); }
.brand-title { font-family: var(--font-display); font-size: 3rem; line-height: 1.1; color: var(--surface); font-weight: 400; margin-bottom: var(--space-lg); }
.brand-desc { font-family: var(--font-body); font-size: 0.8125rem; color: rgba(250, 248, 244, 0.5); line-height: 1.6; }

/* ─── Right form panel ─── */
.auth-form-wrap {
  width: 420px; padding: var(--space-2xl); display: flex; flex-direction: column; justify-content: center;
  position: relative; margin-left: -30px; background: var(--surface); box-shadow: -4px 0 20px rgba(44, 36, 22, 0.06); z-index: 3;
}
.form-heading { font-family: var(--font-display); font-size: 1.75rem; font-weight: 400; color: var(--warm-black); margin-bottom: var(--space-xl); letter-spacing: 0.02em; }
.field { margin-bottom: 16px; }
.field input { width: 100%; padding: 12px 14px; border: 1px solid var(--warm-border); font-family: var(--font-body); font-size: 0.875rem; background: #fff; outline: none; transition: border-color 0.2s; color: var(--warm-black); }
.field input:focus { border-color: var(--copper); box-shadow: inset 0 0 0 1px var(--copper); }
.field input::placeholder { color: var(--warm-gray-light); }
.btn-block { width: 100%; padding: 12px; margin-top: 4px; }
.auth-switch { text-align: center; margin-top: var(--space-lg); font-size: 0.8125rem; color: var(--warm-gray); }
.auth-switch a { color: var(--copper); font-weight: 600; }

@media (max-width: 768px) {
  .auth-panel { flex-direction: column; }
  .auth-brand { display: none; }
  .auth-form-wrap { width: 100%; margin-left: 0; margin-top: 0; padding: var(--space-xl); }
  .auth-page { align-items: flex-start; }
}
</style>
