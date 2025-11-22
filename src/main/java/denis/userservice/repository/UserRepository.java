package denis.userservice.repository;


import denis.userservice.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @Modifying
    @Query("UPDATE User u SET u.active = true WHERE u.id = :userId")
    void activate(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.id = :userId")
    void deactivate(@Param("userId") UUID userId);
}
