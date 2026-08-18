<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const drawer = ref(true)
const router = useRouter()
const { logout } = useAuth()

const navItems = [
  { title: 'Início', icon: 'mdi-view-dashboard', to: { name: 'home' } },
]

function handleLogout() {
  logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <v-app-bar color="primary" density="comfortable">
    <v-app-bar-nav-icon @click="drawer = !drawer" />
    <v-toolbar-title>BANTADS</v-toolbar-title>
    <v-spacer />
    <v-btn icon="mdi-logout" @click="handleLogout" />
  </v-app-bar>

  <v-navigation-drawer v-model="drawer">
    <v-list nav>
      <v-list-item
        v-for="item in navItems"
        :key="item.title"
        :to="item.to"
        :prepend-icon="item.icon"
        :title="item.title"
      />
    </v-list>
  </v-navigation-drawer>

  <v-main>
    <v-container fluid>
      <router-view />
    </v-container>
  </v-main>
</template>
