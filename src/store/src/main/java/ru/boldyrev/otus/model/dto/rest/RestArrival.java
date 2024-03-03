package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RestArrival {
    Long id;
    List<RestArrivalItem> items;
}
