package denis.userservice.dao.Impl;


import denis.userservice.dao.PaymentCardDao;
import denis.userservice.entity.PaymentCard;
import denis.userservice.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class PaymentCardDaoImpl implements PaymentCardDao {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public PaymentCard create(UUID userId, PaymentCard card) {
        User user = entityManager.find(User.class, userId);
        if (user == null) throw new RuntimeException("User not found");
        Long count = entityManager.createQuery(
                        "SELECT COUNT(c) FROM PaymentCard c WHERE c.user.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();

        if (count >= 5)
            throw new RuntimeException("User already has 5 cards");

        card.setUser(user);
        card.setActive(true);
        entityManager.persist(card);
        return card;
    }

    @Override
    public PaymentCard getById(UUID id) {
        return entityManager.find(PaymentCard.class, id);
    }

    @Override
    public Page<PaymentCard> getAll(int page, int size) {
        TypedQuery<PaymentCard> query = entityManager.createQuery(
                "SELECT c FROM PaymentCard c",
                PaymentCard.class);

        query.setFirstResult(page * size);
        query.setMaxResults(size);
        List<PaymentCard> cards = query.getResultList();

        Long total = entityManager.createQuery(
                        "SELECT COUNT(c) FROM PaymentCard c", Long.class)
                .getSingleResult();

        return new PageImpl<>(cards, PageRequest.of(page, size), total);
    }

    @Override
    public List<PaymentCard> getByUserId(UUID userId) {
        return entityManager.createQuery(
                        "SELECT c FROM PaymentCard c WHERE c.user.id = :userId",
                        PaymentCard.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public PaymentCard update(UUID id, PaymentCard newData) {
        PaymentCard existing = entityManager.find(PaymentCard.class, id);
        if (existing == null) {
            throw new EntityNotFoundException("PaymentCard not found for ID: " + id);
        }
        existing.setNumber(newData.getNumber());
        existing.setHolder(newData.getHolder());
        existing.setExpirationDate(newData.getExpirationDate());
        existing.setActive(newData.getActive());
        return entityManager.merge(existing);
    }

    @Override
    public void activate(UUID id) {
        entityManager.createNativeQuery(
                        "UPDATE payment_cards SET active = true WHERE id = ?1")
                .setParameter(1, id)
                .executeUpdate();
    }

    @Override
    public void deactivate(UUID id) {
        entityManager.createNativeQuery(
                        "UPDATE payment_cards SET active = false WHERE id = ?1")
                .setParameter(1, id)
                .executeUpdate();
    }

    @Override
    public void delete(UUID id) {
        PaymentCard card = entityManager.find(PaymentCard.class, id);
        if (card != null) {
            entityManager.remove(card);
        }
    }

}
