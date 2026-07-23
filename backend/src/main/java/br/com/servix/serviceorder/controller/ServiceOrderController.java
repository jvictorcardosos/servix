package br.com.servix.serviceorder.controller;

import br.com.servix.core.api.ApiResponseFactory;
import br.com.servix.core.api.ApiSuccessResponse;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import br.com.servix.serviceorder.dto.ServiceOrderHistoryResponse;
import br.com.servix.serviceorder.dto.ServiceOrderRequest;
import br.com.servix.serviceorder.dto.ServiceOrderResponse;
import br.com.servix.serviceorder.dto.ServiceOrderSearchRequest;
import br.com.servix.serviceorder.service.ServiceOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'OPERADOR')")
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<ServiceOrderResponse>> create(
            @Valid @RequestBody ServiceOrderRequest request,
            HttpServletRequest servletRequest) {
        ServiceOrderResponse response = serviceOrderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseFactory.success(HttpStatus.CREATED, "Ordem de serviço criada com sucesso", servletRequest.getRequestURI(), response));
    }

    @GetMapping
    public ApiSuccessResponse<PagedResponse<ServiceOrderResponse>> search(
            @Valid @ModelAttribute ServiceOrderSearchRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Ordens de serviço listadas com sucesso", servletRequest.getRequestURI(), serviceOrderService.search(request));
    }

    @GetMapping("/{id}")
    public ApiSuccessResponse<ServiceOrderResponse> getById(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Ordem de serviço encontrada com sucesso", servletRequest.getRequestURI(), serviceOrderService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiSuccessResponse<ServiceOrderResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceOrderRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Ordem de serviço atualizada com sucesso", servletRequest.getRequestURI(), serviceOrderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiSuccessResponse<Void> delete(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        serviceOrderService.delete(id);
        return ApiResponseFactory.success(HttpStatus.OK, "Ordem de serviço removida com sucesso", servletRequest.getRequestURI(), null);
    }

    @PatchMapping("/{id}/start")
    public ApiSuccessResponse<ServiceOrderResponse> start(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Atendimento iniciado com sucesso", servletRequest.getRequestURI(), serviceOrderService.start(id));
    }

    @PatchMapping("/{id}/pause")
    public ApiSuccessResponse<ServiceOrderResponse> pause(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Atendimento pausado com sucesso", servletRequest.getRequestURI(), serviceOrderService.pause(id));
    }

    @PatchMapping("/{id}/resume")
    public ApiSuccessResponse<ServiceOrderResponse> resume(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Atendimento retomado com sucesso", servletRequest.getRequestURI(), serviceOrderService.resume(id));
    }

    @PatchMapping("/{id}/finish")
    public ApiSuccessResponse<ServiceOrderResponse> finish(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Atendimento concluído com sucesso", servletRequest.getRequestURI(), serviceOrderService.finish(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiSuccessResponse<ServiceOrderResponse> cancel(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Ordem de serviço cancelada com sucesso", servletRequest.getRequestURI(), serviceOrderService.cancel(id));
    }

    @GetMapping("/customer/{id}")
    public ApiSuccessResponse<PagedResponse<ServiceOrderResponse>> byCustomer(
            @PathVariable UUID id,
            @Valid @ModelAttribute ServiceOrderSearchRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Ordens de serviço do cliente listadas com sucesso", servletRequest.getRequestURI(), serviceOrderService.searchByCustomer(id, request));
    }

    @GetMapping("/professional/{id}")
    public ApiSuccessResponse<PagedResponse<ServiceOrderResponse>> byProfessional(
            @PathVariable UUID id,
            @Valid @ModelAttribute ServiceOrderSearchRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Ordens de serviço do profissional listadas com sucesso", servletRequest.getRequestURI(), serviceOrderService.searchByProfessional(id, request));
    }

    @GetMapping("/status/{status}")
    public ApiSuccessResponse<PagedResponse<ServiceOrderResponse>> byStatus(
            @PathVariable ServiceOrderStatus status,
            @Valid @ModelAttribute ServiceOrderSearchRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Ordens de serviço por status listadas com sucesso", servletRequest.getRequestURI(), serviceOrderService.searchByStatus(status, request));
    }

    @GetMapping("/history/{id}")
    public ApiSuccessResponse<List<ServiceOrderHistoryResponse>> history(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Histórico da ordem de serviço carregado com sucesso", servletRequest.getRequestURI(), serviceOrderService.history(id));
    }
}
