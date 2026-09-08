<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import TransactionModal from '../components/TransactionModal.vue'
import Sidebar from '../components/Sidebar.vue'

type ModalTab = 'transferencia' | 'saque' | 'depositar'

interface Transaction {
  id: number
  name: string
  date: string
  amount: number
  icon: 'bag' | 'coffee' | 'car' | 'home' | 'movie'
}

const user = {
  name: 'John',
  fullName: 'John Doe',
  email: 'john@example.com',
  initials: 'AD'
}

const account = reactive({
  balance: 24582.5,
  number: '•••• 4829'
})

const transactions: Transaction[] = [
  { id: 1, name: 'Amazon', date: 'Today, 2:30 PM', amount: -89.99, icon: 'bag' },
  { id: 2, name: 'Starbucks', date: 'Today, 9:15 AM', amount: -5.5, icon: 'coffee' },
  { id: 3, name: 'Uber', date: 'Yesterday, 6:45 PM', amount: -24.0, icon: 'car' },
  { id: 4, name: 'Salary Deposit', date: 'Dec 1, 2024', amount: 3500.0, icon: 'home' },
  { id: 5, name: 'Netflix', date: 'Nov 28, 2024', amount: -15.99, icon: 'movie' }
]

const quickActions: { label: string; icon: string; tab: ModalTab | null }[] = [
  { label: 'Transferir', icon: 'send', tab: 'transferencia' },
  { label: 'Saque', icon: 'download', tab: 'saque' },
  { label: 'Extrato', icon: 'file', tab: null },
  { label: 'Depositar', icon: 'plus', tab: 'depositar' }
]

const modalOpen = ref(false)
const modalTab = ref<ModalTab>('transferencia')

function openModal(tab: ModalTab | null) {
  if (!tab) return
  modalTab.value = tab
  modalOpen.value = true
}

function handleTransaction(payload: { tab: ModalTab; amount: number }) {
  if (payload.tab === 'depositar') {
    account.balance += payload.amount
  } else {
    account.balance -= payload.amount
  }
}

