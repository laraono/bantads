<template>
<<<<<<< Updated upstream
    <div class="about">
        <h1>This is a login page</h1>
=======
    <div class="login-page">
        <aside class="login-hero">
            <div class="brand">
                <div class="brand-badge">
                    <v-icon icon="mdi-shield-check" size="20" color="#101B2D"></v-icon>
                </div>
                <span class="brand-name">BANTADS</span>
            </div>
            <div class="hero-copy">
                <h1>Banco Digital</h1>
                <p class="hero-tagline">Seu dinheiro, seus planos,<br/> seu futuro.</p>
                <p class="hero-desc">Gerencie suas finanças com inteligência, segurança e praticidade - de qualquer lugar.</p>
                <ul class="hero-features">
                    <li>Transações em tempo real</li>
                    <li>Segurança de nível bancário</li>
                    <li>Suporte 24 horas</li>
                </ul>
            </div>
            <div class="hero-footer">
                <hr/>
                <span>Saldo consolidado diariamente</span>
            </div>
        </aside>
        <main class="login-panel">
            <div class="login-form">
                <h2>Bem-vindo de volta</h2>
                <p class="subtitle">Informe seus dados para acessar o sistema.</p>
                <div class="role-toggle" role="tablist">
                    <button type="button" role="tab" :aria-selected="role === 'cliente'" :class="{ active: role === 'cliente' }" @click="role = 'cliente'">
                        Cliente
                    </button>
                    <button type="button" role="tab" :aria-selected="role === 'gerente'" :class="{ active: role === 'gerente' }" @click="role = 'gerente'">
                        Gerente
                    </button>
                </div>
                <v-form @submit.prevent="handleSubmit">
                    <label class="field-label" for="login-email">E-mail</label>
                    <v-text-field id="login-email" v-model="email" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="seu@email.com" type="email" :rules="[rules.required, rules.email]"/>
                    <label class="field-label" for="login-password">Senha</label>
                    <v-text-field id="login-password" v-model="password" class="field-input" variant="solo" flat single-line density="comfortable" placeholder="........" :type="showPassword ? 'text' : 'password'" :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'" :rules="[rules.required]" @click:append-inner="showPassword = !showPassword"/>
                    <div class="row-between">
                        <v-checkbox v-model="remember" label="Lembrar-me" density="compact" hide-details color="primary"/>
                        <a href="#" class="link">Esqueci a senha</a>
                    </div>
                    <v-alert v-if="errorMessage" type="error" density="compact" class="mb-4">
                        {{ errorMessage }}
                    </v-alert>
                    <v-btn type="submit" class="submit-btn" block size="large" :loading="loading">
                        Entrar
                        <v-icon icon="mdi-arrow-right" end/>
                    </v-btn>
                </v-form>
                <p class="signup-text">Não tem conta?<a href="/cadastro" class="link-strong">Cadastre-se</a></p>
            </div>
        </main>
>>>>>>> Stashed changes
    </div>
</template>