package in.syncboard.bulkmail.repository;

import in.syncboard.bulkmail.entity.TemplateEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TemplateRepository extends ReactiveCrudRepository<TemplateEntity, Long> {

    Mono<TemplateEntity> findByName(String name);

    @Query("SELECT * FROM email_templates WHERE is_active = true")
    Flux<TemplateEntity> findAllActive();
}
