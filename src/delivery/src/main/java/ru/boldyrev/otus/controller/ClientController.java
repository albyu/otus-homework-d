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
import ru.boldyrev.otus.exception.AuthErrorException;
import ru.boldyrev.otus.exception.NotAuthorizedException;
import ru.boldyrev.otus.model.dto.rest.RestDeliveryOption;
import ru.boldyrev.otus.model.dto.rest.RestPickupPoint;
import ru.boldyrev.otus.service.CheckAuthService;
import ru.boldyrev.otus.service.DeliveryService;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Клиентские запросы")
public class ClientController {
    private final CheckAuthService checkAuthService;

    private final DeliveryService deliveryService;

    //##### GET /delivery/pickup: Получить список точек доставки
    @ApiOperation(value = "Получить список точек доставки")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = RestPickupPoint.class),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Authorization Error")
    })
    @GetMapping(value = "/pickup/get", produces = "application/json")
    public ResponseEntity<List<RestPickupPoint>> getPickups(@RequestParam(name = "addressFilter", required = false) String addressFilter,
                                                            @CookieValue(value = "session_id", required = false) String sessionId) throws NotAuthorizedException, AuthErrorException {
        checkAuthService.checkAuth(sessionId);

        List<RestPickupPoint> pickups = deliveryService.getPickups(addressFilter);
        return ResponseEntity.status(HttpStatus.OK).body(pickups);
    }


    //##### GET /delivery/options: Получить варианты доставки по указанному адресу
    @ApiOperation(value = "Получить список опций доставки на дом")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = RestDeliveryOption.class),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Authorization Error")
    })
    @GetMapping(value = "/option/get", produces = "application/json")
    public ResponseEntity<List<RestDeliveryOption>> getOptions(@RequestParam(name = "address") String address,
                                                               @CookieValue(value = "session_id", required = false) String sessionId) throws NotAuthorizedException, AuthErrorException {
        checkAuthService.checkAuth(sessionId);

        List<RestDeliveryOption> options = deliveryService.getDeliveryOption(address);
        return ResponseEntity.status(HttpStatus.OK).body(options);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(NotAuthorizedException notAuthorizedException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorReason", notAuthorizedException.getMessage()));
    }

    @ExceptionHandler(AuthErrorException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(AuthErrorException authErrorException) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorReason", authErrorException.getMessage()));
    }





}
