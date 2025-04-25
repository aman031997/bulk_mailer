package in.syncboard.bulkmail.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("roles")
public class RoleEntity {

    @Id
    @Column("role_id")
    private Long roleId;

    @Column("name")
    private String name;

    @Column("description")
    private String description;
}
