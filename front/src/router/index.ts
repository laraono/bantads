import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuth } from '../composables/useAuth'

import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Cliente from '../views/Cliente.vue'
import Gerente from '../views/Gerente.vue'
import Cadastro from '../views/Cadastro.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/cadastro/usuario',
    name: 'cadastro',
    component: Cadastro,
    meta: { requiresAuth: false }
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

// Configuração de Guard Global
router.beforeEach((to) => {
  const auth = useAuth()
  const isAuthenticated = auth.isAuthenticated()
  const userRole = auth.getUserRole()

  if (to.meta.requiresAuth && !isAuthenticated) {
    return { name: 'login'}
  }

  if (to.name === 'login' && isAuthenticated) {
    if (userRole === 'cliente') {
      return { name: 'cliente'}
    }

    if (userRole === 'gerente') {
      return { name: 'gerente'}
    }

    return { name: 'home'}
  }
})


export default router
