package in.syncboard.bulkmail.repository;

import in.syncboard.bulkmail.entity.ProfileEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProfileRepository extends ReactiveCrudRepository<ProfileEntity, Long> {

    Mono<ProfileEntity> findByEmail(String email);

    Flux<ProfileEntity> findByNameContainingIgnoreCase(String name);

    Flux<ProfileEntity> findByCompanyContainingIgnoreCase(String company);

    @Query("SELECT * FROM profiles WHERE email LIKE :domain")
    Flux<ProfileEntity> findByEmailDomain(String domain);

    @Query("SELECT * FROM profiles WHERE skills LIKE :skill")
    Flux<ProfileEntity> findBySkill(String skill);
}
