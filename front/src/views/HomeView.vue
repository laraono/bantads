<script setup lang="ts">
import { formatCurrency } from '@/utils/currency'
import { formatDate, now } from '@/utils/date'

const balance = formatCurrency('1250.5')
const today = formatDate(now().toJSDate())
</script>

<template>
  <v-row>
    <v-col cols="12" md="4">
      <v-card>
        <v-card-title>Saldo</v-card-title>
        <v-card-text class="text-h5">{{ balance }}</v-card-text>
        <v-card-subtitle>Atualizado em {{ today }}</v-card-subtitle>
      </v-card>
    </v-col>
  </v-row>
</template>
