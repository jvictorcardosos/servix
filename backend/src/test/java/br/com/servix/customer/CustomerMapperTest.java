package br.com.servix.customer;

import br.com.servix.customer.domain.Customer;
import br.com.servix.customer.dto.CustomerRequest;
import br.com.servix.customer.mapper.CustomerMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private final CustomerMapper mapper = new CustomerMapper();

    @Test
    void shouldMapRequestToEntity() {
        CustomerRequest request = new CustomerRequest(
                "Cliente",
                "123",
                "cliente@servix.com",
                "11999998888",
                null,
                "12345678",
                "Rua A",
                "10",
                null,
                "Centro",
                "São Paulo",
                "SP",
                null);

        Customer customer = mapper.toEntity(request);

        assertThat(customer.getNome()).isEqualTo("Cliente");
        assertThat(customer.getCpfCnpj()).isEqualTo("123");
        assertThat(customer.getEmail()).isEqualTo("cliente@servix.com");
    }
}
