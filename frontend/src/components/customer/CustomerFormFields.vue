<template>
  <div class="fields">
    <label>
      Nome
      <input v-model="model.nome" type="text" required />
    </label>
    <label>
      CPF/CNPJ
      <input v-model="model.cpfCnpj" type="text" required />
    </label>
    <label>
      Email
      <input v-model="model.email" type="email" required />
    </label>
    <label>
      Telefone
      <input v-model="model.telefone" type="text" required />
    </label>
    <label>
      Telefone secundário
      <input v-model="model.telefoneSecundario" type="text" />
    </label>
    <label>
      CEP
      <input v-model="model.cep" type="text" required />
    </label>
    <label class="full">
      Logradouro
      <input v-model="model.logradouro" type="text" required />
    </label>
    <label>
      Número
      <input v-model="model.numero" type="text" required />
    </label>
    <label>
      Complemento
      <input v-model="model.complemento" type="text" />
    </label>
    <label>
      Bairro
      <input v-model="model.bairro" type="text" required />
    </label>
    <label>
      Cidade
      <input v-model="model.cidade" type="text" required />
    </label>
    <label>
      Estado
      <input v-model="model.estado" type="text" maxlength="2" required />
    </label>
    <label class="full">
      Observações
      <textarea v-model="model.observacoes" rows="4" />
    </label>
    <label class="checkbox">
      <input v-model="model.ativo" type="checkbox" />
      Ativo
    </label>
  </div>
</template>

<script setup>
const model = defineModel({ type: Object, required: true })
</script>

<style scoped>
.fields {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.full {
  grid-column: 1 / -1;
}

.checkbox {
  flex-direction: row;
  align-items: center;
}

input,
textarea {
  padding: 10px 12px;
}
</style>
