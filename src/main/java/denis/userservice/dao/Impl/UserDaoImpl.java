package denis.userservice.dao.Impl;

import denis.userservice.dao.UserDao;
import denis.userservice.entity.User;
import denis.userservice.specification.UserSpecifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public User create(User user) {
        user.setActive(true);
        entityManager.persist(user);
        return user;
    }

    @Override
    public User getById(UUID id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public Page<User> getAll(String name, String surname, int page, int size) {
        Specification<User> spec = Specification.unrestricted();
        if (name != null && !name.isBlank()) {
            spec = spec.and(UserSpecifications.nameLike(name));
        }
        if (surname != null && !surname.isBlank()) {
            spec = spec.and(UserSpecifications.surnameLike(surname));
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        Predicate predicate = spec.toPredicate(root, cq, cb);
        cq.where(predicate);
        TypedQuery<User> query = entityManager.createQuery(cq);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        List<User> users = query.getResultList();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        Predicate countPredicate = spec.toPredicate(countRoot, countQuery, cb);
        countQuery.select(cb.count(countRoot)).where(countPredicate);
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(users, PageRequest.of(page, size), total);
    }

    @Override
    public User update(UUID id, User newData) {
        User existing = getById(id);
        if (existing == null) throw new RuntimeException("User not found");
        existing.setName(newData.getName());
        existing.setSurname(newData.getSurname());
        existing.setEmail(newData.getEmail());
        existing.setBirthDate(newData.getBirthDate());
        return entityManager.merge(existing);
    }

    @Override
    public void activate(UUID id) {
        entityManager.createNativeQuery(
                        "UPDATE users SET active = TRUE WHERE id = ?1")
                .setParameter(1, id)
                .executeUpdate();
    }

    @Override
    public void deactivate(UUID id) {
        entityManager.createNativeQuery(
                        "UPDATE users SET active = FALSE WHERE id = ?1")
                .setParameter(1, id)
                .executeUpdate();
    }

    @Override
    public void delete(UUID id) {
        User user = getById(id);
        if (user != null) entityManager.remove(user);
    }
}
