<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import { gerarRelatorioClientes, type ClienteRelatorioDTO } from '../services/reportService'
import { JobTimeoutError } from '../services/jobService'
import { formatCpf, formatMoeda, isNegativo, mensagemErro } from '../utils/format'

const clients = ref<ClienteRelatorioDTO[]>([])
const carregando = ref(true)
const erro = ref('')

async function carregar() {
  erro.value = ''
  carregando.value = true
  try {
    clients.value = await gerarRelatorioClientes()
  } catch (err) {
    erro.value =
      err instanceof JobTimeoutError
        ? 'O relatório está demorando mais que o esperado. Tente novamente em instantes.'
        : mensagemErro(err, 'Não foi possível gerar o relatório.')
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)

const sortDirection = ref<'asc' | 'desc'>('asc')

function toggleSort() {
  sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
}

// Ordenação crescente por nome do cliente (padrão do relatório)
const sortedClients = computed(() => {
  const list = [...clients.value].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
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
    <Sidebar active="relatorio" role="gerente" />

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
                <tr v-if="carregando">
                  <td colspan="7" class="estado-tabela">Gerando relatório...</td>
                </tr>
                <tr v-else-if="erro">
                  <td colspan="7" class="estado-tabela estado-erro">
                    {{ erro }}
                    <button class="retry-btn" @click="carregar">Tentar novamente</button>
                  </td>
                </tr>
                <tr v-else-if="pagedClients.length === 0">
                  <td colspan="7" class="estado-tabela">Nenhum cliente com conta aberta.</td>
                </tr>
                <tr v-for="client in pagedClients" v-else :key="client.cpf">
                  <td>
                    <span class="cell-name">{{ client.nome }}</span>
                    <span class="cell-email">{{ client.email }}</span>
                  </td>
                  <td class="cell-muted">{{ formatCpf(client.cpf) }}</td>
                  <td class="align-right cell-muted">{{ formatMoeda(client.salario) }}</td>
                  <td><span class="pill">{{ client.nomeGerente ?? "—" }}</span></td>
                  <td class="cell-muted">{{ formatCpf(client.cpfGerente) }}</td>
                  <td class="cell-muted">{{ client.numeroConta }}</td>
                  <td class="align-right" :class="isNegativo(client.saldo) ? 'balance-negative' : 'balance-positive'">
                    {{ formatMoeda(client.saldo) }}
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

.estado-tabela {
  text-align: center;
  color: #6b7280;
  padding: 32px 20px !important;
}

.estado-erro {
  color: #dc2626;
}

.retry-btn {
  margin-left: 12px;
  border: none;
  background: none;
  color: #0d1b2a;
  font-weight: 600;
  text-decoration: underline;
  cursor: pointer;
}
</style>
