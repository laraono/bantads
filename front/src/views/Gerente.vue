<script setup lang="ts">
import { reactive, ref } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import RejectRequestModal from '../components/RejectRequestModal.vue'

type RequestStatus = 'pendente' | 'aprovado' | 'recusado'

interface ApprovalRequest {
  id: number
  cpf: string
  name: string
  salary: number
  requestDate: string
  requestTime: string
  status: RequestStatus
  reason?: string
  decisionDate?: string
  decisionTime?: string
  processing: boolean
}

const user = {
  fullName: 'Gerente',
  email: 'gerente@bantads.com.br',
  initials: 'GE'
}

const requests = reactive<ApprovalRequest[]>([
  { id: 1, cpf: '123.456.789-00', name: 'Ana Silva Santos', salary: 3500.0, requestDate: '14 Oct 2023', requestTime: '10:30', status: 'pendente', processing: false },
  { id: 2, cpf: '234.567.890-11', name: 'Carlos Eduardo Oliveira', salary: 5200.0, requestDate: '14 Oct 2023', requestTime: '11:15', status: 'pendente', processing: false },
  { id: 3, cpf: '345.678.901-22', name: 'Mariana Costa Ferreira', salary: 2800.0, requestDate: '13 Oct 2023', requestTime: '18:45', status: 'aprovado', decisionDate: '14 Oct 2023', decisionTime: '09:10', processing: false },
  { id: 4, cpf: '456.789.012-33', name: 'Roberto Almeida Lima', salary: 7500.0, requestDate: '13 Oct 2023', requestTime: '14:20', status: 'recusado', reason: 'Score de crédito insuficiente', decisionDate: '13 Oct 2023', decisionTime: '16:05', processing: false },
  { id: 5, cpf: '567.890.123-44', name: 'Juliana Pereira Souza', salary: 4100.0, requestDate: '13 Oct 2023', requestTime: '11:30', status: 'pendente', processing: false }
])

function formatNow(): { date: string; time: string } {
  const now = new Date()
  const date = now.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
  const time = now.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  return { date, time }
}

