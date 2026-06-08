<template>
  <div id="app-root">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
    <Toast ref="toast" />
  </div>
</template>

<script>
import Toast from './components/Toast.vue'

export default {
  name: 'App',
  components: { Toast }
}
</script>

<style>
/* ============================================
   Design System: "Warm Archive"
   ============================================ */

:root {
  --copper: #C4562B;
  --copper-dark: #9E3F1F;
  --denim: #4A6A8A;
  --denim-light: #6B8AAA;
  --ochre: #C49B3A;
  --ochre-light: #DFBC6A;
  --paper: #F2EDE6;
  --surface: #FAF8F4;
  --warm-black: #2C2416;
  --warm-gray: #8A7E72;
  --warm-gray-light: #C4BAAE;
  --warm-border: #E2D8CC;
  --green-muted: #5A8A5A;
  --red-earth: #B54A3A;
  --red-earth-light: #F0DDD4;
  --font-display: 'DM Serif Display', Georgia, 'Times New Roman', serif;
  --font-body: 'Source Sans 3', -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --space-xs: 4px; --space-sm: 8px; --space-md: 16px; --space-lg: 24px; --space-xl: 32px; --space-2xl: 48px;
}

* { margin: 0; padding: 0; box-sizing: border-box; }

body {
  font-family: var(--font-body); background: var(--paper);
  color: var(--warm-black); min-height: 100vh; line-height: 1.6;
  -webkit-font-smoothing: antialiased;
}
body::before {
  content: ''; position: fixed; inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.03'/%3E%3C/svg%3E");
  pointer-events: none; z-index: 9999;
}

h1, h2, h3, h4 { font-family: var(--font-display); font-weight: 400; color: var(--warm-black); letter-spacing: 0.01em; }
a { color: var(--copper); text-decoration: none; transition: color 0.2s; }
a:hover { color: var(--copper-dark); }
::selection { background: var(--ochre); color: var(--warm-black); }

/* ─── Issue 1: Route transition ─── */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ─── Breadcrumbs ─── */
.breadcrumb { display: flex; align-items: center; gap: 6px; font-size: 0.75rem; color: var(--warm-gray-light); margin-bottom: var(--space-sm); font-family: var(--font-body); letter-spacing: 0.03em; }
.breadcrumb a { color: var(--warm-gray); }
.breadcrumb a:hover { color: var(--copper); }
.breadcrumb span { color: var(--warm-gray-light); }

/* ─── Page header ─── */
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: var(--space-xl); border-bottom: 2px solid var(--warm-border); padding-bottom: var(--space-sm); }
.page-header h3 { font-size: 1.5rem; font-family: var(--font-display); font-weight: 400; color: var(--warm-black); letter-spacing: 0.02em; }

/* ─── Table ─── */
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
.data-table tbody tr:hover { background: rgba(196, 86, 43, 0.04); }

/* ─── Skeleton ─── */
@keyframes skeleton-pulse { 0% { opacity: 0.6; } 50% { opacity: 1; } 100% { opacity: 0.6; } }
.skeleton { background: var(--warm-border); border-radius: 0; animation: skeleton-pulse 1.5s ease-in-out infinite; display: inline-block; line-height: 1; }
.skeleton-row { display: flex; gap: 16px; padding: 12px 18px; align-items: center; border-bottom: 1px solid var(--warm-border); }
.skeleton-cell { height: 14px; background: var(--warm-border); animation: skeleton-pulse 1.5s ease-in-out infinite; }
.skeleton-table { background: var(--surface); border: 1px solid var(--warm-border); position: relative; }
.skeleton-table::before { content: ''; position: absolute; top: 0; left: 0; width: 3px; height: 48px; background: var(--copper); }
.skeleton-table .shead { padding: 14px 18px; background: var(--paper); border-bottom: 1px solid var(--warm-border); }
.skeleton-table .shead .sc { height: 12px; width: 60px; background: var(--warm-gray-light); animation: skeleton-pulse 1.5s ease-in-out infinite; }

