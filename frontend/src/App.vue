<template>
  <div id="app-root" :class="{ dark: isDark }">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
    <Toast />
    <SearchModal />
  </div>
</template>

<script>
import Toast from './components/Toast.vue'
import SearchModal from './components/SearchModal.vue'

export default {
  name: 'App',
  components: { Toast, SearchModal },
  data() { return { isDark: false } },
  mounted() { this.isDark = localStorage.getItem('dark')==='1' }
}
</script>

<style>
@import 'nprogress/nprogress.css';

:root {
  --copper: #C4562B; --copper-dark: #9E3F1F; --denim: #4A6A8A; --denim-light: #6B8AAA;
  --ochre: #C49B3A; --ochre-light: #DFBC6A;
  --paper: #F2EDE6; --surface: #FAF8F4; --warm-black: #2C2416;
  --warm-gray: #8A7E72; --warm-gray-light: #C4BAAE; --warm-border: #E2D8CC;
  --green-muted: #5A8A5A; --red-earth: #B54A3A; --red-earth-light: #F0DDD4;
  --font-display: 'DM Serif Display', Georgia, serif;
  --font-body: 'Source Sans 3', -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --space-xs: 4px; --space-sm: 8px; --space-md: 16px; --space-lg: 24px; --space-xl: 32px; --space-2xl: 48px;
}

.dark {
  --paper: #1A1815; --surface: #252220; --warm-black: #F2EDE6;
  --warm-gray: #A89B8A; --warm-gray-light: #6B5F50; --warm-border: #3D362E;
  --warm-black: #F2EDE6; --warm-gray: #A89B8A; --warm-gray-light: #6B5F50; --warm-border: #3D362E;
}

