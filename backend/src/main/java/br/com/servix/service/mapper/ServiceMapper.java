package br.com.servix.service.mapper;

import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.dto.ServiceRequest;
import br.com.servix.service.dto.ServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceOffering toEntity(ServiceRequest request) {
        ServiceOffering service = new ServiceOffering();
        apply(request, service);
        return service;
    }

    public void apply(ServiceRequest request, ServiceOffering service) {
        service.setName(request.name());
        service.setDescription(request.description());
        service.setDurationMinutes(request.durationMinutes());
        service.setPrice(request.price());
    }

    public ServiceResponse toResponse(ServiceOffering service) {
        return new ServiceResponse(
                service.getId(),
                service.getCompanyId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getPrice(),
                service.isActive(),
                service.getCreatedAt(),
                service.getUpdatedAt(),
                service.getCreatedBy(),
                service.getUpdatedBy());
    }
}
