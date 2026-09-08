import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Cliente from '../views/Cliente.vue'
import Gerente from '../views/Gerente.vue'
import Cadastro from '../views/Cadastro.vue'
import CadastroEndereco from '../views/CadastroEndereco.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/cadastro',
    meta: { requiresAuth: false },
    children: [
      {
        path: '',
        name: 'cadastro-dados-pessoais',
        component: Cadastro
      },
      {
        path: 'endereco',
        name: 'cadastro-endereco-financeiro',
        component: CadastroEndereco
      }
    ]
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { requiresAuth: false }  
  },
  {
    path: '/home',
    name: 'home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/clientes',
    name: 'cliente',
    component: Cliente,
    meta: { requiresAuth: true, role: 'cliente'} // Somente clientes têm acesso
  },
  {
    path: '/gerentes',
    name: 'gerente',
    component: Gerente,
    meta: { requiresAuth: true, role: 'gerente'} // Somente gerentes têm acesso
  },
  {
    path: '/:pathMatch(.*)*', // Qualquer rota que não exista, redireciona para /login
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})


export default router
