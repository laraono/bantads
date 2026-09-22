<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

interface SidebarUser {
  fullName: string
  email: string
  initials: string
}

type ClientNavKey = 'home' | 'extrato' | 'config'
type ManagerNavKey = 'aprovacoes' | 'clientes' | 'relatorio' | 'gerentes'
type NavKey = ClientNavKey | ManagerNavKey
type NavIcon = 'home' | 'chart' | 'gear' | 'clock' | 'users' | 'report' | 'shield-user'

const props = withDefaults(
  defineProps<{
    active?: NavKey
    role?: 'cliente' | 'gerente'
    user?: SidebarUser
  }>(),
  {
    active: 'home',
    role: 'cliente',
    user: () => ({ fullName: 'John Doe', email: 'john@example.com', initials: 'AD' })
  }
)

const emit = defineEmits<{
  logout: []
}>()

const router = useRouter()
const { logout } = useAuth()

const clientNavItems: { key: NavKey; label: string; to: string; icon: NavIcon }[] = [
  { key: 'home', label: 'Home', to: '/home', icon: 'home' },
  { key: 'extrato', label: 'Extrato detalhado', to: '/extrato', icon: 'chart' },
  { key: 'config', label: 'Configurações', to: '', icon: 'gear' }
]

const managerNavItems: { key: NavKey; label: string; to: string; icon: NavIcon }[] = [
  { key: 'aprovacoes', label: 'Aprovações pendentes', to: '/gerentes', icon: 'clock' },
  { key: 'clientes', label: 'Clientes', to: '/clientes', icon: 'users' },
  { key: 'relatorio', label: 'Relatório de Cliente', to: '', icon: 'report' },
  { key: 'gerentes', label: 'Gerentes', to: '/listagem', icon: 'shield-user' }
]

const navItems = computed(() => (props.role === 'gerente' ? managerNavItems : clientNavItems))

function go(to: string) {
  if (to) router.push(to)
}

function handleLogout() {
  logout()
  emit('logout')
  router.push('/login')
}
</script>

<template>
  <aside class="sidebar">
    <div class="brand">
      <span class="brand-name">BandTads</span>
    </div>

    <nav class="nav">
      <a
        v-for="item in navItems"
        :key="item.key"
        class="nav-item"
        :class="{ active: item.key === props.active }"
        href="#"
        @click.prevent="go(item.to)"
      >
        <svg v-if="item.icon === 'home'" viewBox="0 0 24 24" class="nav-icon"><path d="M3 11.5 12 4l9 7.5" /><path d="M5 10v9a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1v-9" /></svg>
        <svg v-else-if="item.icon === 'chart'" viewBox="0 0 24 24" class="nav-icon"><path d="m3 17 5-5 4 4 8-8" /><path d="M15 8h5v5" /></svg>
        <svg v-else-if="item.icon === 'clock'" viewBox="0 0 24 24" class="nav-icon"><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 3" /></svg>
        <svg v-else-if="item.icon === 'users'" viewBox="0 0 24 24" class="nav-icon"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></svg>
        <svg v-else-if="item.icon === 'report'" viewBox="0 0 24 24" class="nav-icon"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z" /><path d="M14 2v6h6" /><path d="M9 15v2" /><path d="M12 12v5" /><path d="M15 9v8" /></svg>
        <svg v-else-if="item.icon === 'shield-user'" viewBox="0 0 24 24" class="nav-icon"><path d="M12 2 4 5v6c0 5 3.4 8.9 8 10 4.6-1.1 8-5 8-10V5l-8-3Z" /><circle cx="12" cy="10" r="2.2" /><path d="M8.5 16a3.5 3.5 0 0 1 7 0" /></svg>
        <svg v-else viewBox="0 0 24 24" class="nav-icon"><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.87l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.7 1.7 0 0 0-1.87-.34 1.7 1.7 0 0 0-1.04 1.56V21a2 2 0 1 1-4 0v-.09A1.7 1.7 0 0 0 9 19.4a1.7 1.7 0 0 0-1.87.34l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.7 1.7 0 0 0 4.6 15a1.7 1.7 0 0 0-1.56-1.04H3a2 2 0 1 1 0-4h.09A1.7 1.7 0 0 0 4.6 9a1.7 1.7 0 0 0-.34-1.87l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.7 1.7 0 0 0 9 4.6a1.7 1.7 0 0 0 1.04-1.56V3a2 2 0 1 1 4 0v.09A1.7 1.7 0 0 0 15 4.6a1.7 1.7 0 0 0 1.87-.34l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.7 1.7 0 0 0 19.4 9a1.7 1.7 0 0 0 1.56 1.04H21a2 2 0 1 1 0 4h-.09A1.7 1.7 0 0 0 19.4 15Z" /></svg>
        <span>{{ item.label }}</span>
      </a>
    </nav>

    <div class="user-card">
      <div class="avatar">{{ props.user.initials }}</div>
      <div class="user-info">
        <span class="user-name">{{ props.user.fullName }}</span>
        <span class="user-email">{{ props.user.email }}</span>
      </div>
      <svg viewBox="0 0 24 24" class="logout-icon" role="button" aria-label="Sair" @click="handleLogout">
        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><path d="M16 17l5-5-5-5" /><path d="M21 12H9" />
      </svg>
    </div>
  </aside>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.sidebar {
  width: 240px;
  background: #0d1b2a;
  display: flex;
  flex-direction: column;
  padding: 24px 16px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.brand {
  padding: 0 8px 24px;
}

.brand-name {
  color: #f0c968;
  font-weight: 700;
  font-size: 20px;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  color: #cbd5e1;
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s;
}

.nav-item:hover {
  background: #16243a;
}

.nav-item.active {
  background: #f0c968;
  color: #1a1a1a;
  font-weight: 600;
}

.nav-icon {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  flex-shrink: 0;
}

.user-card {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-top: 1px solid #1e2d42;
  padding-top: 20px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0c968;
  color: #1a1a1a;
  font-weight: 700;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.user-name {
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-email {
  color: #94a3b8;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logout-icon {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: #94a3b8;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  flex-shrink: 0;
  cursor: pointer;
}

.logout-icon:hover {
  stroke: #f0c968;
}
</style>
