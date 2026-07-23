package br.com.servix.billing.controller;

import br.com.servix.billing.dto.PaymentMethodResponse;
import br.com.servix.billing.service.PaymentMethodService;
import br.com.servix.core.api.ApiResponseFactory;
import br.com.servix.core.api.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public ApiSuccessResponse<List<PaymentMethodResponse>> list(HttpServletRequest request) {
        return ApiResponseFactory.success(HttpStatus.OK, "Formas de pagamento listadas com sucesso", request.getRequestURI(), paymentMethodService.list());
    }
}
