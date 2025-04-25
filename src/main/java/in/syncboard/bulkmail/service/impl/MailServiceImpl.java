package in.syncboard.bulkmail.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.syncboard.bulkmail.dto.MailRequestDTO;
import in.syncboard.bulkmail.dto.MailResponseDTO;
import in.syncboard.bulkmail.entity.MailEntity;
import in.syncboard.bulkmail.entity.TemplateEntity;
import in.syncboard.bulkmail.exception.ProfileException;
import in.syncboard.bulkmail.repository.MailRepository;
import in.syncboard.bulkmail.repository.TemplateRepository;
import in.syncboard.bulkmail.service.MailService;
import in.syncboard.bulkmail.utils.EmailUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateRepository templateRepository;
    private final MailRepository mailRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public Mono<MailResponseDTO> sendTemplateEmail(MailRequestDTO mailRequest) {
        if (!EmailUtils.isValidEmail(mailRequest.getRecipient())) {
            return Mono.error(new ProfileException("Invalid email address: " + mailRequest.getRecipient(), HttpStatus.BAD_REQUEST));
        }

        return templateRepository.findById(mailRequest.getTemplateId())
                .switchIfEmpty(Mono.error(new ProfileException("Template not found with ID: " + mailRequest.getTemplateId(), HttpStatus.NOT_FOUND)))
                .flatMap(template -> {
                    if (!Boolean.TRUE.equals(template.getIsActive())) {
                        return Mono.error(new ProfileException("Template is not active: " + template.getName(), HttpStatus.BAD_REQUEST));
                    }

                    return sendEmail(mailRequest, template);
                });
    }

    @Override
    public Flux<MailResponseDTO> sendBulkTemplateEmail(List<String> recipients, Long templateId, Map<String, Object> variables) {
        return Flux.fromIterable(recipients)
                .flatMap(recipient -> {
                    MailRequestDTO request = new MailRequestDTO();
                    request.setRecipient(recipient);
                    request.setTemplateId(templateId);
                    request.setVariables(variables);

                    return sendTemplateEmail(request);
                });
    }

    @Override
    public Flux<MailResponseDTO> getMailHistoryByRecipient(String recipient) {
        return mailRepository.findByRecipient(recipient)
                .map(this::createMailResponseDTO);
    }

    @Override
    public Flux<MailResponseDTO> getMailHistoryByDateRange(LocalDate startDate, LocalDate endDate) {
        return mailRepository.findBySentDateBetween(startDate.toString(), endDate.toString())
                .map(this::createMailResponseDTO);
    }

    @Override
    public Mono<Boolean> validateEmail(String email) {
        if (!EmailUtils.isValidEmail(email)) {
            return Mono.just(false);
        }

        return Mono.fromCallable(() -> {
                    String domain = EmailUtils.extractDomain(email);
                    return domain != null;
                })
                .doOnError(e -> log.error("Error validating email {}: {}", email, e.getMessage()))
                .onErrorReturn(false)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<MailResponseDTO> sendEmail(MailRequestDTO request, TemplateEntity template) {
        // Create a mail entity to track the email
        MailEntity mailEntity = MailEntity.builder()
                .recipient(request.getRecipient())
                .subject(request.getSubject() != null ? request.getSubject() : template.getSubject())
                .templateId(template.getTemplateId())
                .sender(fromEmail)
                .status("SENDING")
                .build();

        // Convert variables to JSON string for storage
        if (request.getVariables() != null) {
            try {
                mailEntity.setVariables(objectMapper.writeValueAsString(request.getVariables()));
            } catch (JsonProcessingException e) {
                log.error("Error serializing variables: {}", e.getMessage());
            }
        }

        // Save initial record to get an ID
        return mailRepository.save(mailEntity)
                .flatMap(savedMail -> {
                    // Send the actual email using JavaMailSender
                    return Mono.fromCallable(() -> {
                                try {
                                    // Prepare the email content
                                    String content = processTemplate(template.getContent(), request.getVariables());
                                    String subject = request.getSubject() != null ? request.getSubject() : template.getSubject();

                                    // Create and send the message
                                    MimeMessage message = mailSender.createMimeMessage();
                                    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                                    helper.setFrom(fromEmail);
                                    helper.setTo(request.getRecipient());
                                    helper.setSubject(subject);
                                    helper.setText(content, true); // true = HTML content

                                    mailSender.send(message);
                                    return true;
                                } catch (MessagingException e) {
                                    log.error("Error sending email: {}", e.getMessage());
                                    throw new RuntimeException("Error sending email: " + e.getMessage(), e);
                                }
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .onErrorResume(e -> {
                                // Update mail record with failure status
                                savedMail.setStatus("FAILED");
                                savedMail.setErrorMessage(e.getMessage());
                                return mailRepository.save(savedMail).then(Mono.just(false));
                            })
                            .flatMap(success -> {
                                // Update the mail record with the final status
                                savedMail.setStatus(success ? "SUCCESS" : "FAILED");
                                return mailRepository.save(savedMail);
                            })
                            .map(updatedMail -> createMailResponseDTO(updatedMail));
                });
    }

    
    private String processTemplate(String templateContent, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return templateContent;
        }

        String processedContent = templateContent;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processedContent = processedContent.replace(placeholder, value);
        }

        return processedContent;
    }

    private MailResponseDTO createMailResponseDTO(MailEntity entity) {
        return MailResponseDTO.builder()
                .mailId(entity.getMailId())
                .recipient(entity.getRecipient())
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .sentDate(entity.getSentDate())
                .errorMessage(entity.getErrorMessage())
                .build();
    }
}
