package br.com.servix.serviceorder.dto;

import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import java.time.LocalDate;
import java.util.UUID;

public record ServiceOrderSearchRequest(
        Integer page,
        Integer size,
        String sortBy,
        String direction,
        String filter,
        UUID appointmentId,
        UUID customerId,
        UUID professionalId,
        UUID serviceId,
        ServiceOrderStatus status,
        LocalDate dateFrom,
        LocalDate dateTo) {
}
