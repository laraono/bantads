<script setup lang="ts">
import { computed } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import { useAccount } from '../composables/useAccount'

const { accountNumber, balance } = useAccount()

const user = {
  name: 'John',
  fullName: 'John Doe',
  initials: 'AD',
  email: 'john@example.com',
  cpf: '321.654.987-00',
  phone: '(41) 9123-4567',
  address: 'Rua Gilberto Squena 1337'
}

const manager = {
  name: 'Maria Silva',
  cpf: '987.654.321-00',
}

const account = {
  creditLimit: 1708095,
  type: 'Conta Corrente',
  agency: '1234',
  status: 'Ativa',
}

const formattedBalance = computed(() => {
  return `R$ ${balance.value.toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`
})

const formattedCreditLimit = computed(() => {
  return `R$ ${account.creditLimit.toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`
})
</script>

<template>
  <div class="layout">

    <Sidebar
      active="config"
      :user="user"
    />

    <main class="main">

      <header class="topbar">
        <div class="topbar-icon">
          <svg
            viewBox="0 0 24 24"
            class="shield-icon"
          >
            <path
              d="M12 2 4 5v6c0 5 3.4 8.9 8 10 4.6-1.1 8-5 8-10V5l-8-3Z"
            />
          </svg>
        </div>

        <div class="topbar-text">
          <span class="topbar-title">BANTADS</span>
          <span class="topbar-subtitle">Internet Banking</span>
        </div>
      </header>

      <div class="content">

        <div class="page-header">
          <h1>Configurações</h1>

          <p>
            Altere suas informações de perfil e conta.
          </p>
        </div>



        <section class="account-card">

          <h2>Informações da conta</h2>

          <div class="account-grid">

            <div class="account-item">

              <span class="account-label">
                Saldo Atual
              </span>

              <strong>
                {{ formattedBalance }}
              </strong>

            </div>

            <div class="account-item">

              <span class="account-label">
                Limite de crédito total
              </span>

              <strong>
                {{ formattedCreditLimit }}
              </strong>

            </div>

          </div>

          <div class="account-client">



          </div>

        </section>

        <section class="info-card">

          <div class="card-header">

            <h2>Informações Pessoais</h2>

            <button class="edit-button">
              Editar Perfil
            </button>

          </div>

          <div class="fields">

            <div class="field">
              <label>Nome Completo</label>

              <div class="field-value">
                {{ user.fullName }}
              </div>
            </div>

            <div class="field">
              <label>E-mail</label>

              <div class="field-value">
                {{ user.email }}
              </div>
            </div>

            <div class="field">
              <label>Telefone</label>

              <div class="field-value">
                {{ user.phone }}
              </div>
            </div>

            <div class="field">
              <label>Endereço</label>

              <div class="field-value">
                {{ user.address }}
              </div>
            </div>

            <div class="field">
              <label>CPF</label>

              <div class="field-value">
                {{ user.cpf }}
              </div>
            </div>

          </div>

        </section>

        <section class="info-card">

          <div class="card-header">
            <h2>Dados da Conta</h2>
          </div>

          <div class="fields two-columns">

            <div class="field">

              <label>Número da conta</label>

              <div class="field-value">
                {{ accountNumber }}
              </div>

            </div>

            <div class="field">

              <label>Agência</label>

              <div class="field-value">
                {{ account.agency }}
              </div>

            </div>

            <div class="field">

              <label>Tipo de conta</label>

              <div class="field-value">
                {{ account.type }}
              </div>

            </div>

            <div class="field">

              <label>Status</label>

              <div class="field-value">
                {{ account.status }}
              </div>

            </div>

          </div>

        </section>

        <section class="info-card">

          <div class="card-header">

            <h2>Gerente responsável</h2>

          </div>

          <div class="fields two-columns">

            <div class="field">

              <label>Nome</label>

              <div class="field-value">
                {{ manager.name }}
              </div>

            </div>

            <div class="field">

              <label>CPF</label>

              <div class="field-value">
                {{ manager.cpf }}
              </div>

            </div>

          </div>

        </section>

      </div>

    </main>

  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.layout {
  min-height: 100vh;
  display: flex;
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
  padding: 28px 32px 48px;
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h1 {
  margin: 0 0 5px;
  font-size: 24px;
  color: #111827;
}

.page-header p {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}

.profile-tab {
  height: 40px;
  background: #e5e7eb;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #334155;
  font-size: 12px;
  margin-bottom: 12px;
}

.profile-tab-icon svg {
  width: 15px;
  height: 15px;
  fill: none;
  stroke: #334155;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.account-card {
  background: #0d1b2a;
  border-radius: 8px;
  padding: 20px 24px;
  color: #fff;
  margin-bottom: 14px;
}

.account-card h2 {
  margin: 0 0 18px;
  font-size: 13px;
}

.account-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.account-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.account-label {
  color: #94a3b8;
  font-size: 10px;
}

.account-item strong {
  font-size: 18px;
}

.account-client {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-top: 18px;
}

.account-client strong {
  font-size: 12px;
}

.account-client span {
  color: #94a3b8;
  font-size: 9px;
}

.info-card {
  background: #fff;
  border-radius: 9px;
  padding: 20px 24px;
  margin-bottom: 14px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  color: #111827;
  font-size: 15px;
}

.edit-button {
  border: none;
  background: transparent;
  color: #7c3aed;
  font-size: 11px;
  cursor: pointer;
}

.fields {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.two-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field label {
  color: #334155;
  font-size: 11px;
}

.field-value {
  min-height: 40px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 7px;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 700px) {
  .content {
    padding: 20px 16px;
  }

  .account-grid,
  .two-columns {
    grid-template-columns: 1fr;
  }
}
</style>
