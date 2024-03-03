package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQClient {
    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String password;
}
