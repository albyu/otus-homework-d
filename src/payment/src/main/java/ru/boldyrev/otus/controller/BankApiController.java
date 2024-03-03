package ru.boldyrev.otus.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.exception.NotFoundException;
import ru.boldyrev.otus.exception.NotSufficientFundsException;
import ru.boldyrev.otus.model.dto.rest.RestClientToken;
import ru.boldyrev.otus.model.dto.rest.RestExternalPayAdviceRequest;
import ru.boldyrev.otus.model.dto.rest.RestExternalPayAdviceResponse;
import ru.boldyrev.otus.service.TokenService;

import java.util.Map;

@RestController
@Slf4j
@Api(tags = "Bank API интерфейс")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BankApiController {
    private final TokenService tokenService;

    @ApiOperation(value = "Получить результат оплаты от Bank API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = RestClientToken.class),
            @ApiResponse(code = 404, message = "[Entity] not found"),
            @ApiResponse(code = 409, message = "[Entity] in inappropriate state")

    })
    @PostMapping(value = "/order/payresult", consumes = "application/json", produces = "application/json")
    public ResponseEntity<RestExternalPayAdviceResponse> processPayResult(@RequestBody RestExternalPayAdviceRequest externalPayAdviceRequest) throws ConflictException, NotFoundException {
        RestExternalPayAdviceResponse externalPayAdviceResponse = tokenService.processPayResult(externalPayAdviceRequest);
        return ResponseEntity.status(HttpStatus.OK).body(externalPayAdviceResponse);

    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> orderExceptionProcessing(ConflictException conflictException) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorReason", conflictException.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> orderExceptionProcessing(NotFoundException conflictException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorReason", conflictException.getMessage()));
    }
}
