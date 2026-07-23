<template>
  <div class="fields">
    <label>
      Cliente
      <select v-model="model.customerId" required>
        <option value="">Selecione</option>
        <option v-for="customer in customers" :key="customer.id" :value="customer.id">
          {{ customer.nome }}
        </option>
      </select>
    </label>
    <label>
      Serviço
      <select v-model="model.serviceId" required>
        <option value="">Selecione</option>
        <option v-for="service in services" :key="service.id" :value="service.id">
          {{ service.name }}
        </option>
      </select>
    </label>
    <label>
      Funcionário
      <select v-model="model.employeeId" required>
        <option value="">Selecione</option>
        <option v-for="employee in employees" :key="employee.id" :value="employee.id">
          {{ employee.name }}
        </option>
      </select>
    </label>
    <label>
      Data
      <input v-model="model.appointmentDate" type="date" required />
    </label>
    <label>
      Horário inicial
      <input v-model="model.startTime" type="time" required />
    </label>
    <label class="full">
      Observações
      <textarea v-model="model.notes" rows="4" />
    </label>
  </div>
</template>

<script setup>
defineProps({
  customers: { type: Array, default: () => [] },
  services: { type: Array, default: () => [] },
  employees: { type: Array, default: () => [] },
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
