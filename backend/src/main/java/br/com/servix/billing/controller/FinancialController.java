package br.com.servix.billing.controller;

import br.com.servix.billing.domain.FinancialStatus;
import br.com.servix.billing.dto.FinancialAdjustmentRequest;
import br.com.servix.billing.dto.FinancialPaymentRequest;
import br.com.servix.billing.dto.FinancialTransactionRequest;
import br.com.servix.billing.dto.FinancialTransactionResponse;
import br.com.servix.billing.dto.FinancialTransactionSearchRequest;
import br.com.servix.billing.service.FinancialService;
import br.com.servix.core.api.ApiResponseFactory;
import br.com.servix.core.api.ApiSuccessResponse;
import br.com.servix.core.pagination.PagedResponse;
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
@RequestMapping("/api/financial")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'OPERADOR')")
public class FinancialController {

    private final FinancialService financialService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<FinancialTransactionResponse>> create(
            @Valid @RequestBody FinancialTransactionRequest request,
            HttpServletRequest servletRequest) {
        FinancialTransactionResponse response = financialService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseFactory.success(HttpStatus.CREATED, "Lançamento financeiro criado com sucesso", servletRequest.getRequestURI(), response));
    }

    @GetMapping
    public ApiSuccessResponse<PagedResponse<FinancialTransactionResponse>> search(
            @Valid @ModelAttribute FinancialTransactionSearchRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Lançamentos financeiros listados com sucesso", servletRequest.getRequestURI(), financialService.search(request));
    }

    @GetMapping("/{id}")
    public ApiSuccessResponse<FinancialTransactionResponse> getById(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Lançamento financeiro encontrado com sucesso", servletRequest.getRequestURI(), financialService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiSuccessResponse<FinancialTransactionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody FinancialTransactionRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Lançamento financeiro atualizado com sucesso", servletRequest.getRequestURI(), financialService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiSuccessResponse<Void> delete(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        financialService.delete(id);
        return ApiResponseFactory.success(HttpStatus.OK, "Lançamento financeiro removido com sucesso", servletRequest.getRequestURI(), null);
    }

    @PatchMapping("/{id}/pay")
    public ApiSuccessResponse<FinancialTransactionResponse> pay(
            @PathVariable UUID id,
            @Valid @RequestBody FinancialPaymentRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Pagamento registrado com sucesso", servletRequest.getRequestURI(), financialService.pay(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public ApiSuccessResponse<FinancialTransactionResponse> cancel(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Lançamento financeiro cancelado com sucesso", servletRequest.getRequestURI(), financialService.cancel(id));
    }

    @PatchMapping("/{id}/discount")
    public ApiSuccessResponse<FinancialTransactionResponse> discount(
            @PathVariable UUID id,
            @Valid @RequestBody FinancialAdjustmentRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Desconto aplicado com sucesso", servletRequest.getRequestURI(), financialService.discount(id, request));
    }

    @PatchMapping("/{id}/surcharge")
    public ApiSuccessResponse<FinancialTransactionResponse> surcharge(
            @PathVariable UUID id,
            @Valid @RequestBody FinancialAdjustmentRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Acréscimo aplicado com sucesso", servletRequest.getRequestURI(), financialService.surcharge(id, request));
    }

    @GetMapping("/service-order/{id}")
    public ApiSuccessResponse<List<FinancialTransactionResponse>> byServiceOrder(
            @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Lançamentos da ordem de serviço listados com sucesso", servletRequest.getRequestURI(), financialService.listByServiceOrder(id));
    }

    @GetMapping("/status/{status}")
    public ApiSuccessResponse<PagedResponse<FinancialTransactionResponse>> byStatus(
            @PathVariable FinancialStatus status,
            @Valid @ModelAttribute FinancialTransactionSearchRequest request,
            HttpServletRequest servletRequest) {
        FinancialTransactionSearchRequest filtered = new FinancialTransactionSearchRequest(
                request.page(),
                request.size(),
                request.sortBy(),
                request.direction(),
                request.filter(),
                request.serviceOrderId(),
                request.customerId(),
                request.professionalId(),
                request.serviceId(),
                request.paymentMethodId(),
                request.transactionType(),
                status,
                request.dateFrom(),
                request.dateTo());
        return ApiResponseFactory.success(HttpStatus.OK, "Lançamentos por status listados com sucesso", servletRequest.getRequestURI(), financialService.search(filtered));
    }

    @GetMapping("/due")
    public ApiSuccessResponse<List<FinancialTransactionResponse>> due(HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Lançamentos a vencer listados com sucesso", servletRequest.getRequestURI(), financialService.listDue());
    }

    @GetMapping("/overdue")
    public ApiSuccessResponse<List<FinancialTransactionResponse>> overdue(HttpServletRequest servletRequest) {
        return ApiResponseFactory.success(HttpStatus.OK, "Lançamentos vencidos listados com sucesso", servletRequest.getRequestURI(), financialService.listOverdue());
    }
}
