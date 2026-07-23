package br.com.servix.service.controller;

import br.com.servix.core.api.ApiResponseFactory;
import br.com.servix.core.api.ApiSuccessResponse;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.service.dto.ServiceRequest;
import br.com.servix.service.dto.ServiceResponse;
import br.com.servix.service.dto.ServiceSearchRequest;
import br.com.servix.service.dto.ServiceStatusUpdateRequest;
import br.com.servix.service.service.ServiceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/services")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'OPERADOR')")
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<ServiceResponse>> create(
            @Valid @RequestBody ServiceRequest request,
            HttpServletRequest servletRequest) {
        ServiceResponse response = serviceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseFactory.success(HttpStatus.CREATED, "Serviço criado com sucesso", servletRequest.getRequestURI(), response));
    }

    @GetMapping
    public ApiSuccessResponse<PagedResponse<ServiceResponse>> search(
            @Valid @ModelAttribute ServiceSearchRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Serviços listados com sucesso", servletRequest.getRequestURI(), serviceService.search(request));
    }

    @GetMapping("/{id}")
    public ApiSuccessResponse<ServiceResponse> getById(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Serviço encontrado com sucesso", servletRequest.getRequestURI(), serviceService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiSuccessResponse<ServiceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Serviço atualizado com sucesso", servletRequest.getRequestURI(), serviceService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiSuccessResponse<ServiceResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Status do serviço atualizado com sucesso", servletRequest.getRequestURI(), serviceService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiSuccessResponse<Void> delete(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        serviceService.delete(id);
        return ApiResponseFactory.success(HttpStatus.OK, "Serviço removido com sucesso", servletRequest.getRequestURI(), null);
    }
}
