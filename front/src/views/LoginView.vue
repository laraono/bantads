<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login, type UserRole } from '@/services/authService'
import { useAuth } from '@/composables/useAuth'

const role = ref<UserRole>('cliente')
const email = ref('')
const password = ref('')
const remember = ref(false)
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')

const route = useRoute()
const router = useRouter()
const { setToken } = useAuth()

const rules = {
  required: (v: string) => !!v || 'Campo obrigatório',
  email: (v: string) => /.+@.+\..+/.test(v) || 'E-mail inválido',
}

async function handleSubmit() {
  errorMessage.value = ''
  loading.value = true
  try {
    const { token } = await login({ email: email.value, password: password.value, role: role.value })
    setToken(token)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.push(redirect)
  } catch {
    errorMessage.value = 'E-mail ou senha inválidos.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <aside class="login-hero">
      <div class="brand">
        <div class="brand-badge">
          <v-icon icon="mdi-shield-check" size="20" color="#101B2D" />
        </div>
        <span class="brand-name">BANTADS</span>
      </div>

      <div class="hero-copy">
        <h1>Banco Digital</h1>
        <p class="hero-tagline">Seu dinheiro, seus planos,<br />seu futuro.</p>
        <p class="hero-desc">
          Gerencie suas finanças com inteligência, segurança e praticidade — de qualquer lugar.
        </p>
        <ul class="hero-features">
          <li>Transações em tempo real</li>
          <li>Segurança de nível bancário</li>
          <li>Suporte 24 horas</li>
        </ul>
      </div>

      <div class="hero-footer">
        <hr />
        <span>Saldo consolidado diariamente</span>
      </div>
    </aside>

    <main class="login-panel">
      <div class="login-form">
        <h2>Bem-vindo de volta</h2>
        <p class="subtitle">Informe seus dados para acessar o sistema.</p>

        <div class="role-toggle" role="tablist">
          <button
            type="button"
            role="tab"
            :aria-selected="role === 'cliente'"
            :class="{ active: role === 'cliente' }"
            @click="role = 'cliente'"
          >
            Cliente
          </button>
          <button
            type="button"
            role="tab"
            :aria-selected="role === 'gerente'"
            :class="{ active: role === 'gerente' }"
            @click="role = 'gerente'"
          >
            Gerente
          </button>
        </div>

        <v-form @submit.prevent="handleSubmit">
          <label class="field-label" for="login-email">E-mail</label>
          <v-text-field
            id="login-email"
            v-model="email"
            class="field-input"
            variant="solo"
            flat
            single-line
            density="comfortable"
            placeholder="seu@email.com"
            type="email"
            :rules="[rules.required, rules.email]"
          />

          <label class="field-label" for="login-password">Senha</label>
          <v-text-field
            id="login-password"
            v-model="password"
            class="field-input"
            variant="solo"
            flat
            single-line
            density="comfortable"
            placeholder="••••••••"
            :type="showPassword ? 'text' : 'password'"
            :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            :rules="[rules.required]"
            @click:append-inner="showPassword = !showPassword"
          />

          <div class="row-between">
            <v-checkbox
              v-model="remember"
              label="Lembrar-me"
              density="compact"
              hide-details
              color="primary"
            />
            <a href="#" class="link">Esqueci a senha</a>
          </div>

          <v-alert v-if="errorMessage" type="error" density="compact" class="mb-4">
            {{ errorMessage }}
          </v-alert>

          <v-btn type="submit" class="submit-btn" block size="large" :loading="loading">
            Entrar
            <v-icon icon="mdi-arrow-right" end />
          </v-btn>
        </v-form>

        <p class="signup-text">Não tem conta? <a href="#" class="link-strong">Cadastre-se</a></p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
}

.login-hero {
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
  margin: 0 0 8px;
}

.hero-tagline {
  color: #f4c561;
  font-weight: 700;
  font-size: 1.4rem;
  line-height: 1.3;
  margin: 0 0 24px;
}

.hero-desc {
  color: #9aa5b1;
  font-size: 0.95rem;
  line-height: 1.5;
  margin: 0 0 20px;
  max-width: 320px;
}

.hero-features {
  list-style: none;
  padding: 0;
  margin: 0;
  color: #cbd3db;
  font-size: 0.9rem;
}

.hero-features li {
  padding: 4px 0;
}

.hero-features li::before {
  content: '• ';
  color: #f4c561;
}

.hero-footer {
  color: #6b7684;
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-footer hr {
  border: none;
  border-top: 1px solid #263447;
  margin-bottom: 16px;
}

.login-panel {
  flex: 1;
  background: #faf8f4;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.login-form {
  width: 100%;
  max-width: 400px;
}

.login-form h2 {
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

.role-toggle {
  display: flex;
  background: #eceef0;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 24px;
}

.role-toggle button {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #6b7280;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.role-toggle button.active {
  background: #ffffff;
  color: #101b2d;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
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

.row-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.link {
  color: #6b7280;
  font-size: 0.85rem;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.submit-btn {
  background: #f4c561 !important;
  color: #101b2d !important;
  font-weight: 700;
  text-transform: none;
  border-radius: 8px;
  letter-spacing: normal;
}

.signup-text {
  text-align: center;
  color: #6b7280;
  font-size: 0.85rem;
  margin-top: 20px;
}

.link-strong {
  color: #101b2d;
  font-weight: 700;
  text-decoration: none;
}

.link-strong:hover {
  text-decoration: underline;
}

@media (max-width: 860px) {
  .login-page {
    flex-direction: column;
  }

  .login-hero {
    width: 100%;
    min-width: 0;
  }
}
</style>
