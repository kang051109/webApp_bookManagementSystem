/**
 * Lightweight global Toast notification
 * - Usage:: import { showToast } from '../services/toast.js'
 *       showToast('Success', 'success')
 *       showToast('error', 'error', 5000)
 */
let toastId = 0
let listeners = []

const state = {
  toasts: []
}

function notify() {
  listeners.forEach(fn => fn(state.toasts))
}

export function showToast(message, type = 'success', duration = 3000) {
  const id = ++toastId
  state.toasts.push({ id, message, type })
  notify()
  setTimeout(() => {
    state.toasts = state.toasts.filter(t => t.id !== id)
    notify()
  }, duration)
}

export function subscribe(fn) {
  listeners.push(fn)
  fn(state.toasts)
  return () => { listeners = listeners.filter(f => f !== fn) }
}

export function getState() { return state }
