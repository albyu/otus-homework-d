package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQHomeDelivery {
    private String companyName;
    private double cost;

}
