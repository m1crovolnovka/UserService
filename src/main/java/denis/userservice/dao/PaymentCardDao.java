package denis.userservice.dao;

import denis.userservice.entity.PaymentCard;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PaymentCardDao {
    PaymentCard create(UUID userId, PaymentCard card);
    PaymentCard getById(UUID id);
    Page<PaymentCard> getAll(int page, int size);
    List<PaymentCard> getByUserId(UUID userId);
    PaymentCard update(UUID id, PaymentCard newData);
    void activate(UUID id);
    void deactivate(UUID id);
    void delete(UUID id);
}
