<template>
  <div class="layout">
    <header class="layout-header">
      <div class="header-left">
        <button class="menu-toggle" @click="sidebarOpen = !sidebarOpen">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 12h18M3 6h18M3 18h18"/></svg>
        </button>
        <h2>图书管理系统</h2>
      </div>
      <div class="header-right">
        <span class="user-info">{{ currentUser ? currentUser.fullName : '' }}</span>
        <span v-if="isAdmin" class="admin-badge">管理员</span>
        <button class="btn-logout" @click="handleLogout">退出</button>
      </div>
    </header>
    <div class="layout-body">
      <aside class="layout-sidebar" :class="{ open: sidebarOpen }">
        <div class="sidebar-overlay" @click="sidebarOpen = false"></div>
        <nav class="sidebar-nav">
          <router-link to="/dashboard" class="nav-item" @click="sidebarOpen = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="4" rx="1"/></svg>
            仪表板
          </router-link>
          <router-link to="/books" class="nav-item" @click="sidebarOpen = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>
            图书管理
          </router-link>
          <router-link to="/categories" class="nav-item" @click="sidebarOpen = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
            分类管理
          </router-link>
          <router-link to="/my-borrows" class="nav-item" @click="sidebarOpen = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
            我的借阅
          </router-link>
          <router-link v-if="isAdmin" to="/admin/borrows" class="nav-item" @click="sidebarOpen = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><path d="M9 14l2 2 4-4"/></svg>
            全部借阅
          </router-link>
        </nav>
      </aside>
      <main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script>
import api from '../services/api.js'
export default {
  name: 'Layout',
  data() { return { currentUser: null, isAdmin: false, sidebarOpen: false } },
  provide() { return { currentUser: this.currentUser, isAdmin: this.isAdmin, fetchCurrentUser: this.fetchCurrentUser } },
  async mounted() { await this.fetchCurrentUser() },
  methods: {
    async fetchCurrentUser() {
      try { const res = await api.get('/auth/me'); if (res.code === 200 && res.data && res.data.user) { this.currentUser = res.data.user; this.isAdmin = this.currentUser.role === 'admin' } else { this.$router.push('/login') } }
      catch { this.$router.push('/login') }
    },
    async handleLogout() { try { await api.post('/auth/logout') } catch {} this.$router.push('/login') }
  }
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
.layout-header { background: var(--warm-black); color: var(--surface); display: flex; align-items: center; justify-content: space-between; padding: 0 var(--space-lg); height: 52px; flex-shrink: 0; border-bottom: 2px solid var(--copper); }
.header-left { display: flex; align-items: center; gap: 12px; }
.header-left h2 { font-family: var(--font-display); font-size: 1.125rem; font-weight: 400; color: var(--surface); letter-spacing: 0.04em; }
.menu-toggle { display: none; background: none; border: none; color: var(--surface); cursor: pointer; padding: 4px; }
.header-right { display: flex; align-items: center; gap: 14px; }
.user-info { font-size: 0.8125rem; opacity: 0.8; }
.admin-badge { font-size: 0.6875rem; background: var(--ochre); color: var(--warm-black); padding: 2px 8px; font-weight: 700; letter-spacing: 0.05em; }
.btn-logout { padding: 5px 14px; border: 1px solid rgba(250, 248, 244, 0.25); background: transparent; color: var(--surface); font-family: var(--font-body); font-size: 0.75rem; cursor: pointer; transition: all 0.15s; }
.btn-logout:hover { background: rgba(250, 248, 244, 0.08); }

.layout-body { display: flex; flex: 1; }

/* WCAG fix: increased contrast from 0.65 to 0.8 */
.layout-sidebar { width: 180px; background: var(--warm-black); flex-shrink: 0; padding-top: var(--space-lg); }
.sidebar-overlay { display: none; }
.sidebar-nav { display: flex; flex-direction: column; gap: 2px; }
.nav-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 20px;
  font-family: var(--font-body); font-size: 0.8125rem; font-weight: 400;
  color: rgba(250, 248, 244, 0.8); text-decoration: none;
  transition: all 0.15s; border-left: 3px solid transparent; letter-spacing: 0.03em;
}
.nav-item svg { flex-shrink: 0; opacity: 0.7; }
.nav-item:hover { color: var(--surface); background: rgba(250, 248, 244, 0.06); }
.nav-item:hover svg { opacity: 1; }
.nav-item.router-link-active { color: var(--ochre-light); border-left-color: var(--ochre); background: rgba(250, 248, 244, 0.04); }
.nav-item.router-link-active svg { opacity: 1; color: var(--ochre-light); }

.layout-main { flex: 1; padding: var(--space-xl); overflow-y: auto; background: var(--paper); }

@media (max-width: 768px) {
  .menu-toggle { display: block; }
  .layout-main { padding: var(--space-md); }
  .layout-sidebar { position: fixed; top: 52px; left: -200px; bottom: 0; width: 200px; z-index: 100; transition: left 0.25s ease; padding-top: var(--space-md); }
  .layout-sidebar.open { left: 0; }
  .sidebar-overlay { display: block; position: fixed; inset: 0; background: rgba(44,36,22,0.4); z-index: -1; opacity: 0; pointer-events: none; transition: opacity 0.25s; }
  .layout-sidebar.open .sidebar-overlay { opacity: 1; pointer-events: auto; }
}
</style>
