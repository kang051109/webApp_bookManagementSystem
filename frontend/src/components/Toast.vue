<template>
  <teleport to="body">
    <div class="toast-container">
      <transition-group name="toast-slide">
        <div v-for="t in toasts" :key="t.id" :class="['toast', t.type]">
          <span class="toast-msg">{{ t.message }}</span>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<script>
import { subscribe } from '../services/toast.js'

export default {
  name: 'Toast',
  data() { return { toasts: [] } },
  mounted() { this.unsubscribe = subscribe(toasts => { this.toasts = toasts }) },
  beforeUnmount() { if (this.unsubscribe) this.unsubscribe() }
}
</script>

<style scoped>
.toast-container {
  position: fixed; top: 16px; right: 16px; z-index: 10000;
  display: flex; flex-direction: column; gap: 8px;
  pointer-events: none;
}
.toast {
  pointer-events: auto;
  padding: 12px 20px;
  border-left: 3px solid;
  font-family: var(--font-body);
  font-size: 0.8125rem;
  font-weight: 500;
  box-shadow: 4px 4px 0 rgba(44, 36, 22, 0.08);
  max-width: 360px;
}
.toast.success { background: #E4F0E4; border-left-color: var(--green-muted); color: var(--green-muted); }
.toast.error { background: var(--red-earth-light); border-left-color: var(--red-earth); color: var(--red-earth); }
.toast.info { background: #E8EFF5; border-left-color: var(--denim); color: var(--denim); }

.toast-slide-enter-active { transition: all 0.3s ease; }
.toast-slide-leave-active { transition: all 0.2s ease; }
.toast-slide-enter-from { opacity: 0; transform: translateX(40px); }
.toast-slide-leave-to { opacity: 0; transform: translateX(40px); }
</style>
