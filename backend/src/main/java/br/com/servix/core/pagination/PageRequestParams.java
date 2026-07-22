package br.com.servix.core.pagination;

public record PageRequestParams(
        Integer page,
        Integer size,
        String sortBy,
        String direction,
        String filter) {
}
