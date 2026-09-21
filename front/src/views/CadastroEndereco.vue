<script setup lang="ts">
import { clientService, requestService } from '@/services';
import { resetCadastro, useCadastro } from '../composables/useCadastro';
import { ref } from 'vue';import { useRoute, useRouter } from 'vue-router';

const {
  nome, cpf, telefone, email,
  cep, numero, rua, complemento, cidade, estado, salario,
} = useCadastro()
const estados = ['AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO']
const mensagemAlerta = ref(false)

const route = useRoute()
const router = useRouter()

const rules = {
    required: (v: string) => !!v || 'Campo obrigatório',
}

const form = ref()
const submitting = ref(false)
const submitError = ref('')

const maskSalario = (v: string) => {

    let valor = v.replace(/\D/g, '');

    if (!valor) {
        salario.value = '';
        return;
    }

    valor = valor.padStart(3, '0');

    const centavos = valor.slice(-2);
    let reais = valor.slice(0, -2);

    reais= reais.replace(/^0+(?=\d)/, '');
    
    reais = reais.replace(/\B(?=(\d{3})+(?!\d))/g,
        '.'
    );

    salario.value = `${reais},${centavos}`;
};

async function submit() {
  submitError.value = ''

  submitting.value = true
  try {
    clientService.insert({
        id: 0,
        name: nome.value,
        cpf: cpf.value.replace(/\D/g, ''),
        email: email.value,
        salary: Number(salario.value),
        address: {
            street: rua.value,
            number: Number(numero.value),
            city: cidade.value,
            state: estado.value ?? '',
            cep: cep.value,
        },
        deleted: false
    })

    const request = requestService.createFromForm({
      name: nome.value,
      cpf: cpf.value.replace(/\D/g, ''),
      email: email.value,
      cep: cep.value,
      street: rua.value,
      number: numero.value,
      complement: complemento.value,
      city: cidade.value,
      state: estado.value ?? '',
      salary: Number(salario.value),
    })

    resetCadastro()

    router.push({ path: '/cadastro/sucesso', query: { request: String(request.id) } })
  } catch (e) {
    submitError.value = e instanceof Error ? e.message : 'Erro ao enviar solicitação'
  } finally {
    submitting.value = false
  }
}

function envioDados() {
    submit()
    mensagemAlerta.value = true
    salario.value = ''
}

</script>

<template>
    <div class="cadastro-page">
        <aside class="cadastro-hero">
            <div class="brand">
                <div class="brand-badge">
                    <v-icon icon="mdi-shield-check" size="20" color="#101B2D"></v-icon>
                </div>
                <span class="brand-name">BANTADS</span>
            </div>
            <div class="hero-copy">
                <h1>Abra sua conta<br/><span class="text">em minutos</span></h1>
                <p class="hero-desc">Preencha o formulário e aguarde a aprovação.<br/> Sua senha será enviada por e-mail após a análise</p>
                <ol class="hero-features">
                    <li>Dados Pessoais</li>
                    <span class="dados">Nome, CPF, e-mail e contato</span>
                    <li>Endereço e Financeiro</li>
                    <span class="dados">Localização e salário mensal</span>
                </ol>
            </div>
            <div class="hero-footer">
                <hr />
            </div>
        </aside>
        <main class="cadastro-panel">
            <div class="cadastro-form">
                <span class="etapa">Etapa 2 de 2</span>
                <h2>Endereço e Financeiro</h2>
                <p class="subtitle">Informe seu endereço e renda mensal.</p>
                <div class="progresso-barra">
                    <div class="progresso"></div>
                </div>
                <v-form>
                    <div class="container-endereco">
                        <div class="endereco">
                            <label class="field-label" for="cadastro-cep">CEP</label>
                            <v-mask-input id="cadastro-cep" v-model="cep" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="00000-000" type="text" :rules="[rules.required]" mask="#####-###"/>
                        </div>
                        <div class="endereco">
                            <label class="field-label" for="cadastro-numero">Número</label>
                            <v-text-field id="cadastro-numero" v-model="numero" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="123" type="text" :rules="[rules.required]"/>
                        </div>
                    </div>
                    <div class="label-field">
                        <label class="field-label" for="cadastro-rua">Rua/Logradouro</label>
                        <v-text-field id="cadastro-rua" v-model="rua" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="Rua Joaquim Theodoro" type="text" :rules="[rules.required]"/>
                    </div>
                    <div class="label-field">
                        <label class="field-label" for="cadastro-complemento">Complemento</label>
                        <v-text-field id="cadastro-complemento" v-model="complemento" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="Apto 42, Bloco B" type="text" />
                    </div>
                    <div class="container-regiao">
                        <div class="localizacao">
                            <label class="field-label" for="cadastro-cidade">Cidade</label>
                            <v-text-field id="cadastro-cidade" v-model="cidade" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="Curitiba" type="text" :rules="[rules.required]"/>
                        </div>
                        <div class="localizacao">
                            <label class="field-label" for="cadastro-estado">Estado</label>
                            <v-select id="cadastro-estado" v-model="estado" class="field-input" variant="solo" flat density="comfortable" :items="estados" label="Selecione" :rules="[rules.required]">
                            </v-select>
                        </div>
                    </div>
                    <div class="label-field">
                        <label class="field-label" for="cadastro-salario">Salário Mensal</label>
                        <v-text-field id="cadastro-salario" :model-value="salario" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="0,00" prefix="R$" type="text" autocomplete="off" :rules="[rules.required]" @update:model-value="maskSalario"/>
                    </div>
                    <div class="container-btn">
                        <v-btn type="button" class="voltar-btn" size="large" to="/cadastro">
                            <v-icon icon="mdi-arrow-left" start/>
                            Voltar
                        </v-btn>
                        <v-btn type="submit" class="submit-btn" block size="largue" @click="envioDados">
                            Enviar Cadastro
                            <v-icon class="separacao" icon="mdi-arrow-right" end/>
                        </v-btn>
                        <v-snackbar v-model="mensagemAlerta" color="success" timeout="3000" location="top">
                            Os dados foram enviados com sucesso.
                        </v-snackbar>
                    </div>
                </v-form>
                <p class="signin-text">Já tem conta?<a href="/login" class="link-strong">Entrar</a></p>
            </div>
        </main>
    </div>
