<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import RejectRequestModal from '../components/RejectRequestModal.vue'
import {
  aprovarSolicitacao,
  listarSolicitacoes,
  rejeitarSolicitacao,
  type SolicitacaoDTO,
  type StatusSolicitacao
} from '../services/requestService'
import { JobTimeoutError, resolverAssincrono } from '../services/jobService'
import { formatCpf, formatDataHora, formatMoeda, mensagemErro } from '../utils/format'

type RequestStatus = 'pendente' | 'aprovado' | 'recusado'

const STATUS_FRONT: Record<StatusSolicitacao, RequestStatus> = {
  PENDENTE: 'pendente',
  APROVADA: 'aprovado',
  NAO_APROVADA: 'recusado'
}

const STATUS_LABEL: Record<RequestStatus, string> = {
  pendente: 'Pendente',
  aprovado: 'Aprovado',
  recusado: 'Não aprovado'
}

const solicitacoes = ref<SolicitacaoDTO[]>([])
const carregando = ref(true)
const erroCarregamento = ref('')
const processando = reactive(new Set<string>())
const aviso = ref<{ tipo: 'sucesso' | 'erro' | 'info'; texto: string } | null>(null)

// pendentes primeiro, depois as já processadas da decisão mais recente para a mais antiga
const linhas = computed(() =>
  [...solicitacoes.value].sort((a, b) => {
    if (a.status === 'PENDENTE' && b.status !== 'PENDENTE') return -1
    if (b.status === 'PENDENTE' && a.status !== 'PENDENTE') return 1
    return (b.dataDecisao ?? '').localeCompare(a.dataDecisao ?? '')
  })
)

// TO DO tem que fazer a busca usando o que está no job 
// pegando o domínio + o id e aí sim fazendo uma chamada pro back
// porém não sei se faz sentido considerando nossa lógica

