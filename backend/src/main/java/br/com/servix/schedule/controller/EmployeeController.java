package br.com.servix.schedule.controller;

import br.com.servix.core.api.ApiResponseFactory;
import br.com.servix.core.api.ApiSuccessResponse;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.schedule.dto.EmployeeRequest;
import br.com.servix.schedule.dto.EmployeeResponse;
import br.com.servix.schedule.dto.EmployeeSearchRequest;
import br.com.servix.schedule.dto.EmployeeStatusUpdateRequest;
import br.com.servix.schedule.service.EmployeeService;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'OPERADOR')")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<EmployeeResponse>> create(
            @Valid @RequestBody EmployeeRequest request,
            HttpServletRequest servletRequest) {
        EmployeeResponse response = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseFactory.success(HttpStatus.CREATED, "Funcionário criado com sucesso", servletRequest.getRequestURI(), response));
    }

    @GetMapping
    public ApiSuccessResponse<PagedResponse<EmployeeResponse>> search(
            @Valid @ModelAttribute EmployeeSearchRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Funcionários listados com sucesso", servletRequest.getRequestURI(), employeeService.search(request));
    }

    @GetMapping("/{id}")
    public ApiSuccessResponse<EmployeeResponse> getById(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Funcionário encontrado com sucesso", servletRequest.getRequestURI(), employeeService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiSuccessResponse<EmployeeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Funcionário atualizado com sucesso", servletRequest.getRequestURI(), employeeService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiSuccessResponse<EmployeeResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Status do funcionário atualizado com sucesso", servletRequest.getRequestURI(), employeeService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiSuccessResponse<Void> delete(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        employeeService.delete(id);
        return ApiResponseFactory.success(HttpStatus.OK, "Funcionário removido com sucesso", servletRequest.getRequestURI(), null);
    }
}
