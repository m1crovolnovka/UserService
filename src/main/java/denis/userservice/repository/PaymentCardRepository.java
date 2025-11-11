package denis.userservice.repository;

import denis.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID> {
    @Query(
            value = "SELECT * FROM payment_card WHERE user_id = :userId",
            nativeQuery = true
    )
    List<PaymentCard> findAllCardsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE PaymentCard c SET c.active = true WHERE c.id = :cardId")
    void activate(@Param("cardId") UUID userId);

    @Modifying
    @Query("UPDATE PaymentCard c SET c.active = false WHERE c.id = :userId")
    void deactivate(@Param("cardId") UUID userId);

}
