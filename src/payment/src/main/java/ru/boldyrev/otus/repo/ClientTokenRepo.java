package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import ru.boldyrev.otus.model.entity.ClientToken;
import ru.boldyrev.otus.model.enums.TokenType;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClientTokenRepo extends JpaRepository<ClientToken, Long> {

    /*Поиск в репозитории по clientId и tokenType*/
    @Query("SELECT ct FROM ClientToken ct WHERE ct.client.id = :clientId AND ct.tokenType = :tokenType")
    List<ClientToken> findByClientIdAndTokenType(@Param("clientId") String clientId,
                                                 @Param("tokenType") TokenType tokenType);

    @Query("SELECT ct FROM ClientToken ct WHERE ct.client.id = :clientId AND ct.externalTokenId = :externalTokenId")
    List<ClientToken> findByClientIdAndExternalTokenId(@Param("clientId") String clientId,
                                                       @Param("externalTokenId") String externalTokenId);

    @Query("SELECT ct FROM ClientToken ct WHERE ct.client.id = :clientId AND ct.id = :id")
    List<ClientToken> findByClientIdAndId(@Param("clientId") String clientId,
                                          @Param("id") Long id);


    @Query("SELECT ct FROM ClientToken ct WHERE ct.client.id = :clientId")
    List<ClientToken> findByClientId(@Param("clientId") String clientId);

}
