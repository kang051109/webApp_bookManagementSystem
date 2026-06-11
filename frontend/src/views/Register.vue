<template>
  <div class="auth-page">
    <div class="auth-panel">
      <div class="auth-form-wrap">
        <h2 class="form-heading">Create Account</h2>
        <form @submit.prevent="handleRegister">
          <div class="field"><input v-model="username" type="text" placeholder="UserUsername (3-50))" required :disabled="loading" /></div>
          <div class="field"><input v-model="password" type="password" placeholder="Password (At least 6位)" required :disabled="loading" /></div>
          <div class="field"><input v-model="email" type="email" placeholder="Email" required :disabled="loading" /></div>
          <div class="field"><input v-model="fullName" type="text" placeholder="姓Username" required :disabled="loading" /></div>
          <div v-if="error" class="error-message">{{ error }}</div>
          <button type="submit" class="btn btn-primary btn-block" :disabled="loading">{{ loading ? 'Registering...' : 'Register' }}</button>
        </form>
        <p class="auth-switch">Already have an account? <router-link to="/login">Back to Login</router-link></p>
      </div>
      <div class="auth-brand">
        <img :src="bgImg" alt="" class="brand-bg-img" />
        <div class="brand-overlay"></div>
        <div class="brand-block">
          <span class="brand-label">Join Us</span>
          <h1 class="brand-title">创建<br/>你的<br/>account</h1>
          <p class="brand-desc">Register to browse, borrow & manage</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api.js'
import { showToast } from '../services/toast.js'
import bgImg from '../assets/library-bg.png'

export default {
  name: 'Register',
  data() { return { username: '', password: '', email: '', fullName: '', error: '', loading: false, bgImg } },
  methods: {
    async handleRegister() {
      this.error = ''; this.loading = true
      try {
        const res = await api.post('/auth/register', { username: this.username, password: this.password, email: this.email, fullName: this.fullName })
        if (res.code === 200) { showToast('Registration successful'); setTimeout(() => { this.$router.push('/login') }, 1000) }
        else { this.error = res.message || 'RegisterFailed' }
      } catch (err) { this.error = err.message || 'RegisterFailed，请重试' } finally { this.loading = false }
    }
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: stretch; background: var(--paper); }
.auth-panel { display: flex; width: 100%; min-height: 100vh; }

/* ─── Left form panel ─── */
.auth-form-wrap {
  width: 420px; padding: var(--space-2xl); display: flex; flex-direction: column; justify-content: center;
  position: relative; margin-right: -30px; background: var(--surface); box-shadow: 4px 0 20px rgba(44, 36, 22, 0.06); z-index: 3;
}
.form-heading { font-family: var(--font-display); font-size: 1.75rem; font-weight: 400; color: var(--warm-black); margin-bottom: var(--space-xl); letter-spacing: 0.02em; }
.field { margin-bottom: 14px; }
.field input { width: 100%; padding: 11px 14px; border: 1px solid var(--warm-border); font-family: var(--font-body); font-size: 0.875rem; background: #fff; outline: none; transition: border-color 0.2s; color: var(--warm-black); }
.field input:focus { border-color: var(--copper); box-shadow: inset 0 0 0 1px var(--copper); }
.field input::placeholder { color: var(--warm-gray-light); }
.btn-block { width: 100%; padding: 12px; margin-top: 4px; }
.auth-switch { text-align: center; margin-top: var(--space-lg); font-size: 0.8125rem; color: var(--warm-gray); }
.auth-switch a { color: var(--copper); font-weight: 600; }

/* ─── Right brand panel (mirror of Login) ─── */
.auth-brand {
  flex: 1; min-width: 0;
  display: flex; align-items: center;
  padding: var(--space-2xl); position: relative; overflow: hidden;
}

.brand-bg-img {
  position: absolute; inset: 0;
  width: 100%; height: 100%;
  object-fit: cover;
  object-position: center left;
  z-index: 0;
}

.brand-overlay {
  position: absolute; inset: 0;
  background: rgba(40, 25, 15, 0.5);
  z-index: 1;
}

.brand-block { max-width: 280px; margin-left: 60px; position: relative; z-index: 2; }
.brand-label { font-family: var(--font-body); font-size: 0.6875rem; text-transform: uppercase; letter-spacing: 0.15em; color: var(--copper); display: block; margin-bottom: var(--space-md); }
.brand-title { font-family: var(--font-display); font-size: 3rem; line-height: 1.1; color: var(--surface); font-weight: 400; margin-bottom: var(--space-lg); }
.brand-desc { font-family: var(--font-body); font-size: 0.8125rem; color: rgba(250, 248, 244, 0.5); line-height: 1.6; }

@media (max-width: 768px) {
  .auth-panel { flex-direction: column; }
  .auth-brand { display: none; }
  .auth-form-wrap { width: 100%; margin-right: 0; padding: var(--space-xl); }
}
</style>
