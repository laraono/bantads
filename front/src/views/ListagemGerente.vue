<script setup lang="ts">
import Sidebar from '@/components/Sidebar.vue';
import { ref } from 'vue';

const user = {
    fullName: 'Gerente',
    email: 'gerente@bantads.com.br',
    initials: 'GE'
}

const gerentes = ref([
    { id: 1, iniciais: 'AS', nome: 'Ana Silva', email: 'ana.silva@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '12 clientes'},
    { id: 2, iniciais: 'CM', nome: 'Carlos Mendes', email: 'carlos.mendes@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '11 clientes'},
    { id: 3, iniciais: 'BS', nome: 'Beatriz Santos', email: 'beatriz.santos@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '10 clientes'},
    { id: 4, iniciais: 'DO', nome: 'Diego Oliveira', email: 'diego.oliveira@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '9 clientes'},
    { id: 5, iniciais: 'FL', nome: 'Fernanda Lima', email: 'fernanda.lima@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '8 clientes'},
    { id: 6, iniciais: 'GC', nome: 'Gabriel Costa', email: 'gabriel.costa@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '7 clientes'},
    { id: 7, iniciais: 'HR', nome: 'Helena Rocha', email: 'helena.rocha@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '6 clientes'},
    { id: 8, iniciais: 'IM', nome: 'Igor Martins', email: 'igor.martins@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '0 clientes'},
    { id: 9, iniciais: 'JF', nome: 'Juliana Ferreira', email: 'juliana.ferreira@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '4 clientes'},
    { id: 10, iniciais: 'LA', nome: 'Lucas Alves', email: 'lucas.alves@bantads.com', cpf: '123.456.789-00', telefone: '(11) 98765-4321', quantidade_clientes: '3 clientes'}
]);

const filtroSelecionado = ref('all')

</script>

<template>
    <div class="layout">
        <Sidebar active="gerentes" role="gerente" :user="user" />
        <div class="main">
            <header class="top-bar">
                <div class="page-heading">
                    <h1 class="title">Gerentes</h1>
                </div>
                <div class="btn-novo">
                    <v-btn type= "button" class="novo-btn" to="">
                        <v-icon icon="mdi-plus" start/>
                        Novo Gerente
                    </v-btn>
                </div>
            </header>
            <main class="content">
                <div class="filtros">
                    <div class="filtroEstatus">
                        <v-btn-toggle v-model="filtroSelecionado" class="estatus">
                            <v-btn class="buttonStatus" value="all">
                                <span>All</span>
                                <span class="quantidade">10</span>
                            </v-btn>
                            <v-btn class="buttonStatus" value="active">
                                <span>Active</span>
                                <span class="quantidade">9</span>
                            </v-btn>
                            <v-btn class="buttonStatus" value="inactive">
                                <span>Inactive</span>
                                <span class="quantidade">1</span>
                            </v-btn>
                        </v-btn-toggle>
                    </div>
                    <div class="search">
                        <div class="search-manager">
                            <v-card color="surface-light" max-width="400">
                                <v-text-field density="comfortable" flat label="Search managers..." variant="solo" hide-details single-line/>
                            </v-card>
                        </div>
                        <div class="filter-icon">
                            <v-icon icon="mdi-filter-outline" />
                        </div>
                    </div>
                </div>
                <div class="gerente-card">
                    <v-row class="mb-6">
                        <v-col v-for="gerente in gerentes" :key="gerente.id" cols="12" sm="6" md="3">
                            <v-card class="mx-auto fill-height d-flex flex-column" variant="elevated"  border="sm" rounded="xl" density="comfortable">
                                <v-card-title>
                                    <div class="title">
                                        <div class="title-initials">
                                            <span class="initials">{{ gerente.iniciais }}</span>
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
                                        {{ gerente.cpf }} <br />
                                    </div>
                                    <div class="mb-2">
                                        <v-icon icon="mdi-phone-outline" start />
                                        {{ gerente.telefone }} <br />
                                    </div>
                                    <div class="mb-2">
                                        <v-icon icon="mdi-account-multiple-outline" start />
                                        {{ gerente.quantidade_clientes }} <br />
                                    </div>
                                </v-card-text>
                                <div class="">
                                    <v-divider class="mx-4" />
                                </div>
                                <v-card-actions class="justify-end">
                                    <div class="card-actions">
                                        <v-btn variant="text" color="primary" prepend-icon="mdi-eye-outline" to="">
                                            View
                                        </v-btn>
                                        <v-btn variant="text" color="primary" prepend-icon="mdi-pencil-outline" to="">
                                            Edit
                                        </v-btn>
                                    </div>
                                </v-card-actions>
                            </v-card>
                        </v-col>
                    </v-row>
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
</style>