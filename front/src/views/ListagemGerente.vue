<script setup lang="ts">
import Sidebar from '@/components/Sidebar.vue';
import { computed, onMounted, ref } from 'vue';
import { useAuth } from '@/composables/useAuth';
import {
    atualizarGerente,
    inserirGerente,
    listarGerentes,
    removerGerente,
    type GerenteDTO
} from '@/services/managerService';
import { JobTimeoutError, resolverAssincrono } from '@/services/jobService';
import { formatCpf, formatTelefone, iniciais, mensagemErro, somenteDigitos } from '@/utils/format';

const { usuario } = useAuth()

const gerentes = ref<GerenteDTO[]>([])
const carregando = ref(true)
const erroCarregamento = ref('')
const busca = ref('')

const snackbar = ref({ aberto: false, texto: '', cor: 'success' })

function avisar(texto: string, cor: 'success' | 'error' | 'warning' = 'success') {
    snackbar.value = { aberto: true, texto, cor }
}


// TO DO tem que fazer a busca usando o que está no job 
// pegando o domínio + o id e aí sim fazendo uma chamada pro back
// porém não sei se faz sentido considerando nossa lógica

async function carregar() {
    erroCarregamento.value = ''
    try {
        gerentes.value = await listarGerentes()
    } catch (err) {
        erroCarregamento.value = mensagemErro(err, 'Não foi possível carregar os gerentes.')
    } finally {
        carregando.value = false
    }
}

onMounted(carregar)

const gerentesFiltrados = computed(() => {
    const valor = busca.value.toLowerCase().trim()
    if (!valor) return gerentes.value
    const digitos = somenteDigitos(valor)
    return gerentes.value.filter(
        (g) =>
            g.nome.toLowerCase().includes(valor) ||
            g.email.toLowerCase().includes(valor) ||
            (digitos !== '' && g.cpf.includes(digitos))
    )
})

function rotuloClientes(qtd: number) {
    return qtd === 1 ? '1 cliente' : `${qtd} clientes`
}

const rules = {
    required: (v: string) => !!v || 'Campo obrigatório',
    nome: (v: string) => v && v.length >= 8 || 'Mínimo de 8 letras',
    cpf: (v: string) => somenteDigitos(v).length === 11 || 'CPF inválido',
    telefone: (v: string) => somenteDigitos(v).length === 11 || 'Telefone inválido',
    email: (v: string) => /.+@.+\..+/.test(v) || 'E-mail inválido',
}

// ---------- R13: inserção ----------
const novoGerente = ref(false)
const salvandoNovo = ref(false)
const formNovo = ref<{ validate: () => Promise<{ valid: boolean }>; reset: () => void } | null>(null)
const novo = ref({ nome: '', cpf: '', telefone: '', email: '', senha: '', confirmarSenha: '' })

const confirmaSenha = (v: string) => v === novo.value.senha || 'As senhas não conferem'

function abrirNovo() {
    novo.value = { nome: '', cpf: '', telefone: '', email: '', senha: '', confirmarSenha: '' }
    novoGerente.value = true
}

async function salvarNovo() {
    const { valid } = (await formNovo.value?.validate()) ?? { valid: false }
    if (!valid) return

    salvandoNovo.value = true
    try {
        const response = await inserirGerente({
            nome: novo.value.nome.trim(),
            cpf: somenteDigitos(novo.value.cpf),
            telefone: somenteDigitos(novo.value.telefone),
            email: novo.value.email.trim(),
            senha: novo.value.senha
        })
        novoGerente.value = false
        await resolverAssincrono(response) // 202 -> polling em /jobs/{id}/status
        avisar(`Gerente ${novo.value.nome} cadastrado.`)
    } catch (err) {
        if (err instanceof JobTimeoutError) {
            avisar('O cadastro do gerente ainda está em processamento.', 'warning')
        } else {
            avisar(mensagemErro(err, 'Não foi possível cadastrar o gerente.'), 'error')
        }
    } finally {
        salvandoNovo.value = false
        await carregar()
    }
}

// ---------- R14: atualização / R15: remoção ----------
const editarGerente = ref(false)
const salvandoEdicao = ref(false)
const formEdicao = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)
const emEdicao = ref<GerenteDTO | null>(null)
const edicao = ref({ nome: '', telefone: '' })

