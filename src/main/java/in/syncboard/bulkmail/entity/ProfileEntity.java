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
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("profiles")
public class ProfileEntity {

    @Id
    @Column("profile_id")
    private Long profileId;

    @Column("name")
    private String name;

    @Column("email")
    private String email;

    @Column("title")
    private String title;

    @Column("company")
    private String company;

    @Column("phone")
    private String phone;

    @Column("linkedin")
    private String linkedin;

    @Column("skills")
    private String skills;

    @Column("resume_url")
    private String resumeUrl;

    @CreatedDate
    @Column("created_date")
    private LocalDate createdDate;

    @LastModifiedDate
    @Column("last_modified_date")
    private LocalDate lastModifiedDate;

    // Generate a unique BIGINT from UUID
    public void generateProfileIdIfNull() {
        if (this.profileId == null) {
            this.profileId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        }
    }
}