/* ─── Empty state ─── */
.empty-state { text-align: center; padding: 64px 20px; color: var(--warm-gray); font-size: 0.875rem; }
.empty-state .empty-icon { display: block; margin: 0 auto 16px; opacity: 0.4; }
.empty-state .empty-title { font-family: var(--font-display); font-size: 1.125rem; color: var(--warm-gray); margin-bottom: 6px; font-weight: 400; }
.empty-state .empty-desc { font-size: 0.8125rem; color: var(--warm-gray-light); margin-bottom: var(--space-md); font-style: italic; }
.empty-state .empty-action { margin-top: var(--space-sm); }

/* ─── Buttons ─── */
.btn { display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 8px 18px; border: none; font-family: var(--font-body); font-size: 0.8125rem; font-weight: 600; cursor: pointer; transition: all 0.15s ease; line-height: 1.4; letter-spacing: 0.02em; }
.btn:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-primary { background: var(--copper); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--copper-dark); transform: scale(0.97); }
.btn-outline { background: transparent; color: var(--warm-gray); border: 1px solid var(--warm-border); }
.btn-outline:hover:not(:disabled) { border-color: var(--copper); color: var(--copper); }
.btn-danger { background: var(--red-earth); color: #fff; }
.btn-danger:hover:not(:disabled) { filter: brightness(0.9); transform: scale(0.97); }
.btn-sm { padding: 5px 12px; font-size: 0.75rem; }
.page-btn.active-page { border-color: var(--copper); background: var(--copper); color: #fff; }

/* ─── Modal ─── */
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

/* ─── Form ─── */
.form-group { margin-bottom: 18px; }
.form-group label { display: block; font-size: 0.8125rem; font-weight: 600; color: var(--warm-gray); margin-bottom: 6px; letter-spacing: 0.03em; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px 14px; border: 1px solid var(--warm-border); font-family: var(--font-body); font-size: 0.875rem; background: #fff; outline: none; transition: border-color 0.2s; color: var(--warm-black); }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus { border-color: var(--copper); box-shadow: inset 0 0 0 1px var(--copper); }
.form-group textarea { resize: vertical; }

/* ─── Status tags ─── */
.status-tag { display: inline-block; padding: 2px 10px; font-size: 0.75rem; font-weight: 600; letter-spacing: 0.04em; }
.status-tag.borrowed { background: var(--denim-light); color: #fff; }
.status-tag.returned { background: var(--green-muted); color: #fff; }
.status-tag.overdue { background: var(--red-earth); color: #fff; }
.overdue-text { color: var(--red-earth); font-weight: 600; }

/* ─── Messages ─── */
.error-message { background: var(--red-earth-light); color: var(--red-earth); font-size: 0.8125rem; padding: 10px 14px; margin-bottom: 16px; border-left: 3px solid var(--red-earth); }
.success-message { background: #E4F0E4; color: var(--green-muted); font-size: 0.8125rem; padding: 10px 14px; margin-bottom: 16px; border-left: 3px solid var(--green-muted); }

/* ─── Pagination ─── */
.pagination { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: var(--space-lg); }
.page-btn { padding: 6px 16px; border: 1px solid var(--warm-border); background: var(--surface); font-family: var(--font-body); font-size: 0.8125rem; color: var(--warm-gray); cursor: pointer; transition: all 0.15s; }
.page-btn:hover:not(:disabled) { border-color: var(--copper); color: var(--copper); }
.page-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.page-info { font-size: 0.8125rem; color: var(--warm-gray); }

/* ─── Responsive ─── */
@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: var(--space-sm); }
  .page-header h3 { font-size: 1.25rem; }
  .data-table { font-size: 0.8125rem; }
  .data-table thead th, .data-table tbody td { padding: 10px 12px; }
}
</style>
