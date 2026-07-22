package br.com.servix.core.api;

import br.com.servix.core.config.CoreConstants;
import java.time.Instant;
import org.springframework.http.HttpStatus;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static <T> ApiSuccessResponse<T> success(HttpStatus status, String message, String path, T data) {
        return new ApiSuccessResponse<>(
                Instant.now(),
                status.value(),
                CoreConstants.API_SUCCESS,
                message,
                path,
                data);
    }
}
