package br.com.servix.service;

import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.dto.ServiceRequest;
import br.com.servix.service.mapper.ServiceMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceMapperTest {

    private final ServiceMapper mapper = new ServiceMapper();

    @Test
    void shouldMapRequestToEntity() {
        ServiceRequest request = new ServiceRequest("Limpeza", "Limpeza geral", 60, new BigDecimal("120.00"));

        ServiceOffering service = mapper.toEntity(request);

        assertThat(service.getName()).isEqualTo("Limpeza");
        assertThat(service.getDurationMinutes()).isEqualTo(60);
        assertThat(service.getPrice()).isEqualByComparingTo("120.00");
    }
}
