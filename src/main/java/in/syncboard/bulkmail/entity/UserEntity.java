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

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {

    @Id
    @Column("user_id")
    private Long userId;

    @Column("username")
    private String username;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("phone")
    private String phone;

    @Column("power_level")
    private Integer powerLevel;

    @Column("account_non_expired")
    private Boolean accountNonExpired;

    @Column("account_non_locked")
    private Boolean accountNonLocked;

    @Column("credentials_non_expired")
    private Boolean credentialsNonExpired;

    @Column("enabled")
    private Boolean enabled;

    @Column("verification_code")
    private String verificationCode;

    @Column("verification_code_expiry")
    private LocalDateTime verificationCodeExpiry;

    @Column("last_login")
    private LocalDateTime lastLogin;

    @CreatedDate
    @Column("created_ts")
    private LocalDateTime createdTs;

    @LastModifiedDate
    @Column("updated_ts")
    private LocalDateTime updatedTs;
}