package br.com.servix.core.logging;

import br.com.servix.core.tenant.SecurityTenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        MDC.put("path", request.getRequestURI());
        try {
            filterChain.doFilter(request, response);
        } finally {
            Optional<UUID> tenantId = SecurityTenantContext.currentTenantId();
            Optional<UUID> userId = SecurityTenantContext.currentUserId();
            tenantId.ifPresent(id -> MDC.put("tenantId", id.toString()));
            userId.ifPresent(id -> MDC.put("userId", id.toString()));
            long elapsed = System.currentTimeMillis() - start;
            LOGGER.info("request completed method={} path={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsed);
            MDC.clear();
        }
    }
}
