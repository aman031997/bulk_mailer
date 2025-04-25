package in.syncboard.bulkmail.repository;

import in.syncboard.bulkmail.entity.MailEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MailRepository extends ReactiveCrudRepository<MailEntity, Long> {

    Flux<MailEntity> findByRecipient(String recipient);

    Flux<MailEntity> findByTemplateId(Long templateId);

    @Query("SELECT * FROM sent_emails WHERE sent_date >= :startDate AND sent_date <= :endDate")
    Flux<MailEntity> findBySentDateBetween(String startDate, String endDate);

    Flux<MailEntity> findByStatus(String status);

    Mono<MailEntity> save(MailEntity mailEntity);
}
