<script setup lang="ts">
import { ref, watch, computed } from 'vue'

type TabKey = 'transferencia' | 'saque' | 'depositar'

const props = defineProps<{
  open: boolean
  tab: TabKey
  balance: number
}>()

const emit = defineEmits<{
  close: []
  'update:tab': [tab: TabKey]
  submit: [payload: { tab: TabKey; amount: number }]
}>()

const tabs: { key: TabKey; label: string; action: string }[] = [
  { key: 'transferencia', label: 'Transferência', action: 'Transferir' },
  { key: 'saque', label: 'Saque', action: 'Sacar' },
  { key: 'depositar', label: 'Depositar', action: 'Depositar' }
]

const transferValue = ref('')
const transferAccount = ref('')
const saqueValue = ref('')
const depositoValue = ref('')
const errorMsg = ref('')

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      transferValue.value = ''
      transferAccount.value = ''
      saqueValue.value = ''
      depositoValue.value = ''
      errorMsg.value = ''
    }
  }
)

watch(
  () => props.tab,
  () => {
    errorMsg.value = ''
  }
)

const formattedBalance = computed(() =>
  `R$ ${props.balance.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
)

function selectTab(key: TabKey) {
  emit('update:tab', key)
}

function parseAmount(raw: string): number {
  const cleaned = raw.replace(/[^\d,.-]/g, '').replace(/\.(?=\d{3}(,|$))/g, '').replace(',', '.')
  return parseFloat(cleaned)
}

function formatAmountInput(raw: string): string {
  const digits = raw.replace(/\D/g, '').replace(/^0+(?=\d)/, '')
  if (!digits) return ''
  const cents = parseInt(digits, 10)
  return `R$ ${(cents / 100).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function onAmountInput(target: 'transferencia' | 'saque' | 'depositar', event: Event) {
  const input = event.target as HTMLInputElement
  const formatted = formatAmountInput(input.value)

  if (target === 'transferencia') transferValue.value = formatted
  else if (target === 'saque') saqueValue.value = formatted
  else depositoValue.value = formatted

  input.value = formatted
}

function submit() {
  const raw = props.tab === 'transferencia' ? transferValue.value : props.tab === 'saque' ? saqueValue.value : depositoValue.value
  const amount = parseAmount(raw)

  if (!amount || amount <= 0) {
    errorMsg.value = 'Informe um valor válido.'
    return
  }
  if (props.tab === 'transferencia' && !transferAccount.value.trim()) {
    errorMsg.value = 'Informe a conta corrente.'
    return
  }
  if (props.tab !== 'depositar' && amount > props.balance) {
    errorMsg.value = 'Saldo insuficiente.'
    return
  }

  errorMsg.value = ''
  emit('submit', { tab: props.tab, amount })
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="overlay" @click.self="emit('close')">
      <div class="modal-wrap">
        <div class="modal-card">
          <button class="close-btn" aria-label="Fechar" @click="emit('close')">
            <svg viewBox="0 0 24 24" class="close-icon"><path d="M18 6 6 18" /><path d="m6 6 12 12" /></svg>
          </button>

          <div class="tabs">
            <button
              v-for="t in tabs"
              :key="t.key"
              class="tab"
              :class="{ active: t.key === tab }"
              @click="selectTab(t.key)"
            >
              <svg v-if="t.key === 'transferencia'" viewBox="0 0 24 24" class="tab-icon"><circle cx="12" cy="8" r="4" /><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" /></svg>
              <svg v-else-if="t.key === 'saque'" viewBox="0 0 24 24" class="tab-icon"><path d="M21 12a9 9 0 1 1-3.5-7.1" /><path d="M21 3v6h-6" /></svg>
              <svg v-else viewBox="0 0 24 24" class="tab-icon"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.7 21a2 2 0 0 1-3.4 0" /></svg>
              <span>{{ t.label }}</span>
            </button>
          </div>

          <div class="saldo-row">
            <span class="saldo">Saldo Atual: {{ formattedBalance }}</span>
          </div>

          <div v-if="tab === 'transferencia'" class="form">
            <label class="field">
              <span class="field-label">Valor da transferência</span>
              <input
                :value="transferValue"
                @input="onAmountInput('transferencia', $event)"
                type="text"
                inputmode="numeric"
                placeholder="R$ 30,00"
              />
            </label>
            <label class="field">
              <span class="field-label">Conta corrente</span>
              <input v-model="transferAccount" type="text" placeholder="XXXXX-X" />
            </label>
          </div>

          <div v-else-if="tab === 'saque'" class="form">
            <label class="field">
              <span class="field-label">Valor do saque</span>
              <input
                :value="saqueValue"
                @input="onAmountInput('saque', $event)"
                type="text"
                inputmode="numeric"
                placeholder="R$ 30,00"
              />
            </label>
          </div>

          <div v-else class="form">
            <label class="field">
              <span class="field-label">Valor do Depósito</span>
              <input
                :value="depositoValue"
                @input="onAmountInput('depositar', $event)"
                type="text"
                inputmode="numeric"
                placeholder="R$ 30,00"
              />
            </label>
          </div>

          <span v-if="errorMsg" class="error-msg">{{ errorMsg }}</span>

          <div class="submit-row">
            <button class="submit-btn" @click="submit">
              <span>{{ tabs.find((t) => t.key === tab)?.action }}</span>
              <svg viewBox="0 0 24 24" class="submit-icon"><path d="M5 12h14" /><path d="m13 6 6 6-6 6" /></svg>
            </button>
          </div>
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

.modal-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  max-width: 480px;
}

.modal-card {
  position: relative;
  background: #fff;
  border-radius: 24px;
  padding: 40px 36px 32px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
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
  width: 20px;
  height: 20px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.tabs {
  display: flex;
  gap: 28px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 22px;
}

.tab {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  padding: 0 0 14px;
  font-size: 15px;
  color: #6b7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;
}

.tab.active {
  color: #1f2937;
  font-weight: 600;
  border-bottom-color: #f0c968;
}

.tab-icon {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.saldo-row {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 22px;
}

.saldo {
  font-size: 14px;
  color: #9ca3af;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 26px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-size: 14px;
  color: #374151;
}

.field input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 14px 16px;
  font-size: 16px;
  color: #1f2937;
  outline: none;
  font-family: inherit;
}

.field input:focus {
  border-color: #f0c968;
}

.error-msg {
  display: block;
  color: #dc2626;
  font-size: 13px;
  margin: -14px 0 16px;
}

.submit-row {
  display: flex;
  justify-content: center;
}

.submit-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f0c968;
  border: none;
  border-radius: 12px;
  padding: 15px 36px;
  font-size: 15px;
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
