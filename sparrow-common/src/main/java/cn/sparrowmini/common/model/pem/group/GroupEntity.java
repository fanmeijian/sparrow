package cn.sparrowmini.common.model.pem.group;

import cn.sparrowmini.common.model.BaseState;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 组包含的成员
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "group_entity")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public class GroupEntity extends BaseState {

    @EmbeddedId
    private GroupEntityId id;

    public GroupEntity(GroupEntityId id) {
        this.id = id;
    }

    public GroupEntity(String groupId, String businessId) {
        this(new GroupEntityId(groupId, businessId));
    }

    @Data
    @NoArgsConstructor
    @Embeddable
    public static class GroupEntityId implements Serializable {
        private String groupId;
        private String businessId;
        public GroupEntityId(String groupId, String businessId) {
            this.groupId = groupId;
            this.businessId = businessId;
        }
    }
}
