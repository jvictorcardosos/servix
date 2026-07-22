package br.com.servix.core.api;

import java.time.Instant;

public record ApiSuccessResponse<T>(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        T data) {
}
