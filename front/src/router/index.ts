import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuth } from '../composables/useAuth'

import Login from '../views/Login.vue'
import HomeCliente from '../views/HomeCliente.vue'
import ExtratoDetalhado from '../views/ExtratoDetalhado.vue'
import HomeGerente from '../views/HomeGerente.vue'
import Cadastro from '../views/Cadastro.vue'
import CadastroEndereco from '../views/CadastroEndereco.vue'
import ListagemGerente from '@/views/ListagemGerente.vue'

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
    component: HomeCliente,
    meta: { requiresAuth: true, role: 'cliente' } // Somente clientes têm acesso
  },
  {
    path: '/extrato',
    name: 'extrato',
    component: ExtratoDetalhado,
    meta: { requiresAuth: true }
  },
  {
    path: '/gerentes',
    name: 'gerente',
    component: HomeGerente,
    meta: { requiresAuth: true, role: 'gerente'} // Somente gerentes têm acesso
  },
  {
    path: '/listagem',
    name: 'listagem-gerentes',
    component: ListagemGerente,
    meta: { requiresAuth: true }
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

// Configuração de Guard Global
router.beforeEach((to) => {
  const auth = useAuth()
  const isAuthenticated = auth.isAuthenticated()
  const userRole = auth.getUserRole()

  if (to.meta.requiresAuth && !isAuthenticated) {
    return { name: 'login'}
  }

  const homeRoute = userRole === 'gerente' ? 'gerente' : 'home'

  if (to.name === 'login' && isAuthenticated) {
    return { name: homeRoute }
  }

  if (to.meta.role && to.meta.role !== userRole) {
    return { name: homeRoute }
  }
})


export default router