const editandoASiMesmo = computed(() => emEdicao.value?.cpf === usuario.value?.cpf)

function abrirEdicao(gerente: GerenteDTO) {
    emEdicao.value = gerente
    edicao.value = { nome: gerente.nome, telefone: somenteDigitos(gerente.telefone) }
    editarGerente.value = true
}

async function salvarEdicao() {
    if (!emEdicao.value) return
    const { valid } = (await formEdicao.value?.validate()) ?? { valid: false }
    if (!valid) return

    salvandoEdicao.value = true
    try {
        await atualizarGerente(emEdicao.value.cpf, {
            nome: edicao.value.nome.trim(),
            telefone: somenteDigitos(edicao.value.telefone)
        })
        editarGerente.value = false
        avisar('Dados do gerente atualizados.')
        await carregar()
    } catch (err) {
        avisar(mensagemErro(err, 'Não foi possível atualizar o gerente.'), 'error')
    } finally {
        salvandoEdicao.value = false
    }
}

const confirmarRemocao = ref(false)
const removendo = ref(false)

async function remover() {
    const gerente = emEdicao.value
    if (!gerente) return

    removendo.value = true
    try {
        const response = await removerGerente(gerente.cpf)
        confirmarRemocao.value = false
        editarGerente.value = false
        await resolverAssincrono(response)
        avisar(`Gerente ${gerente.nome} removido. Os clientes dele foram redistribuídos.`)
    } catch (err) {
        if (err instanceof JobTimeoutError) {
            avisar('A remoção do gerente ainda está em processamento.', 'warning')
        } else {
            avisar(mensagemErro(err, 'Não foi possível remover o gerente.'), 'error')
        }
    } finally {
        removendo.value = false
        confirmarRemocao.value = false
        await carregar()
    }
}
</script>

