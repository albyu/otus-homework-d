package ru.boldyrev.otus.model.dto.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class RestDeliveryOption {
    private String address;
    private String companyName;
    private double cost;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate estimatedDate;
}
