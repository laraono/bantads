<script setup lang="ts">

import { computed, onMounted, ref } from 'vue'

import Sidebar from '../components/Sidebar.vue'
import { listarClientes, type ClienteDTO } from '../services/clientService'
import { formatCpf, formatMoeda, isNegativo, mensagemErro, somenteDigitos } from '../utils/format'

const search = ref('')
const clients = ref<ClienteDTO[]>([])
const carregando = ref(true)
const erro = ref('')

async function carregar() {
  erro.value = ''
  carregando.value = true
  try {
    clients.value = await listarClientes()
  } catch (err) {
    erro.value = mensagemErro(err, 'Não foi possível carregar os clientes.')
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)

// busca por parte do CPF (com ou sem pontuação) ou parte do nome
const filteredClients = computed(() => {
  const value = search.value.toLowerCase().trim()
  if (!value) return clients.value

  const digitos = somenteDigitos(value)
  return clients.value.filter(
    (client) =>
      client.nome.toLowerCase().includes(value) || (digitos !== '' && client.cpf.includes(digitos))
  )
})

</script>
<template>
  <div class="layout">

    <Sidebar
      active="clientes"
      role="gerente"
    />
    <div class="main">
      <header class="topbar">
        <div class="topbar-icon">
          <svg
            viewBox="0 0 24 24"
            class="people-icon"
          >
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
        </div>
        <div class="topbar-text">
          <span class="topbar-title">BANTADS</span>
          <span class="topbar-subtitle">Internet Banking</span>
        </div>
      </header>

      <main class="content">
        <div class="page-heading">
          <h1 class="title">Todos os Clientes</h1>
        </div>
        <p class="subtitle">Lista completa de clientes do banco, ordenada por nome</p>
        <div class="search-box">
          <svg viewBox="0 0 24 24" class="search-icon">
            <circle cx="11" cy="11" r="7" />
            <path d="m20 20-4-4" />
          </svg>

          <input v-model="search" type="text" placeholder="Buscar por CPF ou Nome..."/>

        </div>

        <div class="table-card">

          <table class="table">

            <thead>
              <tr>
                <th>CPF</th>
                <th>Nome</th>
                <th>Cidade</th>
                <th>Estado</th>
                <th class="align-right">Saldo</th>
              </tr>
            </thead>

            <tbody>

              <tr v-if="carregando">
                <td colspan="5" class="estado-tabela">Carregando clientes...</td>
              </tr>
              <tr v-else-if="erro">
                <td colspan="5" class="estado-tabela estado-erro">
                  {{ erro }}
                  <button class="retry-btn" @click="carregar">Tentar novamente</button>
                </td>
              </tr>
              <tr v-else-if="filteredClients.length === 0">
                <td colspan="5" class="estado-tabela">Nenhum cliente encontrado.</td>
              </tr>

              <tr v-for="client in filteredClients" v-else :key="client.cpf">
                <td class="cell-muted">
                  {{ formatCpf(client.cpf) }}
                </td>

                <td class="cell-name">
                  {{ client.nome }}
                </td>

                <td class="cell-muted">
                  {{ client.cidade }}
                </td>

                <td class="cell-muted">
                  {{ client.uf }}
                </td>

                <td
                  class="align-right balance" :class="{ negative: isNegativo(client.saldo) }">
                  {{ formatMoeda(client.saldo) }}
                </td>

              </tr>

            </tbody>

          </table>

          <div class="table-footer">
            Mostrando {{ filteredClients.length }} de {{ clients.length }} clientes
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
  font-family:
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    Helvetica,
    Arial,
    sans-serif;
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

.people-icon {
  width: 21px;
  height: 21px;
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
  padding: 28px 32px 48px;
  max-width: 1180px;
  margin: 0 auto;
  width: 100%;
}

.page-heading {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.subtitle {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 18px;
}

.search-box {
  position: relative;
  width: 100%;
  margin-bottom: 14px;
}

.search-box input {
  width: 100%;
  height: 38px;
  border: 1px solid #d9dee5;
  border-radius: 5px;
  background: #fff;
  padding: 0 14px 0 38px;
  font-size: 12px;
  color: #374151;
  outline: none;
}

.search-box input::placeholder {
  color: #b5bac1;
}

.search-box input:focus {
  border-color: #b9c1cc;
}

.search-icon {
  position: absolute;
  left: 13px;
  top: 11px;
  width: 16px;
  height: 16px;
  fill: none;
  stroke: #9ca3af;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  z-index: 1;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table thead th {
  text-align: left;
  font-size: 10px;
  font-weight: 600;
  color: #6b7280;
  padding: 11px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #edf0f3;
  white-space: nowrap;
}

.table tbody td {
  padding: 11px 20px;
  border-bottom: 1px solid #edf0f3;
  font-size: 11px;
  color: #374151;
  vertical-align: middle;
}

.table tbody tr:last-child td {
  border-bottom: none;
}

.table tbody tr:hover {
  background: #fafafa;
}

.table th.align-right,
.table td.align-right {
  text-align: right;
}

.cell-muted {
  color: #6b7280;
  white-space: nowrap;
}

.cell-name {
  color: #374151;
  font-weight: 500;
  white-space: nowrap;
}

.balance {
  color: #111827;
  font-weight: 600;
  white-space: nowrap;
}

.balance.negative {
  color: #dc2626;
}

.table-footer {
  padding: 11px 20px 13px;
  font-size: 10px;
  color: #9ca3af;
  border-top: 1px solid #f1f5f9;
}

@media (max-width: 900px) {

  .content {
    padding: 20px 16px 32px;
  }
  .table-card {
    overflow-x: auto;
  }
  .table {
    min-width: 750px;
  }

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
