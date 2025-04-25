package in.syncboard.bulkmail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailRequestDTO {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Valid email is required")
    private String recipient;

    private String subject;

    @NotNull(message = "Template ID is required")
    private Long templateId;

    private Map<String, Object> variables;
}
