<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  open: boolean
  cpf: string
  name: string
  salaryLabel: string
}>()

const emit = defineEmits<{
  close: []
  submit: [reason: string]
}>()

const reason = ref('')
const errorMsg = ref('')

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      reason.value = ''
      errorMsg.value = ''
    }
  }
)

function submit() {
  if (!reason.value.trim()) {
    errorMsg.value = 'Informe o motivo da recusa.'
    return
  }
  emit('submit', reason.value.trim())
}
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="overlay" @click.self="emit('close')">
      <div class="modal-card">
        <button class="close-btn" aria-label="Fechar" @click="emit('close')">
          <svg viewBox="0 0 24 24" class="close-icon"><path d="M18 6 6 18" /><path d="m6 6 12 12" /></svg>
        </button>

        <div class="modal-header">
          <svg viewBox="0 0 24 24" class="header-icon"><circle cx="12" cy="8" r="4" /><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" /></svg>
          <h2 class="modal-title">Recusar Cadastro</h2>
        </div>

        <div class="form">
          <label class="field">
            <span class="field-label">CPF</span>
            <input :value="cpf" type="text" disabled />
          </label>

          <label class="field">
            <span class="field-label">Nome</span>
            <input :value="name" type="text" disabled />
          </label>

          <label class="field">
            <span class="field-label">Salário</span>
            <input :value="salaryLabel" type="text" disabled />
          </label>

          <label class="field">
            <span class="field-label">Motivo</span>
            <textarea v-model="reason" rows="3" placeholder="Digite o motivo da recusa..."></textarea>
          </label>
        </div>

        <span v-if="errorMsg" class="error-msg">{{ errorMsg }}</span>

        <div class="submit-row">
          <button class="submit-btn" @click="submit">
            <span>Recusar</span>
            <svg viewBox="0 0 24 24" class="submit-icon"><path d="M5 12h14" /><path d="m13 6 6 6-6 6" /></svg>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.modal-card {
  position: relative;
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 24px;
  padding: 36px 32px 28px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #4b5563;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.close-icon {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 14px;
  margin-bottom: 22px;
  border-bottom: 1px solid #e5e7eb;
}

.header-icon {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: #1f2937;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  flex-shrink: 0;
}

.modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 8px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 12px;
  color: #374151;
  font-weight: 500;
}

.field input,
.field textarea {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 11px 14px;
  font-size: 13px;
  color: #1f2937;
  outline: none;
  font-family: inherit;
  resize: none;
}

.field input:disabled {
  background: #f3f4f6;
  color: #6b7280;
  cursor: default;
}

.field textarea:focus {
  border-color: #f0c968;
}

.error-msg {
  display: block;
  color: #dc2626;
  font-size: 12px;
  margin: 8px 0 0;
}

.submit-row {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

.submit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f0c968;
  border: none;
  border-radius: 12px;
  padding: 13px 32px;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  cursor: pointer;
  transition: background 0.15s;
}

.submit-btn:hover {
  background: #e8bd52;
}

.submit-icon {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>
