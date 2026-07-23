package br.com.servix.serviceorder.repository;

import br.com.servix.serviceorder.domain.ServiceOrderHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOrderHistoryRepository extends JpaRepository<ServiceOrderHistory, UUID> {

    List<ServiceOrderHistory> findAllByServiceOrderIdOrderByChangedAtAsc(UUID serviceOrderId);
}
