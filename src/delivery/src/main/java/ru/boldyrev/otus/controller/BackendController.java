package ru.boldyrev.otus.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.model.dto.rest.RestCompany;
import ru.boldyrev.otus.model.dto.rest.RestOrderDeliveryCommand;
import ru.boldyrev.otus.model.dto.rest.RestPickupPoint;
import ru.boldyrev.otus.service.ConfirmService;
import ru.boldyrev.otus.service.DeliveryService;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Бэкенд операции с доставкой")
public class BackendController {
    private final DeliveryService deliveryService;

    private final ConfirmService confirmService;


    @PutMapping(value = "/order/process", consumes = "application/json", produces = "application/json")
    ResponseEntity<RestOrderDeliveryCommand> orderProcess(@RequestBody RestOrderDeliveryCommand restOrderDeliveryCommandNotification) throws ConflictException {
        restOrderDeliveryCommandNotification = confirmService.processOrder(restOrderDeliveryCommandNotification);
         return ResponseEntity.status(HttpStatus.OK).body(restOrderDeliveryCommandNotification);
    }

    @PostMapping(value = "/pickup/post", consumes = "application/json", produces = "application/json")
    ResponseEntity<RestPickupPoint> pickupAdjust(@RequestBody RestPickupPoint restPickup) {
        restPickup = deliveryService.pickupAdjust(restPickup);
        return ResponseEntity.status(HttpStatus.OK).body(restPickup);
    }

    @PostMapping(value = "/company/post", consumes = "application/json", produces = "application/json")
    ResponseEntity<RestCompany> companyAdjust(@RequestBody RestCompany restCompany) throws ConflictException {
        restCompany = deliveryService.companyAdjust(restCompany);
        return ResponseEntity.status(HttpStatus.OK).body(restCompany);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(ConflictException conflictException) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorReason", conflictException.getMessage()));
    }
}
