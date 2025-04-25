package in.syncboard.bulkmail.service;

import in.syncboard.bulkmail.dto.MailRequestDTO;
import in.syncboard.bulkmail.dto.MailResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MailService {

    /**
     * Send an email using a template from the database
     *
     * @param mailRequest Email request containing recipient, templateId and variables
     * @return Mono<MailResponseDTO> with the result of the operation
     */
    Mono<MailResponseDTO> sendTemplateEmail(MailRequestDTO mailRequest);

    /**
     * Send bulk emails using a template
     *
     * @param recipients List of recipients
     * @param templateId Template to use
     * @param variables Variables for the template (will be used for all recipients)
     * @return Flux<MailResponseDTO> with the result for each recipient
     */
    Flux<MailResponseDTO> sendBulkTemplateEmail(List<String> recipients, Long templateId, Map<String, Object> variables);

    /**
     * Get mail history for a recipient
     *
     * @param recipient Email address of recipient
     * @return Flux<MailResponseDTO> with mail history
     */
    Flux<MailResponseDTO> getMailHistoryByRecipient(String recipient);

    /**
     * Get mail history for a date range
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Flux<MailResponseDTO> with mail history
     */
    Flux<MailResponseDTO> getMailHistoryByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Validate email format and domain
     *
     * @param email Email to validate
     * @return Mono<Boolean> true if valid
     */
    Mono<Boolean> validateEmail(String email);
}
