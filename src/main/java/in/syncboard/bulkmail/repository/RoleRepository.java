package in.syncboard.bulkmail.repository;

import in.syncboard.bulkmail.entity.RoleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<RoleEntity, Long> {

    Mono<RoleEntity> findByName(String name);
}
