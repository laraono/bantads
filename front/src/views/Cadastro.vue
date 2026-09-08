<script setup lang="ts">
import { useCadastro } from '../composables/useCadastro';
import { useRoute, useRouter } from 'vue-router';

const { nome, cpf, telefone, email } = useCadastro()

const route = useRoute()
const router = useRouter()

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
                <span class="etapa">Etapa 1 de 2</span>
                <h2>Dados Pessoais</h2>
                <p class="subtitle">Informe seus dados pessoais e de contato.</p>
                <div class="progresso-barra">
                    <div class="progresso"></div>
                </div>
                <v-form>
                    <label class="field-label" for="cadastro-nome">Nome Completo</label>
                    <v-text-field id="cadastro-nome" v-model="nome" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="Carmem Luisa Oliveira" type="text" />
                    <label class="field-label" for="cadastro-cpf">CPF</label>
                    <v-text-field id="cadastro-cpf" v-model="cpf" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="000.000.000-00" type="text" />
                    <label class="field-label" for="cadastro-telefone">Telefone</label>
                    <v-text-field id="cadastro-telefone" v-model="telefone" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="(41) 99999-9999" type="text" />
                    <label class="field-label" for="cadastro-email">E-mail</label>
                    <v-text-field id="cadastro-email" v-model="email" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="seu@email.com" type="email" />
                    <v-btn type="submit" class="submit-btn" block size="large" to="/cadastro/endereco">
                        Próximo
                        <v-icon icon="mdi-arrow-right" end/>
                    </v-btn>
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
  background-color: #6366f1;
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
  background-color: #334155;
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
    width: 100%;
    height: 10px;
    background-color: #eef1f5;
    border-radius: 10px;
    overflow: hidden;
    margin: 0 0 30px;
}

.progresso {
    width: 50%;
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

.field-input :deep(.v-field) {
    background: #f2f2f2;
    border-radius: 8px;
    box-shadow: none;
}

.field-input :deep(.v-field__input) {
    min-height: 44px;
    font-size: 0.9rem;
}

.submit-btn {
    background: #f4c561 !important;
    color: #101b2d !important;
    font-weight: 700;
    text-transform: none;
    border-radius: 8px;
    letter-spacing: normal;
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
