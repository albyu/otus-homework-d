package ru.boldyrev.otus.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.boldyrev.otus.exception.*;

import ru.boldyrev.otus.model.dto.rest.*;
import ru.boldyrev.otus.model.entity.ClientToken;

import ru.boldyrev.otus.model.enums.TokenType;
import ru.boldyrev.otus.service.CheckAuthService;
import ru.boldyrev.otus.service.PaymentService;
import ru.boldyrev.otus.service.TokenService;

import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Операции со счетом")
public class ClientController {


    private final CheckAuthService checkAuthService;

    private final TokenService tokenService;


    /* GET payment/token/get Получение списка привязанных средств платежа клиента */
    @ApiOperation(value = "Получить список токенов клиента")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = RestClientToken.class),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 404, message = "[Entity] not found"),
            @ApiResponse(code = 500, message = "Authorization Error")
    })
    @GetMapping(value = "/token/get", produces = "application/json")
    public ResponseEntity<List<RestClientToken>> getTokens(@RequestParam(name = "clientId") String clientId,
                                                           @CookieValue(value = "session_id", required = false) String sessionId) throws NotAuthorizedException, AuthErrorException {
        checkAuthService.checkAuth(sessionId, clientId);
        List<RestClientToken> tokens = tokenService.getTokens(clientId);

        return ResponseEntity.status(HttpStatus.OK).body(tokens);
    }

    /*  PUT payment/token/adjust Добавление/удаление/редактирование средства платежа */
    @ApiOperation(value = "Изменить токены клиента (привязать / отвязать / поменять)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = RestClientToken.class),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 404, message = "[Entity] not found"),
            @ApiResponse(code = 409, message = "[Entity] in inappropriate state"),
            @ApiResponse(code = 500, message = "Authorization Error")
    })
    @PutMapping(value = "/token/adjust", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<RestClientToken>> adjustToken(@RequestBody List<RestClientToken> tokens,
                                                             @CookieValue(value = "session_id", required = false) String sessionId)
            throws NotAuthorizedException, AuthErrorException, NotFoundException, ConflictException {

        /* Аутентифицируем клиента */
        String clientId = checkAuthService.checkAuth(sessionId);
        List<RestClientToken> adjustedTokens = tokenService.adjustTokens(tokens, clientId);
        return ResponseEntity.status(HttpStatus.OK).body(adjustedTokens);
    }

//    @ApiOperation(value = "Получить список платежных запросов для токена")
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Success", response = RestClientToken.class),
//            @ApiResponse(code = 401, message = "Not authorized"),
//            @ApiResponse(code = 404, message = "[Entity] not found"),
//            @ApiResponse(code = 500, message = "Authorization Error")
//    })
//    @GetMapping(value = "/token/getrequest", produces = "application/json")
//    public ResponseEntity<List<RestPayRequest>> getPayRequestsByToken(@RequestParam(name = "tokenId") Long tokenId,
//                                                                      @CookieValue(value = "session_id", required = false) String sessionId)
//            throws NotFoundException, NotAuthorizedException, AuthErrorException {
//
//        /* Аутентификация клиента */
//        String clientId = checkAuthService.checkAuth(sessionId);
//
//        /* Достанем токен*/
//        ClientToken clientToken = tokenService.getTokenByParams(clientId, new RestClientToken().setId(tokenId));
//
//
//        List<RestPayRequest> payRequests = tokenService.getPayRequestsByToken(clientToken);
//
//        return ResponseEntity.status(HttpStatus.OK).body(payRequests);
//    }


    @ApiOperation(value = "Получить список платежных запросов для токена")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = RestClientToken.class),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 404, message = "[Entity] not found"),
            @ApiResponse(code = 500, message = "Authorization Error")
    })
    @GetMapping(value = "/token/getrequest", produces = "application/json")
    public ResponseEntity<List<RestPayRequest>> getPayRequestsByToken(@RequestParam(name = "tokenId", required = false) Long tokenId,
                                                                      @RequestParam(name = "tokenType", required = false) TokenType tokenType,
                                                                      @RequestParam(name = "externalTokenId", required = false) String externalTokenId,
                                                                      @CookieValue(value = "session_id", required = false) String sessionId)
            throws NotFoundException, NotAuthorizedException, AuthErrorException {

        /* Аутентификация клиента */
        String clientId = checkAuthService.checkAuth(sessionId);

        /* Достанем токен*/
        ClientToken clientToken = tokenService.getTokenByParams(clientId, new RestClientToken().setId(tokenId).setExternalTokenId(externalTokenId).setTokenType(tokenType));

        List<RestPayRequest> payRequests = tokenService.getPayRequestsByToken(clientToken);

        return ResponseEntity.status(HttpStatus.OK).body(payRequests);
    }


    /* GET payment/order/getreqpay Получить статус оплаты заказа */
    @ApiOperation(value = "Получить список платежных запросов для заказа")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = RestClientToken.class),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Authorization Error")
    })
    @GetMapping(value = "/order/getrequest", produces = "application/json")
    public ResponseEntity<List<RestPayRequest>> getPayRequestsByOrder(@RequestParam(name = "orderId") String orderId,
                                                                 @CookieValue(value = "session_id", required = false) String sessionId)
            throws NotAuthorizedException, AuthErrorException {

        String clientId = checkAuthService.checkAuth(sessionId);

        /*Получить список запросов */
        List<RestPayRequest> payRequests = tokenService.getPayRequestsByOrder(orderId, clientId);

        return ResponseEntity.status(HttpStatus.OK).body(payRequests);
    }


    /* POST /payment/order/pay Выполнение платежа */
    @ApiOperation(value = "Оплата заказа")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = RestClientToken.class),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 404, message = "[Entity] not found"),
            @ApiResponse(code = 409, message = "[Entity] in inappropriate state"),
            @ApiResponse(code = 500, message = "Authorization Error")
    })
    @PostMapping(value = "/order/pay", consumes = "application/json", produces = "application/json")
    public ResponseEntity<RestPayResponse> payOrder(@RequestBody RestPayRequest restPayRequest,
                                                    @CookieValue(value = "session_id", required = false) String sessionId)
            throws NotAuthorizedException, AuthErrorException, ConflictException {

        checkAuthService.checkAuth(sessionId, restPayRequest.getClientId());
        RestPayResponse payResponse = tokenService.payOrder(restPayRequest);
        return ResponseEntity.status(HttpStatus.OK).body(payResponse);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(ConflictException conflictException) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorReason", conflictException.getMessage()));
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(NotAuthorizedException notAuthorizedException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorReason", notAuthorizedException.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(NotFoundException conflictException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorReason", conflictException.getMessage()));
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(EntityExistsException orderException) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorReason", "Cannot change object, try to refresh with /order/get"));
    }

    /*Перехватываем тут, потому что при перехвате внутри @Transactional прокси-класс кидает свое исключение и все равно перехватывать тут еще раз*/
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(OptimisticLockingFailureException orderException) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorReason", "Object was changed by another transaction, try to refresh with /order/get"));
    }

    @ExceptionHandler(AuthErrorException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(AuthErrorException authErrorException) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorReason", authErrorException.getMessage()));
    }


}
