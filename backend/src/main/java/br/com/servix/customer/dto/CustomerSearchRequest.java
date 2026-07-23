package br.com.servix.customer.dto;

public record CustomerSearchRequest(
        Integer page,
        Integer size,
        String sortBy,
        String direction,
        String filter,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        Boolean ativo) {

    public String effectiveFilter() {
        return filter;
    }
}