<template>
    <div class="layout">
        <Sidebar active="gerentes" role="gerente" />
        <div class="main">
            <header class="top-bar">
                <div class="page-heading">
                    <h1 class="title">Gerentes</h1>
                </div>
                <div class="btn-novo">
                    <v-btn type= "button" class="novo-btn" @click="abrirNovo">
                        <v-icon icon="mdi-plus" start/>
                        Novo Gerente
                    </v-btn>
                </div>
            </header>

            <!-- Componente novo gerente (R13) -->
            <div class="novo-gerente">
                <v-dialog v-model="novoGerente" max-width="500" :persistent="salvandoNovo">
                    <v-card>
                        <div class="header">
                            <div class="titulo">
                                <v-card-title class="text-h5">
                                    Cadastrar Gerente
                                </v-card-title>
                            </div>
                            <div class="icono-fechar">
                                <v-icon icon="mdi-window-close" @click="novoGerente = false"/>
                            </div>
                        </div>

                        <v-divider />

                        <v-card-text>
                            <v-form ref="formNovo" @submit.prevent="salvarNovo">
                                <label class="field-label" for="cadastro-nome">Nome Completo</label>
                                <v-text-field id="cadastro-nome" v-model="novo.nome" class="field-input" variant="outlined" flat single-line density="comfortable" placeholder="João da Silva" type="text" :rules="[rules.required, rules.nome]"/>
                                <div class="primeiro-bloco">
                                    <div class="gerente-cpf">
                                        <label class="field-label" for="cadastro-cpf">CPF</label>
                                        <v-mask-input id="cadastro-cpf" v-model="novo.cpf" class="field-input" variant="outlined" flat single-line density="comfortable" placeholder="000.000.000-00" type="text" :rules="[rules.required, rules.cpf]" mask="###.###.###-##"/>
                                    </div>
                                    <div class="gerente-telefone">
                                        <label class="field-label" for="cadastro-telefone">Telefone</label>
                                        <v-mask-input id="cadastro-telefone" v-model="novo.telefone" class="field-input" variant="outlined" flat single-line density="comfortable" placeholder="(00) 00000-0000" type="text" :rules="[rules.required, rules.telefone]" mask='(##) #####-####'/>
                                    </div>
                                </div>
                                <label class="field-label" for="cadastro-email">E-mail (Campo Único)</label>
                                <v-text-field id="cadastro-email" v-model="novo.email" class="field-input" variant="outlined" flat single-line density="comfortable" placeholder="email@bantads.com" type="email" :rules="[rules.required, rules.email]"/>
                                <div class="segundo-bloco">
                                    <div class="gerente-senha">
                                        <label class="field-label" for="gerente-password">Senha</label>
                                        <v-text-field id="gerente-password" v-model="novo.senha" class="field-input" variant="outlined" flat single-line density="comfortable" type="password" :rules="[rules.required]"/>
                                    </div>
                                    <div class="gerente-conferir-senha">
                                        <label class="field-label" for="confirmar-password">Confirmar Senha</label>
                                        <v-text-field id="confirmar-password" v-model="novo.confirmarSenha" class="field-input" variant="outlined" flat single-line density="comfortable" type="password" :rules="[rules.required, confirmaSenha]"/>
                                    </div>
                                </div>
                            </v-form>
                        </v-card-text>

                        <v-divider class="divider"/>

                        <v-card-actions class="acoes">
                            <v-btn class="cancelar-btn" variant="elevated" :disabled="salvandoNovo" @click="novoGerente = false">
                                Cancelar
                            </v-btn>
                            <v-btn class="salvar-btn" variant="elevated" :loading="salvandoNovo" @click="salvarNovo">
                                Salvar Gerente
                            </v-btn>
                        </v-card-actions>
                    </v-card>
                </v-dialog>
            </div>

            <main class="content">
                <div class="filtros">
                    <div class="resumo">
                        <span class="resumo-total">{{ gerentes.length }}</span>
                        <span class="resumo-label">gerentes ativos</span>
                    </div>
                    <div class="search">
                        <div class="search-manager">
                            <v-card color="surface-light" max-width="400">
                                <v-text-field v-model="busca" density="comfortable" flat label="Buscar por nome, CPF ou e-mail..." variant="solo" prepend-inner-icon="mdi-magnify" hide-details single-line clearable/>
                            </v-card>
                        </div>
                    </div>
                </div>
                <div class="gerente-card">
                    <div v-if="carregando" class="estado-lista">
                        <v-progress-circular indeterminate color="#0b1c34" />
                    </div>
                    <div v-else-if="erroCarregamento" class="estado-lista estado-erro">
                        {{ erroCarregamento }}
                        <v-btn variant="text" @click="carregar">Tentar novamente</v-btn>
                    </div>
                    <div v-else-if="gerentesFiltrados.length === 0" class="estado-lista">
                        Nenhum gerente encontrado.
                    </div>
                    <v-row v-else class="mb-6">
                        <v-col v-for="gerente in gerentesFiltrados" :key="gerente.cpf" cols="12" sm="6" md="3">
                            <v-card class="mx-auto fill-height d-flex flex-column" variant="elevated"  border="sm" rounded="xl" density="comfortable">
                                <v-card-title>
                                    <div class="title">
                                        <div class="title-initials">
                                            <span class="initials">{{ iniciais(gerente.nome) }}</span>
                                        </div>
                                        <div class="title-nome">
                                            <h1>{{ gerente.nome }}</h1>
                                        </div>
                                    </div>
                                </v-card-title>
                                <v-card-text class="flex-grow-1">
                                    <div class="mb-2">
                                        <v-icon icon="mdi-email-outline" start />
                                        {{ gerente.email }} <br />
                                    </div>
                                    <div class="mb-2">
                                        <v-icon icon="mdi-account-outline" start />
                                        {{ formatCpf(gerente.cpf) }} <br />
                                    </div>
                                    <div class="mb-2">
                                        <v-icon icon="mdi-phone-outline" start />
                                        {{ formatTelefone(gerente.telefone) }} <br />
                                    </div>
                                    <div class="mb-2">
                                        <v-icon icon="mdi-account-multiple-outline" start />
                                        {{ rotuloClientes(gerente.quantidadeClientes) }} <br />
                                    </div>
                                </v-card-text>
                                <div class="">
                                    <v-divider class="mx-4" />
                                </div>
                                <v-card-actions class="justify-end">
                                    <div class="card-actions">
                                        <v-btn variant="text" color="primary" prepend-icon="mdi-pencil-outline" @click="abrirEdicao(gerente)">
                                            Editar
                                        </v-btn>
                                    </div>
                                </v-card-actions>
                            </v-card>
                        </v-col>
                    </v-row>
                </div>
            </main>

            <!-- Componente atualizar gerente (R14) / desativar (R15) -->
             <div class="editar-gerente">
                <v-dialog v-model="editarGerente" max-width="500" :persistent="salvandoEdicao || removendo">
                    <v-card>
                        <div class="header">
                            <div class="titulo">
                                <v-card-title class="text-h5">
                                    Atualizar Gerente
                                </v-card-title>
                            </div>
                            <div class="icono-fechar">
                                <v-icon icon="mdi-window-close" @click="editarGerente = false"/>
                            </div>
                        </div>

                        <v-divider />

                        <v-card-text>
                            <v-form ref="formEdicao" @submit.prevent="salvarEdicao">
                                <label class="field-label" for="atualizar-nome">Nome Completo</label>
                                <v-text-field id="atualizar-nome" v-model="edicao.nome" class="field-input" variant="outlined" flat single-line density="comfortable" placeholder="Ex: João da Silva" type="text" :rules="[rules.required, rules.nome]"/>
                                <label class="field-label" for="atualizar-telefone">Telefone</label>
                                <v-mask-input id="atualizar-telefone" v-model="edicao.telefone" class="field-input" variant="outlined" flat single-line density="comfortable" placeholder="(00) 00000-0000" type="text" :rules="[rules.required, rules.telefone]" mask='(##) #####-####'/>
                                <div class="bloco-senha">
                                    <div class="gerente-senha">
                                        <label class="field-label" for="atualizar-cpf">CPF</label>
                                        <v-text-field id="atualizar-cpf" :model-value="formatCpf(emEdicao?.cpf)" class="field-input" variant="outlined" flat single-line density="comfortable" disabled hint="Não pode ser alterado" persistent-hint/>
                                    </div>
                                    <div class="gerente-conferir-senha">
                                        <label class="field-label" for="atualizar-email">E-mail</label>
                                        <v-text-field id="atualizar-email" :model-value="emEdicao?.email" class="field-input" variant="outlined" flat single-line density="comfortable" disabled hint="Não pode ser alterado" persistent-hint/>
                                    </div>
                                </div>
                            </v-form>
                        </v-card-text>

                        <v-divider class="divider"/>

                        <v-card-actions class="acoes">
                            <div class="desativar">
                                <v-btn v-if="!editandoASiMesmo" class="desativar-btn" variant="elevated" :disabled="salvandoEdicao" @click="confirmarRemocao = true">
                                    Desativar
                                </v-btn>
                            </div>
                            <div class="cancelar">
                                <v-btn class="cancelar-btn" variant="elevated" :disabled="salvandoEdicao" @click="editarGerente = false">
                                    Cancelar
                                </v-btn>
                            </div>
                            <div class="salvar">
                                <v-btn class="salvar-btn" variant="elevated" :loading="salvandoEdicao" @click="salvarEdicao">
                                    Salvar
                                </v-btn>
                            </div>
                        </v-card-actions>
                    </v-card>
                </v-dialog>
             </div>

            <!-- Confirmação de remoção (R15) -->
            <v-dialog v-model="confirmarRemocao" max-width="440" :persistent="removendo">
                <v-card>
                    <v-card-title class="text-h6">Desativar gerente?</v-card-title>
                    <v-card-text>
                        <strong>{{ emEdicao?.nome }}</strong> será desativado e perderá o acesso ao sistema.
                        Os {{ rotuloClientes(emEdicao?.quantidadeClientes ?? 0) }} dele serão transferidos
                        para o gerente com menos clientes e avisados por e-mail.
                    </v-card-text>
                    <v-card-actions class="justify-end">
                        <v-btn variant="text" :disabled="removendo" @click="confirmarRemocao = false">Cancelar</v-btn>
                        <v-btn color="error" variant="elevated" :loading="removendo" @click="remover">Desativar</v-btn>
                    </v-card-actions>
                </v-card>
            </v-dialog>

            <v-snackbar v-model="snackbar.aberto" :color="snackbar.cor" timeout="5000" location="top right">
                {{ snackbar.texto }}
            </v-snackbar>

            <!-- Confirmação de remoção (R15) -->
            <v-dialog v-model="confirmarRemocao" max-width="440" :persistent="removendo">
                <v-card>
                    <v-card-title class="text-h6">Desativar gerente?</v-card-title>
                    <v-card-text>
                        <strong>{{ emEdicao?.nome }}</strong> será desativado e perderá o acesso ao sistema.
                        Os {{ rotuloClientes(emEdicao?.quantidadeClientes ?? 0) }} dele serão transferidos
                        para o gerente com menos clientes e avisados por e-mail.
                    </v-card-text>
                    <v-card-actions class="justify-end">
                        <v-btn variant="text" :disabled="removendo" @click="confirmarRemocao = false">Cancelar</v-btn>
                        <v-btn color="error" variant="elevated" :loading="removendo" @click="remover">Desativar</v-btn>
                    </v-card-actions>
                </v-card>
            </v-dialog>

            <v-snackbar v-model="snackbar.aberto" :color="snackbar.cor" timeout="5000" location="top right">
                {{ snackbar.texto }}
            </v-snackbar>

            <!-- Confirmação de remoção (R15) -->
            <v-dialog v-model="confirmarRemocao" max-width="440" :persistent="removendo">
                <v-card>
                    <v-card-title class="text-h6">Desativar gerente?</v-card-title>
                    <v-card-text>
                        <strong>{{ emEdicao?.nome }}</strong> será desativado e perderá o acesso ao sistema.
                        Os {{ rotuloClientes(emEdicao?.quantidadeClientes ?? 0) }} dele serão transferidos
                        para o gerente com menos clientes e avisados por e-mail.
                    </v-card-text>
                    <v-card-actions class="justify-end">
                        <v-btn variant="text" :disabled="removendo" @click="confirmarRemocao = false">Cancelar</v-btn>
                        <v-btn color="error" variant="elevated" :loading="removendo" @click="remover">Desativar</v-btn>
                    </v-card-actions>
                </v-card>
            </v-dialog>

            <v-snackbar v-model="snackbar.aberto" :color="snackbar.cor" timeout="5000" location="top right">
                {{ snackbar.texto }}
            </v-snackbar>

            <!-- Confirmação de remoção (R15) -->
            <v-dialog v-model="confirmarRemocao" max-width="440" :persistent="removendo">
                <v-card>
                    <v-card-title class="text-h6">Desativar gerente?</v-card-title>
                    <v-card-text>
                        <strong>{{ emEdicao?.nome }}</strong> será desativado e perderá o acesso ao sistema.
                        Os {{ rotuloClientes(emEdicao?.quantidadeClientes ?? 0) }} dele serão transferidos
                        para o gerente com menos clientes e avisados por e-mail.
                    </v-card-text>
                    <v-card-actions class="justify-end">
                        <v-btn variant="text" :disabled="removendo" @click="confirmarRemocao = false">Cancelar</v-btn>
                        <v-btn color="error" variant="elevated" :loading="removendo" @click="remover">Desativar</v-btn>
                    </v-card-actions>
                </v-card>
            </v-dialog>

            <v-snackbar v-model="snackbar.aberto" :color="snackbar.cor" timeout="5000" location="top right">
                {{ snackbar.texto }}
            </v-snackbar>

            <!-- Confirmação de remoção (R15) -->
            <v-dialog v-model="confirmarRemocao" max-width="440" :persistent="removendo">
                <v-card>
                    <v-card-title class="text-h6">Desativar gerente?</v-card-title>
                    <v-card-text>
                        <strong>{{ emEdicao?.nome }}</strong> será desativado e perderá o acesso ao sistema.
                        Os {{ rotuloClientes(emEdicao?.quantidadeClientes ?? 0) }} dele serão transferidos
                        para o gerente com menos clientes e avisados por e-mail.
                    </v-card-text>
                    <v-card-actions class="justify-end">
                        <v-btn variant="text" :disabled="removendo" @click="confirmarRemocao = false">Cancelar</v-btn>
                        <v-btn color="error" variant="elevated" :loading="removendo" @click="remover">Desativar</v-btn>
                    </v-card-actions>
                </v-card>
            </v-dialog>

            <v-snackbar v-model="snackbar.aberto" :color="snackbar.cor" timeout="5000" location="top right">
                {{ snackbar.texto }}
            </v-snackbar>
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
  flex-direction: column;
  min-width: 0;
}