async function carregar() {
  erroCarregamento.value = ''
  try {
    solicitacoes.value = await listarSolicitacoes()
  } catch (err) {
    erroCarregamento.value = mensagemErro(err, 'Não foi possível carregar as solicitações.')
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)

async function aprovar(req: SolicitacaoDTO) {
  if (processando.has(req.cpf)) return
  processando.add(req.cpf)
  aviso.value = null
  try {
    const response = await aprovarSolicitacao(req.cpf)
    await resolverAssincrono(response) // 202 -> polling em /jobs/{id}/status
    aviso.value = { tipo: 'sucesso', texto: `Cliente ${req.nome} aprovado. A senha foi enviada por e-mail.` }
  } catch (err) {
    if (err instanceof JobTimeoutError) {
      aviso.value = { tipo: 'info', texto: `A aprovação de ${req.nome} ainda está em processamento.` }
    } else {
      aviso.value = { tipo: 'erro', texto: mensagemErro(err, `Não foi possível aprovar ${req.nome}.`) }
    }
  } finally {
    processando.delete(req.cpf)
    await carregar()
  }
}

const rejectModalOpen = ref(false)
const rejectTarget = ref<SolicitacaoDTO | null>(null)

function openRejectModal(req: SolicitacaoDTO) {
  rejectTarget.value = req
  rejectModalOpen.value = true
}

async function confirmReject(motivo: string) {
  const req = rejectTarget.value
  rejectModalOpen.value = false
  rejectTarget.value = null
  if (!req) return

  processando.add(req.cpf)
  aviso.value = null
  try {
    await rejeitarSolicitacao(req.cpf, motivo)
    aviso.value = { tipo: 'sucesso', texto: `Solicitação de ${req.nome} recusada.` }
  } catch (err) {
    aviso.value = { tipo: 'erro', texto: mensagemErro(err, `Não foi possível recusar ${req.nome}.`) }
  } finally {
    processando.delete(req.cpf)
    await carregar()
  }
}
</script>

<template>
  <div class="layout">
    <Sidebar active="aprovacoes" role="gerente" />

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
          <h1 class="title">Solicitações de Autocadastro</h1>
        </div>
        <p class="subtitle">Clientes aguardando análise e histórico de solicitações processadas</p>

        <div v-if="aviso" class="aviso" :class="`aviso-${aviso.tipo}`">
          <span>{{ aviso.texto }}</span>
          <button class="aviso-fechar" aria-label="Fechar" @click="aviso = null">×</button>
        </div>

        <div class="table-card">
          <table class="table">
            <thead>
              <tr>
                <th>CPF</th>
                <th>Nome</th>
                <th class="align-right">Salário</th>
                <th>Status / Motivo</th>
                <th class="align-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="carregando">
                <td colspan="5" class="estado-tabela">
                  <span class="spinner"></span>
                  Carregando solicitações...
                </td>
              </tr>
              <tr v-else-if="erroCarregamento">
                <td colspan="5" class="estado-tabela estado-erro">
                  {{ erroCarregamento }}
                  <button class="action-btn retry" @click="carregar">Tentar novamente</button>
                </td>
              </tr>
              <tr v-else-if="linhas.length === 0">
                <td colspan="5" class="estado-tabela">Nenhuma solicitação de autocadastro.</td>
              </tr>
              <template v-else>
              <tr v-for="req in linhas" :key="req.cpf">
                <td class="cell-muted">{{ formatCpf(req.cpf) }}</td>
                <td class="cell-name">{{ req.nome }}</td>
                <td class="align-right cell-muted">{{ formatMoeda(req.salario) }}</td>
                <td>
                  <span class="status" :class="`status-${STATUS_FRONT[req.status]}`">
                    <svg v-if="req.status === 'PENDENTE'" viewBox="0 0 24 24" class="status-icon"><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 3" /></svg>
                    <svg v-else-if="req.status === 'APROVADA'" viewBox="0 0 24 24" class="status-icon"><path d="m5 13 4 4L19 7" /></svg>
                    <svg v-else viewBox="0 0 24 24" class="status-icon"><path d="M18 6 6 18" /><path d="m6 6 12 12" /></svg>
                    {{ STATUS_LABEL[STATUS_FRONT[req.status]] }}
                  </span>
                  <span v-if="req.status !== 'PENDENTE' && req.dataDecisao" class="decision-date">
                    em {{ formatDataHora(req.dataDecisao) }}
                  </span>
                  <span v-if="req.status === 'NAO_APROVADA' && req.motivo" class="reason">{{ req.motivo }}</span>
                </td>
                <td class="align-right">
                  <div v-if="processando.has(req.cpf)" class="processing">
                    <span class="spinner"></span>
                    Processando...
                  </div>
                  <div v-else-if="req.status === 'PENDENTE'" class="actions">
                    <button class="action-btn approve" @click="aprovar(req)">
                      <svg viewBox="0 0 24 24" class="action-icon"><path d="m5 13 4 4L19 7" /></svg>
                      Aprovar
                    </button>
                    <button class="action-btn reject" @click="openRejectModal(req)">
                      <svg viewBox="0 0 24 24" class="action-icon"><path d="M18 6 6 18" /><path d="m6 6 12 12" /></svg>
                      Recusar
                    </button>
                  </div>
                </td>
              </tr>
              </template>
            </tbody>
          </table>
        </div>
      </main>
    </div>

    <RejectRequestModal
      :open="rejectModalOpen"
      :cpf="rejectTarget ? formatCpf(rejectTarget.cpf) : ''"
      :name="rejectTarget?.nome ?? ''"
      :salary-label="rejectTarget ? formatMoeda(rejectTarget.salario) : ''"
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

.aviso {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  margin-bottom: 16px;
}

.aviso-sucesso {
  background: #dcfce7;
  color: #166534;
}

.aviso-erro {
  background: #fee2e2;
  color: #991b1b;
}

.aviso-info {
  background: #fef3c7;
  color: #92400e;
}

.aviso-fechar {
  border: none;
  background: none;
  font-size: 18px;
  line-height: 1;
  color: inherit;
  cursor: pointer;
}

.estado-tabela {
  text-align: center;
  color: #6b7280;
  padding: 32px 20px !important;
}

.estado-tabela .spinner {
  display: inline-block;
  vertical-align: middle;
  margin-right: 8px;
}

.estado-erro {
  color: #dc2626;
}

.action-btn.retry {
  margin-left: 12px;
  color: #0d1b2a;
  text-decoration: underline;
}
</style>
