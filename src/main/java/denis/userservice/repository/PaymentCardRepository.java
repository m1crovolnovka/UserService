package denis.userservice.repository;

import denis.userservice.entity.PaymentCard;
import denis.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID>, JpaSpecificationExecutor<PaymentCard> {
    @Query(
            value = "SELECT * FROM payment_cards WHERE user_id = :userId",
            nativeQuery = true
    )
    List<PaymentCard> findAllCardsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE PaymentCard c SET c.active = true WHERE c.id = :cardId")
    void activate(@Param("cardId") UUID cardId);

    @Modifying
    @Query("UPDATE PaymentCard c SET c.active = false WHERE c.id = :cardId")
    void deactivate(@Param("cardId") UUID cardId);

}