.top-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex: 1;
    min-width: 0;
    padding: 28px 32px 0px;
    margin: 0 auto;
    width: 100%;
    max-width: 1180px;
}

.novo-btn {
    background: #0b1c34;
    color: #ffffff;
    border-radius: 8px;
    letter-spacing: normal;
    font-weight: 700;
}

.filtros {
    border-top: 2px solid #eef1f6;
    padding-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex: 1;
    min-width: 0;
    padding: 28px 32px 0px;
    margin: 0 auto;
    width: 100%;
    max-width: 1180px;
    
}

.filtroEstatus {
    display: flex;
    align-items: center;
}

.estatus {
    width: 100%;
    max-width: 585px;
    height: 80px;
    padding: 7px;
    border: 1px solid #dce3eb;
    border-radius: 20px;
    background-color: white;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.08);
    display: flex;
    gap: 8px;
}

.buttonStatus {
    flex: 1;
    height: 64px !important;
    border-radius: 15px !important;
    background: transparent !important;
    color: #60728f !important;
    font-size: 20px;
    font-weight: 500;
    text-transform: none;
    letter-spacing: 0;
    box-shadow: none !important;
}

.buttonStatus.v-btn--active {
    background-color: #f1f5f9 !important;
    color: #172033 !important;
}

