<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import Sidebar from '../components/Sidebar.vue'

interface ClientReportRow {
  id: number
  cpf: string
  name: string
  email: string
  salary: number
  accountNumber: string
  balance: number
  managerCpf: string
  managerName: string
}

const user = {
  fullName: 'Gerente',
  email: 'gerente@bantads.com.br',
  initials: 'GE'
}

// Dados mockados (farão o papel do relatório consolidado — MS Cliente + MS Conta + MS Gerente —
// enquanto o back-end não expõe um endpoint único para o relatório).
const clients = reactive<ClientReportRow[]>([
  { id: 1, cpf: '333.444.555-66', name: 'Ana Carolina', email: 'ana@email.com', salary: 4200.0, accountNumber: '1003-1', balance: 12500.75, managerCpf: '111.222.333-44', managerName: 'Ana Silva' },
  { id: 2, cpf: '899.909.010-12', name: 'Arthur Pendragon', email: 'arthur@email.com', salary: 3100.0, accountNumber: '1010-6', balance: 4500.0, managerCpf: '222.333.444-55', managerName: 'Helena Rocha' },
  { id: 3, cpf: '809.859.121-23', name: 'Bruno Mars', email: 'bruno@email.com', salary: 9800.0, accountNumber: '1019-4', balance: 24000.0, managerCpf: '333.444.555-66', managerName: 'Juliana Ferreira' },
  { id: 4, cpf: '155.666.777-88', name: 'Camila Torres', email: 'camila@email.com', salary: 2600.0, accountNumber: '1005-7', balance: -5000.0, managerCpf: '444.555.666-77', managerName: 'Fernanda Lima' },
  { id: 5, cpf: '810.321.232-34', name: 'Clara Nunes', email: 'clara@email.com', salary: 1900.0, accountNumber: '1026-2', balance: 1200.0, managerCpf: '555.666.777-88', managerName: 'Carlos Mendes' },
  { id: 6, cpf: '222.333.444-55', name: 'João Pedro', email: 'joao@email.com', salary: 3300.0, accountNumber: '1002-3', balance: -1200.0, managerCpf: '555.666.777-88', managerName: 'Carlos Mendes' },
  { id: 7, cpf: '777.888.999-00', name: 'Larissa Manoela', email: 'larissa@email.com', salary: 5400.0, accountNumber: '1067-3', balance: 2340.2, managerCpf: '222.333.444-55', managerName: 'Helena Rocha' },
  { id: 8, cpf: '888.999.000-11', name: 'Marcos Vinícius', email: 'marcos@email.com', salary: 2200.0, accountNumber: '1088-1', balance: 0.0, managerCpf: '333.444.555-66', managerName: 'Juliana Ferreira' },
  { id: 9, cpf: '111.222.333-44', name: 'Maria Souza', email: 'maria@email.com', salary: 6700.0, accountNumber: '1001-5', balance: 5430.5, managerCpf: '111.222.333-44', managerName: 'Ana Silva' },
  { id: 10, cpf: '999.000.111-22', name: 'Patrícia Lima', email: 'patricia@email.com', salary: 3900.0, accountNumber: '1009-9', balance: 1500.0, managerCpf: '111.222.333-44', managerName: 'Ana Silva' },
  { id: 11, cpf: '666.777.888-99', name: 'Pedro Henrique', email: 'pedro@email.com', salary: 15000.0, accountNumber: '1006-5', balance: 50000.0, managerCpf: '555.666.777-88', managerName: 'Carlos Mendes' },
  { id: 12, cpf: '800.111.222-33', name: 'Ricardo Oliveira', email: 'ricardo@email.com', salary: 2100.0, accountNumber: '1018-2', balance: -250.5, managerCpf: '666.777.888-99', managerName: 'Beatriz Santos' },
  { id: 13, cpf: '444.555.666-77', name: 'Roberto Dias', email: 'roberto@email.com', salary: 1800.0, accountNumber: '1004-9', balance: 450.0, managerCpf: '666.777.888-99', managerName: 'Beatriz Santos' },
  { id: 14, cpf: '121.232.343-45', name: 'Sofia Costa', email: 'sofia@email.com', salary: 4800.0, accountNumber: '1011-0', balance: 8900.0, managerCpf: '444.555.666-77', managerName: 'Fernanda Lima' },
  { id: 15, cpf: '232.343.454-56', name: 'Thiago Silva', email: 'thiago@email.com', salary: 2900.0, accountNumber: '1032-8', balance: 120.0, managerCpf: '222.333.444-55', managerName: 'Helena Rocha' },
  { id: 16, cpf: '343.454.565-67', name: 'Vanessa Santos', email: 'vanessa@email.com', salary: 7200.0, accountNumber: '1013-6', balance: 34000.0, managerCpf: '333.444.555-66', managerName: 'Juliana Ferreira' },
  { id: 17, cpf: '454.565.676-78', name: 'Wagner Souza', email: 'wagner@email.com', salary: 2000.0, accountNumber: '1044-4', balance: -1500.0, managerCpf: '555.666.777-88', managerName: 'Carlos Mendes' },
  { id: 18, cpf: '565.676.787-89', name: 'Xuxa Meneghel', email: 'xuxa@email.com', salary: 25000.0, accountNumber: '1053-2', balance: 1000000.0, managerCpf: '111.222.333-44', managerName: 'Ana Silva' },
  { id: 19, cpf: '676.787.898-90', name: 'Yuri Gagarin', email: 'yuri@email.com', salary: 3500.0, accountNumber: '1038-8', balance: 50.0, managerCpf: '666.777.888-99', managerName: 'Beatriz Santos' },
  { id: 20, cpf: '787.898.909-01', name: 'Zelda Fitzgerald', email: 'zelda@email.com', salary: 4100.0, accountNumber: '1037-0', balance: -300.0, managerCpf: '444.555.666-77', managerName: 'Fernanda Lima' }
])