/* Fix dark mode text */
.dark { color: #E0D8CC; }
.dark h1, .dark h2, .dark h3, .dark h4 { color: #E0D8CC; }
.dark .table-wrap, .dark .layout-main { background: var(--paper); }
.dark .layout-main { background: var(--paper); }
.dark .data-table thead th { background: #252220; }
.dark .data-table tbody tr:hover { background: rgba(196,86,43,0.08); }
.dark input, .dark select, .dark textarea { background: #1A1815; color: #E0D8CC; }
.dark .btn-outline { color: var(--warm-gray); }
.dark .modal { background: var(--surface); }
.dark .stat-block { background: #252220; }
.dark .form-card { background: #252220; }
.dark .detail-main { background: #252220; }
.dark .dash-grid { background: var(--warm-border); }
.dark .quick-btn { background: #252220; }
.dark .page-btn { background: #252220; }
.dark .filter-input, .dark .filter-select { background: #1A1815; }

* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: var(--font-body); background: var(--paper);
  color: var(--warm-black); min-height: 100vh; line-height: 1.6;
  -webkit-font-smoothing: antialiased; transition: background 0.3s;
}

h1, h2, h3, h4 { font-family: var(--font-display); font-weight: 400; color: var(--warm-black); letter-spacing: 0.01em; }
a { color: var(--copper); text-decoration: none; transition: color 0.2s; }
a:hover { color: var(--copper-dark); }
::selection { background: var(--ochre); color: var(--warm-black); }

/* NProgress custom color */
#nprogress .bar { background: var(--copper) !important; height: 3px !important; }
#nprogress .peg { box-shadow: 0 0 10px var(--copper), 0 0 5px var(--copper) !important; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.breadcrumb { display: flex; align-items: center; gap: 6px; font-size: 0.75rem; color: var(--warm-gray-light); margin-bottom: var(--space-sm); font-family: var(--font-body); letter-spacing: 0.03em; }
.breadcrumb a { color: var(--warm-gray); }
.breadcrumb a:hover { color: var(--copper); }

.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: var(--space-xl); border-bottom: 2px solid var(--warm-border); padding-bottom: var(--space-sm); }
.page-header h3 { font-size: 1.5rem; font-family: var(--font-display); font-weight: 400; color: var(--warm-black); letter-spacing: 0.02em; }

.table-scroll { overflow-x: auto; -webkit-overflow-scrolling: touch; }
.table-scroll::-webkit-scrollbar { height: 6px; }
.table-scroll::-webkit-scrollbar-track { background: var(--paper); }
.table-scroll::-webkit-scrollbar-thumb { background: var(--warm-border); border-radius: 3px; }
.table-wrap { background: var(--surface); border: 1px solid var(--warm-border); position: relative; }
.table-wrap::before { content: ''; position: absolute; top: 0; left: 0; width: 3px; height: 48px; background: var(--copper); }
.data-table { width: 100%; border-collapse: collapse; font-size: 0.875rem; white-space: nowrap; }
.data-table thead th { text-align: left; padding: 14px 18px; font-family: var(--font-body); font-weight: 600; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.08em; color: var(--warm-gray); background: var(--paper); border-bottom: 1px solid var(--warm-border); }
.data-table tbody td { padding: 12px 18px; border-bottom: 1px solid var(--warm-border); color: var(--warm-black); line-height: 1.5; }
.data-table tbody tr:last-child td { border-bottom: none; }

@keyframes skeleton-pulse { 0% { opacity: 0.6; } 50% { opacity: 1; } 100% { opacity: 0.6; } }
.skeleton { background: var(--warm-border); animation: skeleton-pulse 1.5s ease-in-out infinite; display: inline-block; line-height: 1; }
.skeleton-row { display: flex; gap: 16px; padding: 12px 18px; align-items: center; border-bottom: 1px solid var(--warm-border); }
.skeleton-cell { height: 14px; background: var(--warm-border); animation: skeleton-pulse 1.5s ease-in-out infinite; }
.skeleton-table { background: var(--surface); border: 1px solid var(--warm-border); position: relative; }
.skeleton-table::before { content: ''; position: absolute; top: 0; left: 0; width: 3px; height: 48px; background: var(--copper); }
.skeleton-table .shead { padding: 14px 18px; background: var(--paper); border-bottom: 1px solid var(--warm-border); }
.skeleton-table .shead .sc { height: 12px; width: 60px; background: var(--warm-gray-light); animation: skeleton-pulse 1.5s ease-in-out infinite; }

.empty-state { text-align: center; padding: 64px 20px; color: var(--warm-gray); font-size: 0.875rem; }
.empty-state .empty-icon { display: block; margin: 0 auto 16px; opacity: 0.4; }
.empty-state .empty-title { font-family: var(--font-display); font-size: 1.125rem; color: var(--warm-gray); margin-bottom: 6px; font-weight: 400; }
.empty-state .empty-desc { font-size: 0.8125rem; color: var(--warm-gray-light); margin-bottom: var(--space-md); font-style: italic; }

.btn { display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 8px 18px; border: none; font-family: var(--font-body); font-size: 0.8125rem; font-weight: 600; cursor: pointer; transition: all 0.15s ease; line-height: 1.4; letter-spacing: 0.02em; }
.btn:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-primary { background: var(--copper); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--copper-dark); transform: scale(0.97); }
.btn-outline { background: transparent; color: var(--warm-gray); border: 1px solid var(--warm-border); }
.btn-outline:hover:not(:disabled) { border-color: var(--copper); color: var(--copper); }
.btn-danger { background: var(--red-earth); color: #fff; }
.btn-danger:hover:not(:disabled) { filter: brightness(0.9); transform: scale(0.97); }
.btn-sm { padding: 5px 12px; font-size: 0.75rem; }
.btn-sep { display: inline-block; width: 1px; height: 16px; background: var(--warm-border); margin: 0 4px; vertical-align: middle; }
.page-btn.active-page { border-color: var(--copper); background: var(--copper); color: #fff; }

.modal-overlay { position: fixed; inset: 0; background: rgba(44, 36, 22, 0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: var(--surface); width: 100%; max-width: 460px; border: 1px solid var(--warm-border); box-shadow: 8px 8px 0 rgba(44, 36, 22, 0.1); }
.modal-sm { max-width: 380px; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px 0; }
.modal-header h4 { font-family: var(--font-display); font-size: 1.125rem; font-weight: 400; }
.modal-close { border: none; background: none; font-size: 1.5rem; color: var(--warm-gray); cursor: pointer; line-height: 1; }
.modal-close:hover { color: var(--warm-black); }
.modal-body { padding: 20px 24px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 20px; }
.delete-target-name { padding: 0 2px; font-weight: 600; color: var(--copper); }

.form-group { margin-bottom: 18px; }
.form-group label { display: block; font-size: 0.8125rem; font-weight: 600; color: var(--warm-gray); margin-bottom: 6px; letter-spacing: 0.03em; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px 14px; border: 1px solid var(--warm-border); font-family: var(--font-body); font-size: 0.875rem; background: #fff; outline: none; transition: border-color 0.2s; color: var(--warm-black); }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus { border-color: var(--copper); box-shadow: inset 0 0 0 1px var(--copper); }
.form-group textarea { resize: vertical; }

.status-tag { display: inline-block; padding: 2px 10px; font-size: 0.75rem; font-weight: 600; letter-spacing: 0.04em; }
.status-tag.borrowed { background: var(--denim-light); color: #fff; }
.status-tag.returned { background: var(--green-muted); color: #fff; }
.status-tag.overdue { background: var(--red-earth); color: #fff; }
.overdue-text { color: var(--red-earth); font-weight: 600; }
.error-message { background: var(--red-earth-light); color: var(--red-earth); font-size: 0.8125rem; padding: 10px 14px; margin-bottom: 16px; border-left: 3px solid var(--red-earth); }
.success-message { background: #E4F0E4; color: var(--green-muted); font-size: 0.8125rem; padding: 10px 14px; margin-bottom: 16px; border-left: 3px solid var(--green-muted); }

.pagination { display: flex; justify-content: center; align-items: center; gap: 8px; margin-top: var(--space-lg); }
.page-btn { padding: 6px 14px; border: 1px solid var(--warm-border); background: var(--surface); font-family: var(--font-body); font-size: 0.8125rem; color: var(--warm-gray); cursor: pointer; transition: all 0.15s; }
.page-btn:hover:not(:disabled) { border-color: var(--copper); color: var(--copper); }
.page-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.page-info { font-size: 0.8125rem; color: var(--warm-gray); }

/* Dark mode toggle */
.dark-toggle { position: fixed; bottom: 24px; right: 24px; z-index: 999; width: 40px; height: 40px; border-radius: 50%; border: 1px solid var(--warm-border); background: var(--surface); cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 18px; transition: all 0.2s; }
.dark-toggle:hover { border-color: var(--copper); }

@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: var(--space-sm); }
  .page-header h3 { font-size: 1.25rem; }
}

/* D12: Print styles - hide nav/buttons when printing borrow records */
@media print {
  .layout-header, .layout-sidebar, .btn, .breadcrumb, .pagination,
  .filter-bar, .dark-toggle, .toast-container, .modal-overlay { display: none !important; }
  .layout-main { padding: 0 !important; background: #fff !important; }
  body { background: #fff; }
  .data-table { font-size: 10pt; }
  .data-table thead th { background: #f0f0f0; color: #000; }
}

/* D15: Sold-out badge on book cards */
.bc-soldout {
  position: absolute; top: 8px; right: 8px;
  background: var(--red-earth); color: #fff; font-size: 0.625rem;
  padding: 2px 8px; font-weight: 700; letter-spacing: 0.05em;
}
</style>
