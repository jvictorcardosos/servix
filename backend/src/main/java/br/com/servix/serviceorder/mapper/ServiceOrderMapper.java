package br.com.servix.serviceorder.mapper;

import br.com.servix.serviceorder.domain.ServiceOrder;
import br.com.servix.serviceorder.domain.ServiceOrderHistory;
import br.com.servix.serviceorder.dto.ServiceOrderHistoryResponse;
import br.com.servix.serviceorder.dto.ServiceOrderRequest;
import br.com.servix.serviceorder.dto.ServiceOrderResponse;
import org.springframework.stereotype.Component;

@Component
public class ServiceOrderMapper {

    public ServiceOrder toEntity(ServiceOrderRequest request) {
        ServiceOrder serviceOrder = new ServiceOrder();
        apply(request, serviceOrder);
        return serviceOrder;
    }

    public void apply(ServiceOrderRequest request, ServiceOrder serviceOrder) {
        serviceOrder.setAppointmentId(request.appointmentId());
        serviceOrder.setCustomerId(request.customerId());
        serviceOrder.setProfessionalId(request.professionalId());
        serviceOrder.setServiceId(request.serviceId());
        serviceOrder.setScheduledStart(request.scheduledStart());
        serviceOrder.setObservations(request.observations());
    }

    public ServiceOrderResponse toResponse(
            ServiceOrder serviceOrder,
            String customerName,
            String professionalName,
            String serviceName) {
        return new ServiceOrderResponse(
                serviceOrder.getId(),
                serviceOrder.getCompanyId(),
                serviceOrder.getAppointmentId(),
                serviceOrder.getCustomerId(),
                customerName,
                serviceOrder.getProfessionalId(),
                professionalName,
                serviceOrder.getServiceId(),
                serviceName,
                serviceOrder.getServicePrice(),
                serviceOrder.getEstimatedDuration(),
                serviceOrder.getActualDuration(),
                serviceOrder.getScheduledStart(),
                serviceOrder.getScheduledEnd(),
                serviceOrder.getStartedAt(),
                serviceOrder.getFinishedAt(),
                serviceOrder.getStatus(),
                serviceOrder.getObservations(),
                serviceOrder.getCreatedAt(),
                serviceOrder.getUpdatedAt(),
                serviceOrder.getCreatedBy(),
                serviceOrder.getUpdatedBy());
    }

    public ServiceOrderHistoryResponse toHistoryResponse(ServiceOrderHistory history) {
        return new ServiceOrderHistoryResponse(
                history.getId(),
                history.getServiceOrderId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedBy(),
                history.getChangedAt(),
                history.getObservation());
    }
}
