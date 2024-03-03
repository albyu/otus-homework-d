package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.exception.NotSufficientFundsException;
import ru.boldyrev.otus.model.dto.mq.MQPayRequest;
import ru.boldyrev.otus.model.entity.Account;
import ru.boldyrev.otus.model.entity.ClientToken;
import ru.boldyrev.otus.model.entity.PayRequest;
import ru.boldyrev.otus.model.entity.Transaction;
import ru.boldyrev.otus.model.enums.PaymentGoal;
import ru.boldyrev.otus.model.enums.PaymentResult;
import ru.boldyrev.otus.model.enums.TokenType;
import ru.boldyrev.otus.model.enums.TransactionType;
import ru.boldyrev.otus.repo.AccountRepo;
import ru.boldyrev.otus.repo.ClientTokenRepo;
import ru.boldyrev.otus.repo.TransactionRepo;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentService {
    private final TransactionRepo transactionRepo;
    private final AccountRepo accountRepo;
    private final NotificationService notificationService;
    private final ClientTokenRepo clientTokenRepo;


    public PayRequest reverse(PayRequest payRequest) throws NotSufficientFundsException {

        /* Смотрим, по какому токену был платеж  */
        if (payRequest.getClientToken().getTokenType() == TokenType.WALLET) {
            /*Если по кошельку, надо вернуть средства в кошелек*/
            Transaction transaction = new Transaction()
                    .setTransactionType(TransactionType.REVERSAL)
                    .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                    .setPayRequest(payRequest)
                    .setAmount(payRequest.getAmount())
                    .setAccount(payRequest.getClientToken().getAccount());

            transaction = carryout(transaction);
        } else {
            /* Если платеж шел на пополнение кошелька, отменим его */
            if (payRequest.getPaymentGoal() == PaymentGoal.WALLET_CREDIT) {
                Transaction transaction = new Transaction()
                        .setTransactionType(TransactionType.CREDIT_REVERSAL)
                        .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                        .setPayRequest(payRequest)
                        .setAmount(payRequest.getAmount())
                        .setAccount(payRequest.getClientToken().getAccount());

                transaction = carryout(transaction);
            }
            /*Тут сценарий отмены платежа через bank-api*/

            /* Отменяем платеж */

        }
        payRequest.setPaymentResult(PaymentResult.REVERSED);
        notificationService.sendNotification(new MQPayRequest(payRequest));

        return payRequest;
    }

    public PayRequest charge(PayRequest payRequest) {
        try {
            /* Создаем транзакцию */
            Transaction paymentTransaction = new Transaction()
                    .setTransactionType(TransactionType.PAYMENT)
                    .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                    .setPayRequest(payRequest)
                    .setAmount(payRequest.getAmount())
                    .setAccount(payRequest.getClientToken().getAccount());

            paymentTransaction = carryout(paymentTransaction);

            /* Если платеж шел на пополнение кошелька, пополним его */
            if (payRequest.getPaymentGoal() == PaymentGoal.WALLET_CREDIT) {

                Transaction creditTransaction = new Transaction()
                        .setTransactionType(TransactionType.CREDIT)
                        .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                        .setPayRequest(payRequest)
                        .setAmount(payRequest.getAmount())
                        .setAccount(payRequest.getClientToken().getAccount());

                creditTransaction = carryout(creditTransaction);
            }
            /* Платеж прошел */
            payRequest.setPaymentResult(PaymentResult.SUCCESS);
        } catch (NotSufficientFundsException e) {
            payRequest.setPaymentResult(PaymentResult.NOT_SUFFICIENT_FUNDS);
        }
        notificationService.sendNotification(new MQPayRequest(payRequest));
        return payRequest;
    }

    public PayRequest credit(PayRequest payRequest, Account accountForCredit)  {

        try {
            Transaction creditTransaction = new Transaction()
                    .setTransactionType(TransactionType.CREDIT)
                    .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                    .setPayRequest(payRequest)
                    .setAmount(payRequest.getAmount())
                    .setAccount(accountForCredit);

            creditTransaction = carryout(creditTransaction);
            payRequest.setPaymentResult(PaymentResult.SUCCESS);

        }
        catch (NotSufficientFundsException e){
            payRequest.setPaymentResult(PaymentResult.NOT_SUFFICIENT_FUNDS);

        }
        return payRequest;
    }




    public Transaction carryout(Transaction transaction) throws NotSufficientFundsException {
        Account account = transaction.getAccount();

        /*Добавляем средств*/
        if (transaction.getTransactionType() == TransactionType.CREDIT || transaction.getTransactionType() == TransactionType.REVERSAL) {
            account.setBalance(account.getBalance() + transaction.getAmount());
        }
        /*Убавляем средств*/
        else if (transaction.getTransactionType() == TransactionType.PAYMENT || transaction.getTransactionType() == TransactionType.CREDIT_REVERSAL) {
            if (transaction.getAmount() <= account.getBalance()) {
                account.setBalance(account.getBalance() - transaction.getAmount());
            } else {
                throw new NotSufficientFundsException("Недостаточно средств на счете");
            }
        }
        /* Сохраняем счет */
        account = accountRepo.saveAndFlush(account);

        /* Сохраняем транзакцию */
        transaction = transactionRepo.saveAndFlush(transaction);

        return transaction;

    }


}
