package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.NotificationType;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "NOTIFICATIONS")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String clientId;

    String email;

    NotificationType notificationType;

    Timestamp timestamp;

    String text;

    Boolean isDeadNotification;
}
