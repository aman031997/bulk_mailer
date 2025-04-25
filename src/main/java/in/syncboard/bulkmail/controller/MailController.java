package in.syncboard.bulkmail.controller;

import in.syncboard.bulkmail.dto.APIResponse;
import in.syncboard.bulkmail.dto.MailRequestDTO;
import in.syncboard.bulkmail.dto.MailResponseDTO;
import in.syncboard.bulkmail.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
@Tag(name = "Email Service API", description = "APIs for sending and managing emails")
public class MailController {

    private final MailService mailService;

    @PostMapping("/send")
    @Operation(summary = "Send email", description = "Sends an email using a template")
    public Mono<ResponseEntity<APIResponse<MailResponseDTO>>> sendEmail(@Valid @RequestBody MailRequestDTO mailRequest) {
        return mailService.sendTemplateEmail(mailRequest)
                .map(response -> ResponseEntity.ok(
                        APIResponse.<MailResponseDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Email sent successfully")
                                .data(response)
                                .build()
                ));
    }

    @PostMapping("/send/bulk")
    @Operation(summary = "Send bulk emails", description = "Sends emails to multiple recipients using the same template")
    public Mono<ResponseEntity<APIResponse<List<MailResponseDTO>>>> sendBulkEmail(
            @RequestParam List<String> recipients,
            @RequestParam Long templateId,
            @RequestBody(required = false) Map<String, Object> variables) {

        return mailService.sendBulkTemplateEmail(recipients, templateId, variables)
                .collectList()
                .map(responses -> ResponseEntity.ok(
                        APIResponse.<List<MailResponseDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Bulk emails sent")
                                .data(responses)
                                .build()
                ));
    }

    @GetMapping("/history/recipient/{recipient}")
    @Operation(summary = "Get mail history by recipient", description = "Retrieves the email history for a specific recipient")
    public Mono<ResponseEntity<APIResponse<List<MailResponseDTO>>>> getMailHistoryByRecipient(
            @PathVariable String recipient) {

        return mailService.getMailHistoryByRecipient(recipient)
                .collectList()
                .map(history -> ResponseEntity.ok(
                        APIResponse.<List<MailResponseDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Mail history retrieved successfully")
                                .data(history)
                                .build()
                ));
    }

    @GetMapping("/history/date-range")
    @Operation(summary = "Get mail history by date range", description = "Retrieves email history within a date range")
    public Mono<ResponseEntity<APIResponse<List<MailResponseDTO>>>> getMailHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return mailService.getMailHistoryByDateRange(startDate, endDate)
                .collectList()
                .map(history -> ResponseEntity.ok(
                        APIResponse.<List<MailResponseDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Mail history retrieved successfully")
                                .data(history)
                                .build()
                ));
    }

    @GetMapping("/validate/{email}")
    @Operation(summary = "Validate email", description = "Validates an email address format and domain")
    public Mono<ResponseEntity<APIResponse<Boolean>>> validateEmail(@PathVariable String email) {
        return mailService.validateEmail(email)
                .map(isValid -> ResponseEntity.ok(
                        APIResponse.<Boolean>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Email validation completed")
                                .data(isValid)
                                .build()
                ));
    }
}
