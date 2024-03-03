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
import ru.boldyrev.otus.exception.AuthErrorException;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.exception.NotAuthorizedException;
import ru.boldyrev.otus.exception.NotFoundException;
import ru.boldyrev.otus.model.dto.rest.RestStorePosition;
import ru.boldyrev.otus.service.CheckAuthService;
import ru.boldyrev.otus.service.StoreService;

import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Операции со счетом")
public class ClientController {


    private final CheckAuthService checkAuthService;

    private final StoreService storeService;


    /* GET payment/token/get Получение списка привязанных средств платежа клиента */
    @ApiOperation(value = "Получить список рапределения товаров по складам")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = RestStorePosition.class),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Authorization Error")
    })
    @GetMapping(value = "/storeposition/get", produces = "application/json")
    public ResponseEntity<List<RestStorePosition>> getStorePositions(@RequestParam(name = "productId") Long productId,
                                                                    @CookieValue(value = "session_id", required = false) String sessionId) throws NotAuthorizedException, AuthErrorException {
        checkAuthService.checkAuth(sessionId);
        List<RestStorePosition> positions = storeService.getStorePositions(productId);
        return ResponseEntity.status(HttpStatus.OK).body(positions);
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
