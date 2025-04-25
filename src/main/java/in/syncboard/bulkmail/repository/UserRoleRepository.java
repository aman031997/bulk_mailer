package in.syncboard.bulkmail.repository;

import in.syncboard.bulkmail.entity.UserRoleEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRoleRepository extends ReactiveCrudRepository<UserRoleEntity, Long> {

    Flux<UserRoleEntity> findByUserId(Long userId);

    @Query("SELECT r.name FROM roles r " +
            "JOIN user_roles ur ON r.role_id = ur.role_id " +
            "WHERE ur.user_id = :userId")
    Flux<String> findRoleNamesByUserId(Long userId);

    Mono<Void> deleteByUserIdAndRoleId(Long userId, Long roleId);
}
