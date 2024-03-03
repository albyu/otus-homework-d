package ru.boldyrev.otus.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.model.dto.rest.*;
import ru.boldyrev.otus.service.ConfirmService;
import ru.boldyrev.otus.service.StoreService;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Бэкенд операции со складом")
public class BackendController {
    private final StoreService storeService;

    private final ConfirmService confirmService;

    @PostMapping(value = "/store/arrive", consumes = "application/json", produces = "application/json")
    ResponseEntity<Map<String, String>> storeArrival(@RequestBody List<RestArrivalItem> restArrival) throws ConflictException {
        storeService.arrive(restArrival);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "ok"));
    }

    @PostMapping(value = "/store/post", consumes = "application/json", produces = "application/json")
    ResponseEntity<RestStore> storeAdjust(@RequestBody RestStore restStore) {
         restStore = storeService.storeAdjust(restStore);
         return ResponseEntity.status(HttpStatus.OK).body(restStore);
    }

    @PostMapping(value = "/product/post", consumes = "application/json", produces = "application/json")
    ResponseEntity<RestProduct> productAdjust(@RequestBody RestProduct restProduct) {
        restProduct = storeService.productAdjust(restProduct);
        return ResponseEntity.status(HttpStatus.OK).body(restProduct);
    }

    @PutMapping(value = "/order/process", consumes = "application/json", produces = "application/json")
    ResponseEntity<RestOrderNotification> orderProcess(@RequestBody RestOrderNotification restOrderNotification) throws ConflictException {
        restOrderNotification = confirmService.processOrder(restOrderNotification);
        return ResponseEntity.status(HttpStatus.OK).body(restOrderNotification);
    }


    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> accountExceptionProcessing(ConflictException conflictException) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorReason", conflictException.getMessage()));
    }

}
