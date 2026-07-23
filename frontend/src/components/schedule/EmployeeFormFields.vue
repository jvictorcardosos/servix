<template>
  <div class="fields">
    <label>
      Nome
      <input v-model="model.name" type="text" required />
    </label>
    <label>
      Email
      <input v-model="model.email" type="email" required />
    </label>
    <label>
      Telefone
      <input v-model="model.phone" type="text" />
    </label>
    <label class="checkbox">
      <input v-model="model.active" type="checkbox" />
      Ativo
    </label>

    <div class="schedule-section">
      <div class="schedule-header">
        <h3>Jornadas de trabalho</h3>
        <button type="button" @click="addSchedule">Adicionar jornada</button>
      </div>

      <div v-for="(schedule, index) in model.workSchedules" :key="index" class="schedule-row">
        <label>
          Dia da semana
          <select v-model.number="schedule.dayOfWeek">
            <option :value="1">Segunda</option>
            <option :value="2">Terça</option>
            <option :value="3">Quarta</option>
            <option :value="4">Quinta</option>
            <option :value="5">Sexta</option>
            <option :value="6">Sábado</option>
            <option :value="7">Domingo</option>
          </select>
        </label>
        <label>
          Início
          <input v-model="schedule.startTime" type="time" required />
        </label>
        <label>
          Fim
          <input v-model="schedule.endTime" type="time" required />
        </label>
        <label class="checkbox">
          <input v-model="schedule.active" type="checkbox" />
          Ativa
        </label>
        <button type="button" class="danger" @click="removeSchedule(index)" :disabled="model.workSchedules.length === 1">
          Remover
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
const model = defineModel({ type: Object, required: true })

function addSchedule() {
  model.value.workSchedules.push({
    dayOfWeek: 1,
    startTime: '08:00',
    endTime: '17:00',
    active: true,
  })
}

function removeSchedule(index) {
  if (model.value.workSchedules.length === 1) {
    return
  }
  model.value.workSchedules.splice(index, 1)
}
</script>

<style scoped>
.fields {
  display: grid;
  gap: 12px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.checkbox {
  flex-direction: row;
  align-items: center;
}

.schedule-section {
  margin-top: 12px;
  display: grid;
  gap: 12px;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.schedule-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  align-items: end;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

input,
select,
button {
  padding: 10px 12px;
}

.danger {
  background: #dc2626;
  color: #fff;
  border: none;
}
</style>
