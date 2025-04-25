package in.syncboard.bulkmail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailResponseDTO {
    private Long mailId;
    private String recipient;
    private String subject;
    private String status;
    private LocalDate sentDate;
    private String errorMessage;
}