.quantidade {
    margin-left: 14px;
    min-width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background-color: #f1f5f9;
    color: #60728f;
    font-size: 16px;
    font-weight: 500;
}

.buttonStatus.v-btn--active .quantidade {
    background-color: white;
    color: #172033;
}

.search {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 16px;
}

.search-manager { 
    width: 270px; 
}


.filter-icon {
    width: 45px;
    height: 45px;
    border-radius: 10px;
    background: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
}

.gerente-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-width: 0;
    padding: 28px 32px 0px;
    margin: 0 auto;
    width: 100%;
    max-width: 1180px;
}

.title {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.initials {
    border-radius: 50%;
    background: #f0c968;
    color: #ffffff;
    width: 36px;
    height: 36px;
    font-weight: 700;
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.title-nome {
    display: flex;
    flex-direction: column;
    min-width: 0;
    flex: 1;
    padding: 0px 12px 0px;
    margin: 0 auto;
}

.title h1 {
    font-size: 24px;
}

.card-actions {
    padding: 28px 32px 0px;
    margin: 0 auto;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    max-width: 1180px;
    min-width: 0;
    flex: 1;
    display: flex;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* CSS modal insertar novo gerente, atualizar gerente */

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 10px 0px;
}

.field-label {
    display: block;
    font-size: 0.85rem;
    font-weight: 600;
    margin-bottom: 6px;
}

.field-input :deep(.v-field) {
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: none;
}

.field-input :deep(.v-field__input) {
    min-height: 44px;
    font-size: 0.9rem;
}

.primeiro-bloco, .segundo-bloco, .bloco-senha {
    display: flex;
    gap: 15px;
}

.gerente-cpf, .gerente-telefone, .gerente-senha, .gerente-conferir-senha {
    display: flex;
    flex-direction: column;
    min-width: 219px;
}


.desativar-btn {
    background: #ba1a1a;
    color: #ffffff;
    border-radius: 8px;
    letter-spacing: normal;
    font-weight: 500;
    text-transform: none;
    margin-right: 206px;
}

.salvar-btn {
    background: #0b1c34;
    color: #ffffff;
    border-radius: 8px;
    letter-spacing: normal;
    font-weight: 500;
    text-transform: none;
}

.cancelar-btn {
    background-color: #ffffff;
    color: #1f2937;
    border-radius: 8px;
    letter-spacing: normal;
    font-weight: 500;
    text-transform: none;
}

.acoes {
    padding: 0px 20px 0px;
    background-color: #f8fafc;
}

.icono-fechar {
    margin-right: 9px;
}


.resumo {
    display: flex;
    align-items: baseline;
    gap: 8px;
    color: #60728f;
}

.resumo-total {
    font-size: 28px;
    font-weight: 700;
    color: #0b1c34;
}

.resumo-label {
    font-size: 15px;
}

.estado-lista {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 48px 0;
    color: #60728f;
}

.estado-erro {
    color: #dc2626;
}

</style>
