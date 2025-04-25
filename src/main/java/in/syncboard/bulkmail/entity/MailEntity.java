package in.syncboard.bulkmail.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("sent_emails")
public class MailEntity {

    @Id
    @Column("mail_id")
    private Long mailId;

    @Column("recipient")
    private String recipient;

    @Column("subject")
    private String subject;

    @Column("template_id")
    private Long templateId;

    @Column("sender")
    private String sender;

    @Column("status")
    private String status; // SUCCESS, FAILED

    @Column("error_message")
    private String errorMessage;

    @Column("variables")
    private String variables; // JSON string of variables used

    @CreatedDate
    @Column("sent_date")
    private LocalDate sentDate;

    @LastModifiedDate
    @Column("last_modified_date")
    private LocalDate lastModifiedDate;
}
