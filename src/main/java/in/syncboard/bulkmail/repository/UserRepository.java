package in.syncboard.bulkmail.repository;

import in.syncboard.bulkmail.entity.UserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long> {

    /**
     * Find a user by username
     * @param username The username to search for
     * @return A Mono containing the user, or an empty Mono if not found
     */
    Mono<UserEntity> findByUsername(String username);

    /**
     * Find a user by email
     * @param email The email to search for
     * @return A Mono containing the user, or an empty Mono if not found
     */
    Mono<UserEntity> findByEmail(String email);

    /**
     * Find users by role name
     * @param roleName The role name to search for
     * @return A Flux of users with the specified role
     */
    @Query("SELECT u.* FROM users u " +
            "JOIN user_roles ur ON u.user_id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.role_id " +
            "WHERE r.name = :roleName")
    Flux<UserEntity> findByRoleName(String roleName);

    // The following methods are no longer needed since we're using findByUsername/findByEmail
    // with switchIfEmpty pattern instead

    /*
    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username")
    Mono<Boolean> existsByUsername(String username);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email")
    Mono<Boolean> existsByEmail(String email);
    */
}
