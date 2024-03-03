package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldyrev.otus.model.entity.ClientToken;
import ru.boldyrev.otus.model.entity.PayRequest;

import java.util.List;

import ru.boldyrev.otus.model.enums.*;

public interface PayRequestRepo  extends JpaRepository<PayRequest, Long> {
    List<PayRequest> findByOrderIdAndPaymentResult(String orderId, PaymentResult paymentResult);

    List<PayRequest> findByOrderIdAndClientId(String orderId, String clientId);

    List<PayRequest> findByClientToken(ClientToken clientToken);
}
