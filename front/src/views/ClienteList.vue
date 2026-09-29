<script setup lang="ts">

import { computed, ref } from 'vue'

import Sidebar from '../components/Sidebar.vue'

interface Client {
  cpf: string
  name: string
  city: string
  state: string
  balance: number
}

const user = {
  fullName: 'Gerente',
  email: 'gerente@bantads.com.br',
  initials: 'GE'
}

const search = ref('')

const clients: Client[] = [
  {
    cpf: '111.222.333-44',
    name: 'Amanda Rodrigues Silva',
    city: 'São Paulo',
    state: 'SP',
    balance: 5200.50
  },
  {
    cpf: '222.333.444-55',
    name: 'Bruno Henrique Costa',
    city: 'Rio de Janeiro',
    state: 'RJ',
    balance: -850.00
  },
  {
    cpf: '333.444.555-66',
    name: 'Camila Fernandes Souza',
    city: 'Belo Horizonte',
    state: 'MG',
    balance: 12400.75
  },
  {
    cpf: '444.555.666-77',
    name: 'Daniel Alves Pereira',
    city: 'Curitiba',
    state: 'PR',
    balance: 320.00
  },
  {
    cpf: '555.666.777-88',
    name: 'Eduarda Santos Lima',
    city: 'Porto Alegre',
    state: 'RS',
    balance: 8900.25
  },
  {
    cpf: '666.777.888-99',
    name: 'Felipe Oliveira Martins',
    city: 'Brasília',
    state: 'DF',
    balance: -1200.50
  },
  {
    cpf: '777.888.999-00',
    name: 'Gabriela Mendes Rocha',
    city: 'Salvador',
    state: 'BA',
    balance: 3450.00
  },
  {
    cpf: '888.999.000-11',
    name: 'Henrique Barbosa Dias',
    city: 'Fortaleza',
    state: 'CE',
    balance: 1850.75
  },
  {
    cpf: '999.000.111-22',
    name: 'Isabela Carvalho Nunes',
    city: 'Recife',
    state: 'PE',
    balance: 7200.00
  },
  {
    cpf: '000.111.222-33',
    name: 'João Pedro Araújo',
    city: 'Manaus',
    state: 'AM',
    balance: -450.25
  },
  {
    cpf: '111.333.555-77',
    name: 'Larissa Gomes Ferreira',
    city: 'Goiânia',
    state: 'GO',
    balance: 2100.50
  },
  {
    cpf: '222.444.666-88',
    name: 'Marcelo Teixeira Santos',
    city: 'Belém',
    state: 'PA',
    balance: 9800.00
  },
  {
    cpf: '333.555.777-99',
    name: 'Natália Ribeiro Alves',
    city: 'São Luís',
    state: 'MA',
    balance: 680.75
  },
  {
    cpf: '444.666.888-00',
    name: 'Otávio Moreira Castro',
    city: 'Natal',
    state: 'RN',
    balance: 6300.25
  },
  {
    cpf: '555.777.999-11',
    name: 'Patrícia Vieira Lopes',
    city: 'João Pessoa',
    state: 'PB',
    balance: -320.00
  }
]

const filteredClients = computed(() => {
  const value = search.value.toLowerCase().trim()
  
  if (!value) {return clients}
  return clients.filter(client =>client.cpf.toLowerCase().includes(value)||client.name.toLowerCase().includes(value)
  )
})

function formatBalance(value: number): string {
  const formatted = Math.abs(value).toLocaleString('pt-BR', 
  {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })

  return value < 0 ? `- R$ ${formatted}` : `R$ ${formatted}`
}

</script>
<template>
  <div class="layout">

    <Sidebar
      active="clientes"
      role="gerente"
      :user="user"
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
        <p class="subtitle">Lista completa de clientes sob sua gestão</p>
        <div class="search-box">
          <svg viewBox="0 0 24 24"class="search-icon">
            <circle cx="11" cy="11" r="7" />
            <path d="m20 20-4-4" />
          </svg>

          <input v-model="search"type="text"placeholder="Buscar por CPF ou Nome..."/>

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

              <tr v-for="client in filteredClients":key="client.cpf">
                <td class="cell-muted">
                  {{ client.cpf }}
                </td>

                <td class="cell-name">
                  {{ client.name }}
                </td>

                <td class="cell-muted">
                  {{ client.city }}
                </td>

                <td class="cell-muted">
                  {{ client.state }}
                </td>

                <td
                  class="align-right balance":class="{ negative: client.balance < 0 }">
                  {{ formatBalance(client.balance) }}
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

</style>