</template>

<style scoped>
.cadastro-page {
    min-height: 100vh;
    display: flex;
}

.cadastro-hero {
    width: 42%;
    min-width: 360px;
    background: #101b2d;
    color: #ffffff;
    padding: 48px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.brand {
    display: flex;
    align-items: center;
    gap: 12px;
}

.brand-badge {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: #f4c561;
    display: flex;
    align-items: center;
    justify-content: center;
}

.brand-name {
    font-weight: 700;
    letter-spacing: 0.08em;
    font-size: 0.95rem;
}

.hero-copy h1 {
    font-size: 1.9rem;
    font-weight: 700;
    margin: 0 0 50px;
}

.text {
    color: #f4c561;
}

.hero-desc {
    font-size: 0.95rem;
    line-height: 1.5;
    margin: 0 0 40px;
    max-width: 340px;
}

.hero-features {
    padding: 0;
    margin: 0;
    font-size: 0.9rem;
    list-style-type: none;
    counter-reset: minha-lista;
    display: grid;
    grid-template-columns: auto 1fr;
    row-gap: 4px;
    column-gap: 40px;
}

.hero-features li, 
.hero-features .dados {
  grid-column: 2;
}

.hero-features .dados {
  margin-bottom: 20px;         
  color: #666666;
  font-size: 14px;
}

.hero-features li {
  counter-increment: minha-lista;
  font-weight: bold;
  position: relative;
}

.hero-features li::before {
  content: counter(minha-lista);
  grid-column: 1;
  position: absolute;
  right: calc(100% + 15px);
  top: 50%;
  transform: translateY(-50%);
  background-color: #22c55e;
  color: #ffffff;
  border-radius: 50%;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.hero-features li:nth-of-type(2)::before {
  background-color: #6366f1;
}

.dados {
    color: #9aa5b1;
}

.hero-footer {
    color: #6b7684;
    font-size: 0.75rem;
    letter-spacing: 0.08em;
}

.hero-footer hr {
    border: none;
    border-top: 1px solid #263447;
    margin-bottom: 16px;
}

.cadastro-panel {
    flex: 1;
    background: #faf8f4;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
}

.cadastro-form {
    width: 100%;
    max-width: 400px;
}

.etapa {
  font-size: 0.85rem;
  border-radius: 8px;
  background-color: #eef2ff;
  padding: 5px 16px;
  border-radius: 40px;
  display: inline-block;
  margin: 0 0 10px;
  text-align: center;
  height: 30px;
  width: 28%;
  font-weight: 500;  
}

.cadastro-form h2 {
    color: #101b2d;
    font-weight: 700;
    font-size: 1.5rem;
    margin: 0 0 4px;
}

.subtitle {
    color: #6b7280;
    font-size: 0.9rem;
    margin: 0 0 24px;
}

.progresso-barra {
    min-width: 128%;
    height: 10px;
    background-color: #eef1f5;
    border-radius: 10px;
    overflow: hidden;
    margin: 0 0 30px;
}

.progresso {
    width: 100%;
    height: 100%;
    background-color: #ffdc9a;
    border-radius: 999px;
}

.field-label {
    display: block;
    font-size: 0.85rem;
    font-weight: 600;
    color: #101b2d;
    margin-bottom: 6px;
}

.label-field {
    min-width: 129%;
}

.field-input :deep(.v-field) {
    background: #f2f2f2;
    border-radius: 8px;
    box-shadow: none;
}

.field-input :deep(.v-field__input) {
    min-height: 44px;
    font-size: 0.9rem;
}

.field-input :deep(.v-label) {
    font-size: 0.9rem;
}

.container-endereco, .container-regiao {
    display: flex;
    gap: 15px;
}

.endereco, .localizacao {
    display: flex;
    flex-direction: column;
    min-width: 250px;
}

/* Posiciona os botões horizontalmente, lado a lado, e define um gap (espaço) de 10px entre eles.*/
.container-btn {
    display: flex;
    gap: 10px;
}

.voltar-btn {
    border-radius: 8px;
    letter-spacing: normal;
}

.submit-btn {
    background: #f4c561 !important;
    color: #101b2d !important;
    font-weight: 700;
    text-transform: none;
    border-radius: 8px;
    letter-spacing: normal;
}

/* Separação da flecha com o texto */
.separacao {
    margin: 0px 10px 0px;
}

.signin-text {
    text-align: center;
    color: #6b7280;
    font-size: 0.85rem;
    margin-top: 20px;
}

.link-strong {
    color: #101b2d;
    font-weight: 700;
    text-decoration: none;
    margin-left: 5px;
}

.link-strong:hover {
    text-decoration: underline;
}

@media (max-width: 860px) {
    .cadastro-page {
        flex-direction: column;
    }

    .cadastro-hero {
        width: 100%;
        min-width: 0;
    }
}
</style>