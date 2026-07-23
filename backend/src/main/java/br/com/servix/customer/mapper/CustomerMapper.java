package br.com.servix.customer.mapper;

import br.com.servix.customer.domain.Customer;
import br.com.servix.customer.dto.CustomerRequest;
import br.com.servix.customer.dto.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        apply(request, customer);
        return customer;
    }

    public void apply(CustomerRequest request, Customer customer) {
        customer.setNome(request.nome());
        customer.setCpfCnpj(request.cpfCnpj());
        customer.setEmail(request.email());
        customer.setTelefone(request.telefone());
        customer.setTelefoneSecundario(request.telefoneSecundario());
        customer.setCep(request.cep());
        customer.setLogradouro(request.logradouro());
        customer.setNumero(request.numero());
        customer.setComplemento(request.complemento());
        customer.setBairro(request.bairro());
        customer.setCidade(request.cidade());
        customer.setEstado(request.estado());
        customer.setObservacoes(request.observacoes());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCompanyId(),
                customer.getNome(),
                customer.getCpfCnpj(),
                customer.getEmail(),
                customer.getTelefone(),
                customer.getTelefoneSecundario(),
                customer.getCep(),
                customer.getLogradouro(),
                customer.getNumero(),
                customer.getComplemento(),
                customer.getBairro(),
                customer.getCidade(),
                customer.getEstado(),
                customer.getObservacoes(),
                customer.isAtivo(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getCreatedBy(),
                customer.getUpdatedBy());
    }
}
