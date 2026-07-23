<template>
  <div class="fields">
    <label>
      Agendamento (opcional)
      <input v-model="model.appointmentId" type="text" :readonly="appointmentLocked" placeholder="UUID do agendamento" />
    </label>
    <label>
      Cliente
      <select v-model="model.customerId" :disabled="appointmentLocked" required>
        <option value="">Selecione</option>
        <option v-for="customer in customers" :key="customer.id" :value="customer.id">
          {{ customer.nome }}
        </option>
      </select>
    </label>
    <label>
      Serviço
      <select v-model="model.serviceId" :disabled="appointmentLocked" required>
        <option value="">Selecione</option>
        <option v-for="service in services" :key="service.id" :value="service.id">
          {{ service.name }}
        </option>
      </select>
    </label>
    <label>
      Profissional
      <select v-model="model.professionalId" :disabled="appointmentLocked" required>
        <option value="">Selecione</option>
        <option v-for="employee in professionals" :key="employee.id" :value="employee.id">
          {{ employee.name }}
        </option>
      </select>
    </label>
    <label>
      Início previsto
      <input v-model="model.scheduledStart" :disabled="appointmentLocked" type="datetime-local" required />
    </label>
    <label class="full">
      Observações
      <textarea v-model="model.observations" rows="4" placeholder="Anotações operacionais da ordem de serviço" />
    </label>
  </div>
</template>

<script setup>
defineProps({
  customers: { type: Array, default: () => [] },
  services: { type: Array, default: () => [] },
  professionals: { type: Array, default: () => [] },
  appointmentLocked: { type: Boolean, default: false },
})

const model = defineModel({ type: Object, required: true })
</script>

<style scoped>
.fields {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.full {
  grid-column: 1 / -1;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

textarea,
input,
select {
  padding: 10px 12px;
}
</style>
