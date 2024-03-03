package ru.boldyrev.otus.model.dto.mq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQClient {
    @JsonProperty("username")
    private String clientId;

}
