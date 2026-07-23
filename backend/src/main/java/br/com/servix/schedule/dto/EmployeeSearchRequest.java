package br.com.servix.schedule.dto;

public record EmployeeSearchRequest(
        Integer page,
        Integer size,
        String sortBy,
        String direction,
        String filter,
        String name,
        String email,
        String phone,
        Boolean active) {
}
