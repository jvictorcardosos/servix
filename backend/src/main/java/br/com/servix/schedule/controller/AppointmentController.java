package br.com.servix.schedule.controller;

import br.com.servix.core.api.ApiResponseFactory;
import br.com.servix.core.api.ApiSuccessResponse;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.schedule.domain.AppointmentStatus;
import br.com.servix.schedule.dto.AppointmentRequest;
import br.com.servix.schedule.dto.AppointmentResponse;
import br.com.servix.schedule.dto.AppointmentSearchRequest;
import br.com.servix.schedule.dto.AppointmentStatusUpdateRequest;
import br.com.servix.schedule.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'OPERADOR')")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<AppointmentResponse>> create(
            @Valid @RequestBody AppointmentRequest request,
            HttpServletRequest servletRequest) {
        AppointmentResponse response = appointmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseFactory.success(HttpStatus.CREATED, "Agendamento criado com sucesso", servletRequest.getRequestURI(), response));
    }

    @GetMapping
    public ApiSuccessResponse<PagedResponse<AppointmentResponse>> search(
            @Valid @ModelAttribute AppointmentSearchRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Agendamentos listados com sucesso", servletRequest.getRequestURI(), appointmentService.search(request));
    }

    @GetMapping("/{id}")
    public ApiSuccessResponse<AppointmentResponse> getById(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Agendamento encontrado com sucesso", servletRequest.getRequestURI(), appointmentService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiSuccessResponse<AppointmentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Agendamento atualizado com sucesso", servletRequest.getRequestURI(), appointmentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiSuccessResponse<AppointmentResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Status do agendamento atualizado com sucesso", servletRequest.getRequestURI(), appointmentService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiSuccessResponse<Void> delete(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        appointmentService.delete(id);
        return ApiResponseFactory.success(HttpStatus.OK, "Agendamento removido com sucesso", servletRequest.getRequestURI(), null);
    }

    @GetMapping("/day")
    public ApiSuccessResponse<List<AppointmentResponse>> day(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) AppointmentStatus status,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Agenda diária carregada com sucesso", servletRequest.getRequestURI(), appointmentService.listDay(date, employeeId, customerId, serviceId, status));
    }

    @GetMapping("/week")
    public ApiSuccessResponse<List<AppointmentResponse>> week(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) AppointmentStatus status,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Agenda semanal carregada com sucesso", servletRequest.getRequestURI(), appointmentService.listWeek(date, employeeId, customerId, serviceId, status));
    }

    @GetMapping("/month")
    public ApiSuccessResponse<List<AppointmentResponse>> month(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) AppointmentStatus status,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Agenda mensal carregada com sucesso", servletRequest.getRequestURI(), appointmentService.listMonth(date, employeeId, customerId, serviceId, status));
    }

    @GetMapping("/employee/{id}")
    public ApiSuccessResponse<List<AppointmentResponse>> employeeAgenda(
            @PathVariable UUID id,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Agenda do funcionário carregada com sucesso", servletRequest.getRequestURI(), appointmentService.listByEmployee(id, from, to));
    }

    @GetMapping("/customer/{id}")
    public ApiSuccessResponse<List<AppointmentResponse>> customerAgenda(
            @PathVariable UUID id,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Agenda do cliente carregada com sucesso", servletRequest.getRequestURI(), appointmentService.listByCustomer(id, from, to));
    }
}