function formatSalary(value: number): string {
  return `R$ ${value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

// Simula o back-end aceitando a decisão de forma assíncrona (202 Accepted + jobId da saga)
// e o front reconsultando o status do job até a saga terminar de processar.
function acceptDecision(): Promise<{ jobId: string }> {
  return new Promise((resolve) => {
    setTimeout(() => resolve({ jobId: `saga-${Math.random().toString(36).slice(2, 10)}` }), 300)
  })
}

function pollJobStatus(jobId: string, attempt: number): Promise<'PROCESSING' | 'DONE'> {
  return new Promise((resolve) => {
    setTimeout(() => resolve(attempt >= 2 ? 'DONE' : 'PROCESSING'), 700)
  })
}

async function pollUntilDone(jobId: string): Promise<void> {
  let attempt = 0
  for (;;) {
    const status = await pollJobStatus(jobId, attempt)
    if (status === 'DONE') return
    attempt += 1
  }
}

async function handleDecision(request: ApprovalRequest, decision: 'aprovado' | 'recusado', reason?: string) {
  if (request.processing) return

  request.processing = true
  try {
    const { jobId } = await acceptDecision() // 202 Accepted { jobId: sagaId }
    await pollUntilDone(jobId)
    request.status = decision
    request.reason = decision === 'recusado' ? reason : undefined
    const { date, time } = formatNow()
    request.decisionDate = date
    request.decisionTime = time
  } finally {
    request.processing = false
  }
}

const rejectModalOpen = ref(false)
const rejectTarget = ref<ApprovalRequest | null>(null)

function openRejectModal(request: ApprovalRequest) {
  rejectTarget.value = request
  rejectModalOpen.value = true
}

async function confirmReject(reason: string) {
  if (rejectTarget.value) {
    await handleDecision(rejectTarget.value, 'recusado', reason)
  }
  rejectModalOpen.value = false
  rejectTarget.value = null
}
</script>

<template>
  <div class="layout">
    <Sidebar active="aprovacoes" role="gerente" :user="user" />

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
        <div class="page-heading">
          <svg viewBox="0 0 24 24" class="heading-icon"><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 3" /></svg>
          <h1 class="title">Solicitações Pendentes de Aprovação</h1>
        </div>
        <p class="subtitle">Clientes aguardando análise e aprovação de cadastro</p>

        <div class="table-card">
          <table class="table">
            <thead>
              <tr>
                <th>CPF</th>
                <th>Nome</th>
                <th class="align-right">Salário</th>
                <th>Data/Hora Solicitação</th>
                <th>Status / Motivo</th>
                <th class="align-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="req in requests" :key="req.id">
                <td class="cell-muted">{{ req.cpf }}</td>
                <td class="cell-name">{{ req.name }}</td>
                <td class="align-right cell-muted">{{ formatSalary(req.salary) }}</td>
                <td class="cell-muted">{{ req.requestDate }}, {{ req.requestTime }}</td>
                <td>
                  <span class="status" :class="`status-${req.status}`">
                    <svg v-if="req.status === 'pendente'" viewBox="0 0 24 24" class="status-icon"><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 3" /></svg>
                    <svg v-else-if="req.status === 'aprovado'" viewBox="0 0 24 24" class="status-icon"><path d="m5 13 4 4L19 7" /></svg>
                    <svg v-else viewBox="0 0 24 24" class="status-icon"><path d="M18 6 6 18" /><path d="m6 6 12 12" /></svg>
                    {{ req.status === 'pendente' ? 'Pendente' : req.status === 'aprovado' ? 'Aprovado' : 'Recusado' }}
                  </span>
                  <span v-if="req.status !== 'pendente' && req.decisionDate" class="decision-date">
                    em {{ req.decisionDate }}, {{ req.decisionTime }}
                  </span>
                  <span v-if="req.reason" class="reason">{{ req.reason }}</span>
                </td>
                <td class="align-right">
                  <div v-if="req.processing" class="processing">
                    <span class="spinner"></span>
                    Processando...
                  </div>
                  <div v-else-if="req.status === 'pendente'" class="actions">
                    <button class="action-btn approve" @click="handleDecision(req, 'aprovado')">
                      <svg viewBox="0 0 24 24" class="action-icon"><path d="m5 13 4 4L19 7" /></svg>
                      Aprovar
                    </button>
                    <button class="action-btn reject" @click="openRejectModal(req)">
                      <svg viewBox="0 0 24 24" class="action-icon"><path d="M18 6 6 18" /><path d="m6 6 12 12" /></svg>
                      Recusar
                    </button>
                  </div>
                  <button v-else class="view-btn" aria-label="Ver detalhes">
                    <svg viewBox="0 0 24 24" class="action-icon"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" /><circle cx="12" cy="12" r="3" /></svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </main>
    </div>

    <RejectRequestModal
      :open="rejectModalOpen"
      :cpf="rejectTarget?.cpf ?? ''"
      :name="rejectTarget?.name ?? ''"
      :salary-label="rejectTarget ? formatSalary(rejectTarget.salary) : ''"
      @close="rejectModalOpen = false"
      @submit="confirmReject"
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
  padding: 28px 32px 48px;
  max-width: 1180px;
  margin: 0 auto;
  width: 100%;
}

.page-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.heading-icon {
  width: 20px;
  height: 20px;
  fill: none;
  stroke: #111827;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
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
  margin: 0 0 20px;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  overflow: hidden;
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
  padding: 14px 20px;
  border-bottom: 1px solid #f1f5f9;
  white-space: nowrap;
}

.table tbody td {
  padding: 14px 20px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
  color: #374151;
  vertical-align: top;
}

.table tbody tr:last-child td {
  border-bottom: none;
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
  color: #111827;
  font-weight: 500;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.status-icon {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2.4;
  stroke-linecap: round;
  stroke-linejoin: round;
  flex-shrink: 0;
}

.status-pendente {
  color: #d97706;
}

.status-aprovado {
  color: #16a34a;
}

.status-recusado {
  color: #dc2626;
}

.reason {
  display: block;
  font-size: 11px;
  color: #dc2626;
  margin-top: 4px;
}

.decision-date {
  display: block;
  font-size: 11px;
  color: #9ca3af;
  margin-top: 2px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: none;
  background: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  white-space: nowrap;
}

.action-icon {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2.4;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.action-btn.approve {
  color: #16a34a;
}

.action-btn.approve:hover {
  color: #15803d;
}

.action-btn.reject {
  color: #dc2626;
}

.action-btn.reject:hover {
  color: #b91c1c;
}

.view-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 0;
}

.view-btn:hover {
  color: #4b5563;
}

.processing {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
}

.spinner {
  width: 12px;
  height: 12px;
  border: 2px solid #e5e7eb;
  border-top-color: #f0c968;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
