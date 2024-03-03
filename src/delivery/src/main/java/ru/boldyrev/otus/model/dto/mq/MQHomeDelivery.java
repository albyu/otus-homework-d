package ru.boldyrev.otus.model.dto.mq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.HomeDelivery;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQHomeDelivery {
    private String companyName;
    private double cost;
    private String address;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate deliveryDate;
    private int hourFrom;
    private int hourTo;
    public MQHomeDelivery (HomeDelivery homeDelivery){
      this.companyName = homeDelivery.getCompany().getCompanyName();
      this.cost = homeDelivery.getCost();

      this.deliveryDate = homeDelivery.getDeliveryDate();
      this.hourFrom = homeDelivery.getHourFrom();
      this.hourTo = homeDelivery.getHourTo();
    }
}
