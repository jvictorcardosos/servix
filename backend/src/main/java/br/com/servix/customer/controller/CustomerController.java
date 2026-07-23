package br.com.servix.customer.controller;

import br.com.servix.core.api.ApiResponseFactory;
import br.com.servix.core.api.ApiSuccessResponse;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.customer.dto.CustomerRequest;
import br.com.servix.customer.dto.CustomerResponse;
import br.com.servix.customer.dto.CustomerSearchRequest;
import br.com.servix.customer.dto.CustomerStatusUpdateRequest;
import br.com.servix.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'OPERADOR')")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<CustomerResponse>> create(
            @Valid @RequestBody CustomerRequest request,
            HttpServletRequest servletRequest) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseFactory.success(HttpStatus.CREATED, "Cliente criado com sucesso", servletRequest.getRequestURI(), response));
    }

    @GetMapping
    public ApiSuccessResponse<PagedResponse<CustomerResponse>> search(
            @Valid @ModelAttribute CustomerSearchRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Clientes listados com sucesso", servletRequest.getRequestURI(), customerService.search(request));
    }

    @GetMapping("/{id}")
    public ApiSuccessResponse<CustomerResponse> getById(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Cliente encontrado com sucesso", servletRequest.getRequestURI(), customerService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiSuccessResponse<CustomerResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Cliente atualizado com sucesso", servletRequest.getRequestURI(), customerService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiSuccessResponse<CustomerResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Status do cliente atualizado com sucesso", servletRequest.getRequestURI(), customerService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiSuccessResponse<Void> delete(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        customerService.delete(id);
        return ApiResponseFactory.success(HttpStatus.OK, "Cliente removido com sucesso", servletRequest.getRequestURI(), null);
    }
}
