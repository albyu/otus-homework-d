package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.*;
import ru.boldyrev.otus.model.dto.mq.MQPayRequest;
import ru.boldyrev.otus.model.dto.rest.*;
import ru.boldyrev.otus.model.entity.*;
import ru.boldyrev.otus.model.enums.PaymentGoal;
import ru.boldyrev.otus.model.enums.PaymentResult;
import ru.boldyrev.otus.model.enums.TokenType;
import ru.boldyrev.otus.repo.ClientRepo;
import ru.boldyrev.otus.repo.ClientTokenRepo;
import ru.boldyrev.otus.repo.PayRequestRepo;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TokenService {
    private final ClientTokenRepo clientTokenRepo;

    private final ClientRepo clientRepo;

    private final PayRequestRepo payRequestRepo;

    private final PaymentService paymentService;

    private final NotificationService notificationService;

    public List<RestClientToken> getTokens(String clientId) {
        return clientTokenRepo.findByClientId(clientId).stream().map(RestClientToken::new).toList();
    }

    public ClientToken getTokenById(Long id) throws NotFoundException {
        Optional<ClientToken> optionalClientToken = clientTokenRepo.findById(id);
        if (optionalClientToken.isPresent()) {
            return optionalClientToken.get();
        } else {
            throw new NotFoundException("Токен " + id + " не найден");
        }
    }

    public ClientToken getTokenByParams(String clientId, RestClientToken requiredToken) throws NotFoundException {

        List<ClientToken> clientTokenList = new ArrayList<>();

        /* Если для токен задан id, то ищем по id*/
        if (requiredToken.getId() != null) {
            clientTokenList = clientTokenRepo.findByClientIdAndId(clientId, requiredToken.getId());
        }
        /* Если запрошена оплата по бумажнику, то найдем бумажник клиента */
        else if (requiredToken.getTokenType() == TokenType.WALLET) {
            clientTokenList = clientTokenRepo.findByClientIdAndTokenType(clientId, TokenType.WALLET);
        }
        /* Если оплата по банковской карте и задан номер, то поищем такой токен у клиента*/
        else if (requiredToken.getTokenType() == TokenType.BANK_CARD && requiredToken.getExternalTokenId() != null) {
            clientTokenList = clientTokenRepo.findByClientIdAndExternalTokenId(clientId, requiredToken.getExternalTokenId());
        }
        if (clientTokenList.isEmpty())
            throw new NotFoundException("Токен не найден");
        else
            return clientTokenList.get(0);
    }

    public RestClientToken createToken(RestClientToken token, String clientId) throws ConflictException, NotFoundException {
        /* Валидация */
        if (token.getTokenType() == TokenType.WALLET) {
            throw new ConflictException("Невозможно привязать еще один кошелек");
        }
        if (token.getTokenType() == TokenType.BANK_CARD && token.getExternalTokenId() == null) {
            throw new ConflictException("Для bank card externalTokenId должен быть задан");
        }

        /* Поиск клиента */
        Optional<Client> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new NotFoundException("Клиент не найден");
        }

        /* Новый токен */
        ClientToken newToken = new ClientToken().setTokenType(token.getTokenType())
                .setExternalTokenId(token.getExternalTokenId())
                .setClient(client.get())
                .setTokenType(token.getTokenType())
                .setDescription(token.getDescription());

        newToken = clientTokenRepo.saveAndFlush(newToken);

        return new RestClientToken(newToken);
    }

    public RestClientToken updateToken(ClientToken existingToken, RestClientToken token) throws ConflictException {
        /* Валидация */
        if (token.getTokenType() != existingToken.getTokenType()) {
            throw new ConflictException("Невозможно изменить тип токена");
        }

        existingToken.setTokenType(token.getTokenType())
                .setExternalTokenId(token.getExternalTokenId())
                .setDescription(token.getDescription());

        existingToken = clientTokenRepo.saveAndFlush(existingToken);

        return new RestClientToken(existingToken);
    }

    private void deleteToken(ClientToken token) {
        clientTokenRepo.delete(token);
    }


    @Transactional
    public List<RestClientToken> adjustTokens(List<RestClientToken> newTokens, String clientId) throws ConflictException, NotFoundException, NotAuthorizedException {

        /*Удалить все токены, которых нет в новом списке*/
        List<ClientToken> tokens = clientTokenRepo.findByClientId(clientId);
        for (ClientToken token : tokens) {
            if (token.getTokenType() != TokenType.WALLET) { /* Бумажник нельзя удалить */
                if (newTokens.stream().noneMatch((i) -> (Objects.equals(i.getId(), token.getId())))) {
                    deleteToken(token);
                }
            }
        }

        /* По всем токенам из нового списка*/
        for (RestClientToken token : newTokens) {
            if (token.getId() == null) { /*Создаем новый токен*/
                token = createToken(token, clientId);

            } else {
                /* Найти токен в базе данных по id*/
                ClientToken existingToken = getTokenById(token.getId());
                if (!Objects.equals(clientId, existingToken.getClient().getClientId())) {
                    throw new NotAuthorizedException("Client not authorized");
                }
                /**/
                token = updateToken(existingToken, token);
            }
        }


        return clientTokenRepo.findByClientId(clientId).stream().map(RestClientToken::new).toList();

    }

    public List<RestPayRequest> getPayRequestsByToken(ClientToken clientToken) {
        List<PayRequest> payRequests = payRequestRepo.findByClientToken(clientToken);
        return payRequests.stream().map(RestPayRequest::new).toList();
    }


    public List<RestPayRequest> getPayRequestsByOrder(String orderId, String clientId) {
        List<PayRequest> payRequests = payRequestRepo.findByOrderIdAndClientId(orderId, clientId);
        return payRequests.stream().map(RestPayRequest::new).toList();
    }

    @Transactional
    public RestPayResponse payOrder(RestPayRequest restPayRequest) throws ConflictException, AuthErrorException {

        RestPayResponse response = new RestPayResponse();
        String clientId = restPayRequest.getClientId();

        /* Если токена нет, то ошибка */
        if (restPayRequest.getClientToken() == null) throw new ConflictException("Токен не задан");
        ClientToken clientToken;

        try {
            clientToken = getTokenByParams(clientId, restPayRequest.getClientToken());
        } catch (NotFoundException e) {
            throw new ConflictException("Заданный токен не найден у клиента");
        }

        /* Не был ли заказ уже оплачен ?*/
        /* Ищем в репозитории одобренный платеж для нашего запроса */
        List<PayRequest> payRequests = payRequestRepo.findByOrderIdAndPaymentResult(/* orderId */restPayRequest.getOrderId(), /*PayResult*/ PaymentResult.SUCCESS);
        if (!payRequests.isEmpty()) {
            throw new ConflictException("Заказ уже оплачен");
        }

        response.setOrderId(restPayRequest.getOrderId());


        /* Создаем Pay Request */
        PayRequest payRequest = new PayRequest().setOrderId(restPayRequest.getOrderId())
                .setClientToken(clientToken)
                .setAmount(restPayRequest.getAmount())
                .setPaymentGoal(restPayRequest.getPaymentGoal())
                .setClientId(restPayRequest.getClientId())
                .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                .setPaymentResult(PaymentResult.IN_PROGRESS);

        payRequest = payRequestRepo.saveAndFlush(payRequest);
        response.setPaymentRequestId(payRequest.getId());

        /* Теперь посмотрим на тип токена */
        if (payRequest.getClientToken().getTokenType() == TokenType.WALLET) {
            /* Оплата по кошельку */
            payRequest = paymentService.charge(payRequest);
            payRequest = payRequestRepo.saveAndFlush(payRequest);

            //notificationService.sendNotification(new MQPayRequest(payRequest));

            response.setPaymentResult(payRequest.getPaymentResult());

        } else {
            /* Оплата по внешнему средству платежа */
            /* Здесь мы должны создать ссылку на bank-api, вернем просто ссылку на tinkoff.ru*/
            String payLink = "https://tinkoff.ru";
            response.setExternalPayLink(payLink);
            response.setPaymentResult(PaymentResult.IN_PROGRESS);

        }

        response.setClientToken(new RestClientToken(clientToken));


        return response;

    }

    /* Обработка внешнего уведомления о севершении платежа*/
    @Transactional
    public RestExternalPayAdviceResponse processPayResult(RestExternalPayAdviceRequest externalPayResponse) throws NotFoundException, ConflictException {
        Optional<PayRequest> optionalPayRequest = payRequestRepo.findById(externalPayResponse.getInternalPaymentId());
        RestExternalPayAdviceResponse response = new RestExternalPayAdviceResponse()
                .setOrderId(externalPayResponse.getOrderId())
                .setExternalPaymentId(externalPayResponse.getExternalPaymentId())
                .setInternalPaymentId(externalPayResponse.getInternalPaymentId());

        if (optionalPayRequest.isEmpty()) {
            throw new NotFoundException("Pay request not found");
        }

        PayRequest payRequest = optionalPayRequest.get();

        if (!Objects.equals(payRequest.getClientToken().getExternalTokenId(), externalPayResponse.getExternalTokenId()) ||
                payRequest.getClientToken().getTokenType() != externalPayResponse.getTokenType() ||
                payRequest.getAmount() != externalPayResponse.getAmount()
        ) {

            throw new ConflictException("Данные платежа не совпадают");
        }

        if (payRequest.getPaymentResult() == PaymentResult.IN_PROGRESS) {

            /* Ставим внутреннему запросу тот статус, который нам прислали */
            payRequest.setPaymentResult(externalPayResponse.getPaymentResult()).
                    setExternalPaymentId(externalPayResponse.getExternalPaymentId());

            /*Если надо кредитовать кошелек, сделаем это здесь*/
            if (payRequest.getPaymentGoal() == PaymentGoal.WALLET_CREDIT && /* цель - кредитование*/
                    payRequest.getPaymentResult() == PaymentResult.SUCCESS) { /* результат - успех */

                /* Сперва найдем кошелек клиента */
                List<ClientToken> clientTokens = clientTokenRepo.findByClientIdAndTokenType(payRequest.getClientId(), TokenType.WALLET);
                if (clientTokens.isEmpty()) {
                    throw new ConflictException("Не найден кошелек клиента");
                }
                Account accountForCredit = clientTokens.get(0).getAccount();

                /* Теперь можно пополнять */
                payRequest = paymentService.credit(payRequest, accountForCredit);

                /* Копируем конечный результат в ответное сообщение банку */
                response.setFinalPaymentResult(payRequest.getPaymentResult());
            }

            /* Шлем уведомление */
            //notificationService.sendNotification(new MQPayRequest(payRequest));

        } else {
            /* Обеспечиваем идемпотентность. Если payRequest уже зафинален, просто вернем статус */
            response.setFinalPaymentResult(payRequest.getPaymentResult());
        }
        payRequestRepo.saveAndFlush(payRequest);
        return response;

    }
}
