package ru.boldyrev.otus.model.dto.detached;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.StorePosition;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DetachedOrderReservationItem
{
    StorePosition storePosition;
    Boolean entireStorePosition;
    Integer quantity;
}




