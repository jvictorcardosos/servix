package br.com.servix.customer.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 150, message = ValidationMessages.MAX_LENGTH)
        String nome,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 32, message = ValidationMessages.MAX_LENGTH)
        String cpfCnpj,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Email(message = ValidationMessages.INVALID_EMAIL)
        @Size(max = 150, message = ValidationMessages.MAX_LENGTH)
        String email,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 20, message = ValidationMessages.MAX_LENGTH)
        String telefone,

        @Size(max = 20, message = ValidationMessages.MAX_LENGTH)
        String telefoneSecundario,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 8, message = ValidationMessages.MAX_LENGTH)
        String cep,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 180, message = ValidationMessages.MAX_LENGTH)
        String logradouro,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 20, message = ValidationMessages.MAX_LENGTH)
        String numero,

        @Size(max = 100, message = ValidationMessages.MAX_LENGTH)
        String complemento,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 100, message = ValidationMessages.MAX_LENGTH)
        String bairro,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 100, message = ValidationMessages.MAX_LENGTH)
        String cidade,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(min = 2, max = 2, message = ValidationMessages.MAX_LENGTH)
        String estado,

        @Size(max = 4000, message = ValidationMessages.MAX_LENGTH)
        String observacoes) {
}