function formatCurrency(value: number): string {
  const formatted = Math.abs(value).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  return `${value < 0 ? '-' : ''}R$ ${formatted}`
}

const sortDirection = ref<'asc' | 'desc'>('asc')

function toggleSort() {
  sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
}

// Ordenação crescente por nome do cliente (padrão do relatório)
const sortedClients = computed(() => {
  const list = [...clients].sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'))
  return sortDirection.value === 'asc' ? list : list.reverse()
})

const pageSize = 20
const currentPage = ref(1)
const totalPages = computed(() => Math.max(1, Math.ceil(sortedClients.value.length / pageSize)))

const pagedClients = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return sortedClients.value.slice(start, start + pageSize)
})

function goToPreviousPage() {
  if (currentPage.value > 1) currentPage.value -= 1
}

function goToNextPage() {
  if (currentPage.value < totalPages.value) currentPage.value += 1
}
</script>

<template>
  <div class="layout">
    <Sidebar active="relatorio" role="gerente" :user="user" />

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
        <div class="tabs">
          <span class="tab active">Relatório de Clientes</span>
        </div>

        <div class="table-card">
          <div class="table-scroll">
            <table class="table">
              <thead>
                <tr>
                  <th class="sortable" @click="toggleSort">
                    Nome do Cliente
                    <svg viewBox="0 0 24 24" class="sort-icon" :class="{ desc: sortDirection === 'desc' }"><path d="m18 15-6-6-6 6" /></svg>
                  </th>
                  <th>CPF</th>
                  <th class="align-right">Salário</th>
                  <th>Gerente</th>
                  <th>CPF do Gerente</th>
                  <th>Número da Conta</th>
                  <th class="align-right">Saldo</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="client in pagedClients" :key="client.id">
                  <td>
                    <span class="cell-name">{{ client.name }}</span>
                    <span class="cell-email">{{ client.email }}</span>
                  </td>
                  <td class="cell-muted">{{ client.cpf }}</td>
                  <td class="align-right cell-muted">{{ formatCurrency(client.salary) }}</td>
                  <td><span class="pill">{{ client.managerName }}</span></td>
                  <td class="cell-muted">{{ client.managerCpf }}</td>
                  <td class="cell-muted">{{ client.accountNumber }}</td>
                  <td class="align-right" :class="client.balance < 0 ? 'balance-negative' : 'balance-positive'">
                    {{ formatCurrency(client.balance) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="table-footer">
            <span class="showing">Mostrando {{ pagedClients.length }} clientes</span>
            <div class="pagination">
              <button class="page-btn" :disabled="currentPage === 1" @click="goToPreviousPage">Anterior</button>
              <button class="page-btn" :disabled="currentPage === totalPages" @click="goToNextPage">Próxima</button>
            </div>
          </div>
        </div>
      </main>
    </div>
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
  flex: 1;
  min-width: 0;
  padding: 12px 32px 48px;
  width: 100%;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tab {
  padding: 10px 18px;
  border-radius: 8px 8px 0 0;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
}

.tab.active {
  background: #f0c968;
  color: #1a1a1a;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.table-scroll {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table thead th {
  text-align: left;
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  white-space: nowrap;
}

.table th.sortable {
  cursor: pointer;
  user-select: none;
  display: table-cell;
}

.sort-icon {
  width: 12px;
  height: 12px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2.4;
  stroke-linecap: round;
  stroke-linejoin: round;
  margin-left: 4px;
  vertical-align: middle;
  transition: transform 0.15s;
}

.sort-icon.desc {
  transform: rotate(180deg);
}

.table tbody td {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
  color: #374151;
  vertical-align: middle;
}

.table tbody tr:last-child td {
  border-bottom: none;
}

.table th.align-right,
.table td.align-right {
  text-align: right;
}

.cell-name {
  display: block;
  color: #111827;
  font-weight: 500;
}

.cell-email {
  display: block;
  color: #6b7280;
  font-size: 12px;
  margin-top: 2px;
}

.cell-muted {
  color: #6b7280;
  white-space: nowrap;
}

.pill {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #374151;
  font-size: 12px;
  white-space: nowrap;
}

.balance-positive {
  color: #16a34a;
  font-weight: 600;
  white-space: nowrap;
}

.balance-negative {
  color: #dc2626;
  font-weight: 600;
  white-space: nowrap;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-top: 1px solid #f1f5f9;
}

.showing {
  font-size: 12px;
  color: #6b7280;
}

.pagination {
  display: flex;
  gap: 8px;
}

.page-btn {
  padding: 6px 14px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  background: transparent;
  color: #374151;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.page-btn:hover:not(:disabled) {
  background: #f3f4f6;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
