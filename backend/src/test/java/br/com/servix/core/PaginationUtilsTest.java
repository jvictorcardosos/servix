package br.com.servix.core;

import br.com.servix.core.pagination.PageRequestParams;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.core.pagination.PaginationUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationUtilsTest {

    @Test
    void shouldBuildPageableWithDefaults() {
        Pageable pageable = PaginationUtils.toPageable(new PageRequestParams(null, null, null, null, null));
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void shouldClampPageSizeToMax() {
        Pageable pageable = PaginationUtils.toPageable(new PageRequestParams(0, 1000, "nome", "DESC", "ativo"));
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().getOrderFor("nome").getDirection().name()).isEqualTo("DESC");
    }

    @Test
    void shouldMapPageToResponse() {
        Page<String> page = new PageImpl<>(List.of("a", "b"), PaginationUtils.toPageable(new PageRequestParams(1, 2, "nome", "ASC", "x")), 10);
        PagedResponse<String> response = PaginationUtils.fromPage(page, new PageRequestParams(1, 2, "nome", "ASC", "x"));

        assertThat(response.content()).containsExactly("a", "b");
        assertThat(response.totalElements()).isEqualTo(10);
        assertThat(response.sortBy()).isEqualTo("nome");
        assertThat(response.filter()).isEqualTo("x");
    }
}