function formatCurrency(value: number): string {
  const sign = value < 0 ? '-' : ''
  return `${sign}R$${Math.abs(value).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const formattedBalance = computed(() => `R$${account.balance.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`)
</script>

<template>
  <div class="layout">
    <Sidebar active="home" :user="user" />

    <div class="main">
      <header class="topbar">
        <div class="topbar-icon">
          <svg viewBox="0 0 24 24" class="shield-icon"><path d="M12 2 4 5v6c0 5 3.4 8.9 8 10 4.6-1.1 8-5 8-10V5l-8-3Z" /></svg>
        </div>
        <div class="topbar-text">
          <span class="topbar-title">BANTADS</span>
          <span class="topbar-subtitle">Internet Banking</span>
        </div>
      </header>

      <main class="content">
        <h1 class="greeting">Bem vindo, {{ user.name }}</h1>

        <section class="balance-card">
          <div class="balance-top">
            <span class="balance-label">Saldo Atual</span>
            <span class="balance-value">{{ formattedBalance }}</span>
          </div>
          <hr class="balance-divider" />
          <div class="balance-bottom">
            <div class="balance-account">
              <span class="balance-label">Número da conta</span>
              <span class="balance-limit">{{ account.number }}</span>
            </div>
          </div>
        </section>

        <h2 class="section-title">O que deseja fazer?</h2>
        <section class="actions-grid">
          <button
            v-for="action in quickActions"
            :key="action.label"
            class="action-card"
            @click="openModal(action.tab)"
          >
            <span class="action-icon">
              <svg v-if="action.icon === 'send'" viewBox="0 0 24 24"><path d="m22 2-7 20-4-9-9-4Z" /><path d="M22 2 11 13" /></svg>
              <svg v-else-if="action.icon === 'download'" viewBox="0 0 24 24"><path d="M12 3v12" /><path d="m7 10 5 5 5-5" /><path d="M5 21h14" /></svg>
              <svg v-else-if="action.icon === 'file'" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z" /><path d="M14 2v6h6" /></svg>
              <svg v-else viewBox="0 0 24 24"><path d="M12 5v14" /><path d="M5 12h14" /></svg>
            </span>
            <span class="action-label">{{ action.label }}</span>
          </button>
        </section>

        <section class="transactions-card">
          <div class="transactions-header">
            <span class="section-title">Transações recentes</span>
            <a href="#" class="ver-todos">Ver Todos</a>
          </div>

          <ul class="transaction-list">
            <li v-for="tx in transactions" :key="tx.id" class="transaction-row">
              <span class="transaction-icon">
                <svg v-if="tx.icon === 'bag'" viewBox="0 0 24 24"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" /><path d="M3 6h18" /><path d="M16 10a4 4 0 0 1-8 0" /></svg>
                <svg v-else-if="tx.icon === 'coffee'" viewBox="0 0 24 24"><path d="M17 8h1a4 4 0 1 1 0 8h-1" /><path d="M3 8h14v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4Z" /><path d="M6 2v2" /><path d="M10 2v2" /><path d="M14 2v2" /></svg>
                <svg v-else-if="tx.icon === 'car'" viewBox="0 0 24 24"><path d="M14 16H9m10 0h3v-3.15a1 1 0 0 0-.84-.99L16 11l-2.7-3.6a1 1 0 0 0-.8-.4H5.24a2 2 0 0 0-1.8 1.1l-.8 1.63A6 6 0 0 0 2 12.42V16h2" /><circle cx="6.5" cy="16.5" r="2.5" /><circle cx="16.5" cy="16.5" r="2.5" /></svg>
                <svg v-else-if="tx.icon === 'home'" viewBox="0 0 24 24"><path d="M3 11.5 12 4l9 7.5" /><path d="M5 10v9a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1v-9" /></svg>
                <svg v-else viewBox="0 0 24 24"><rect x="2" y="3" width="20" height="14" rx="2" /><path d="M8 21h8" /><path d="M12 17v4" /></svg>
              </span>
              <span class="transaction-info">
                <span class="transaction-name">{{ tx.name }}</span>
                <span class="transaction-date">{{ tx.date }}</span>
              </span>
              <span class="transaction-amount" :class="tx.amount > 0 ? 'positive' : 'negative'">
                {{ tx.amount > 0 ? '+ ' : '' }}{{ formatCurrency(tx.amount) }}
              </span>
            </li>
          </ul>
        </section>
      </main>
    </div>

    <TransactionModal
      :open="modalOpen"
      v-model:tab="modalTab"
      :balance="account.balance"
      @close="modalOpen = false"
      @submit="handleTransaction"
    />
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.layout {
  display: flex;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  background: #f3f4f6;
}

/* Main */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  background: #0d1b2a;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 32px;
}

.topbar-icon {
  width: 40px;
  height: 40px;
  background: #f0c968;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.shield-icon {
  width: 20px;
  height: 20px;
  fill: none;
  stroke: #1a1a1a;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.topbar-text {
  display: flex;
  flex-direction: column;
}

.topbar-title {
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  letter-spacing: 0.5px;
}

.topbar-subtitle {
  color: #94a3b8;
  font-size: 12px;
}

.content {
  padding: 28px 32px 48px;
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
}

.greeting {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 20px;
}

/* Balance card */
.balance-card {
  background: #0d1b2a;
  border-radius: 16px;
  padding: 24px 28px;
  color: #fff;
  margin-bottom: 28px;
}

.balance-top {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.balance-label {
  color: #94a3b8;
  font-size: 12px;
}

.balance-value {
  font-size: 36px;
  font-weight: 700;
}

.balance-divider {
  border: none;
  border-top: 1px solid #223349;
  margin: 16px 0;
}

.balance-bottom {
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
}

.balance-bottom > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.balance-account {
  align-items: flex-end;
}

.balance-limit {
  font-size: 16px;
  font-weight: 600;
}

.balance-limit small {
  font-size: 11px;
  font-weight: 400;
  color: #94a3b8;
}

/* Section title */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #6b7280;
  margin: 0 0 14px;
}

/* Actions grid */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}

.action-card {
  background: #fff;
  border: none;
  border-radius: 12px;
  padding: 24px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.15s, transform 0.15s;
}

.action-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #fbe6ae;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon svg {
  width: 20px;
  height: 20px;
  fill: none;
  stroke: #b8860b;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.action-label {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
}

/* Transactions */
.transactions-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.transactions-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.transactions-header .section-title {
  margin: 0;
  color: #111827;
  font-size: 16px;
}

.ver-todos {
  font-size: 13px;
  color: #2563eb;
  text-decoration: none;
}

.transaction-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.transaction-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
}

.transaction-row:last-child {
  border-bottom: none;
}

.transaction-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.transaction-icon svg {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: #475569;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.transaction-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.transaction-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.transaction-date {
  font-size: 12px;
  color: #9ca3af;
}

.transaction-amount {
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.transaction-amount.negative {
  color: #dc2626;
}

.transaction-amount.positive {
  color: #16a34a;
}
</style>
