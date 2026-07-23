<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ serviceOrder?.customerName ?? 'Histórico da ordem' }}</h1>
        <p>Linha do tempo operacional da execução.</p>
      </div>
      <RouterLink class="secondary" :to="`/service-orders/${route.params.id}`">Voltar</RouterLink>
    </header>

    <p v-if="serviceOrderStore.loading">Carregando histórico...</p>
    <p v-else-if="serviceOrderStore.error" class="error">{{ serviceOrderStore.error }}</p>
    <p v-else-if="serviceOrderStore.message" class="success">{{ serviceOrderStore.message }}</p>

    <div v-else class="summary">
      <div><strong>Status atual:</strong> {{ formatStatus(serviceOrder?.status) }}</div>
      <div><strong>Cliente:</strong> {{ serviceOrder?.customerName }}</div>
      <div><strong>Serviço:</strong> {{ serviceOrder?.serviceName }}</div>
    </div>

    <ServiceOrderTimeline :history="serviceOrderStore.history" />
  </section>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ServiceOrderTimeline from '../../components/serviceorder/ServiceOrderTimeline.vue'
import { useServiceOrderStore } from '../../stores/serviceOrderStore'

const route = useRoute()
const serviceOrderStore = useServiceOrderStore()

const serviceOrder = computed(() => serviceOrderStore.currentServiceOrder)

onMounted(async () => {
  await serviceOrderStore.loadServiceOrder(route.params.id)
  await serviceOrderStore.loadServiceOrderHistory(route.params.id)
})

function formatStatus(status) {
  const labels = {
    OPEN: 'Aberta',
    CONFIRMED: 'Confirmada',
    IN_PROGRESS: 'Em andamento',
    PAUSED: 'Pausada',
    COMPLETED: 'Concluída',
    CANCELLED: 'Cancelada',
    NO_SHOW: 'Não compareceu',
  }
  return labels[status] || status
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.secondary {
  padding: 10px 16px;
  background: #e5e7eb;
  color: #111827;
  text-decoration: none;
  border-radius: 10px;
}

.summary {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  color: #374151;
}

.error {
  color: #b91c1c;
}

.success {
  color: #166534;
}
</style>
