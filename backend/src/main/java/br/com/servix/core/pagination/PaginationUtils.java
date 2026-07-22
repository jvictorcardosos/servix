package br.com.servix.core.pagination;

import br.com.servix.core.config.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static Pageable toPageable(PageRequestParams params) {
        int page = params.page() == null || params.page() < 0 ? CoreConstants.DEFAULT_PAGE : params.page();
        int size = params.size() == null || params.size() <= 0 ? CoreConstants.DEFAULT_PAGE_SIZE : Math.min(params.size(), CoreConstants.MAX_PAGE_SIZE);
        String sortBy = params.sortBy() == null || params.sortBy().isBlank() ? "createdAt" : params.sortBy();
        Sort.Direction direction = "DESC".equalsIgnoreCase(params.direction()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    public static <T> PagedResponse<T> fromPage(Page<T> page, PageRequestParams params) {
        String sortBy = params.sortBy() == null || params.sortBy().isBlank() ? "createdAt" : params.sortBy();
        String direction = "DESC".equalsIgnoreCase(params.direction()) ? "DESC" : "ASC";
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                sortBy,
                direction,
                params.filter());
    }
}
