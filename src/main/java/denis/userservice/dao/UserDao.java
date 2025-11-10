package denis.userservice.dao;

import denis.userservice.entity.User;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserDao {
    User create(User user);
    User getById(UUID id);
    Page<User> getAll(String name, String surname, int page, int size);
    User update(UUID id, User newData);
    void activate(UUID id);
    void deactivate(UUID id);
    void delete(UUID id);
}