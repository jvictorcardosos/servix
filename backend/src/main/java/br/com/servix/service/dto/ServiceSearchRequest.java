package br.com.servix.service.dto;

import java.math.BigDecimal;

public record ServiceSearchRequest(
        Integer page,
        Integer size,
        String sortBy,
        String direction,
        String filter,
        String name,
        Boolean active,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minDuration,
        Integer maxDuration) {
}
