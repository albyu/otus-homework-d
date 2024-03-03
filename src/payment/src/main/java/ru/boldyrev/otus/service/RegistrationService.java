package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.model.dto.mq.MQClient;
import ru.boldyrev.otus.model.enums.TokenType;
import ru.boldyrev.otus.repo.AccountRepo;
import ru.boldyrev.otus.repo.ClientRepo;
import ru.boldyrev.otus.repo.ClientTokenRepo;
import ru.boldyrev.otus.model.entity.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RegistrationService {

    private final ClientRepo clientRepo;

    private final ClientTokenRepo clientTokenRepo;

    private final AccountRepo accountRepo;


    @Transactional
    public void register(MQClient mqClient) {
        /* Проверяем есть ли клиент */
        Optional<Client> optionalClient = clientRepo.findById(mqClient.getClientId());
        Client client;
        /* Если нет, создаем клиента */
        if (optionalClient.isEmpty()){
            client = new Client().setClientId(mqClient.getClientId());
            client = clientRepo.saveAndFlush(client);
        } else {
            client = optionalClient.get();
        }

        /* Проверяем есть ли токен */
        List<ClientToken> clientTokens = clientTokenRepo.findByClientIdAndTokenType(mqClient.getClientId(), TokenType.WALLET);
        ClientToken clientToken;

        /* Если нет, то создаем токен и счет*/
        if (clientTokens.isEmpty()){

            /* создаем токен */
            clientToken = new ClientToken().setClient(client).setTokenType(TokenType.WALLET);
            clientToken = clientTokenRepo.saveAndFlush(clientToken);
        } else {
            clientToken = clientTokens.get(0);
        }

        /*Проверяем, есть ли account*/
        if (clientToken.getAccount() == null){
            /* Создаем счет */
            Account account = new Account().setBalance(0);
            account = accountRepo.saveAndFlush(account);

            /* Привязываем к ClientToken */
            clientToken.setAccount(account);
            clientToken = clientTokenRepo.saveAndFlush(clientToken);
        }
    }
}
