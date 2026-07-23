package br.com.servix.customer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID companyId,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,
        String telefoneSecundario,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String observacoes,
        boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID createdBy,
        UUID updatedBy) {
}
