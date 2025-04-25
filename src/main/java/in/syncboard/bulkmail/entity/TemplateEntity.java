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
@Table("email_templates")
public class TemplateEntity {

    @Id
    @Column("template_id")
    private Long templateId;

    @Column("name")
    private String name;

    @Column("subject")
    private String subject;

    @Column("content")
    private String content;

    @Column("description")
    private String description;

    @Column("is_active")
    private Boolean isActive;

    @CreatedDate
    @Column("created_date")
    private LocalDate createdDate;

    @LastModifiedDate
    @Column("last_modified_date")
    private LocalDate lastModifiedDate;
}
