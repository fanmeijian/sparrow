package cn.sparrowmini.permission.model.relation;

import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "pem_group_user")
@Table(name = TablePrefix.NAME + "pem_group_user")
public class GroupUser implements Serializable {
    public GroupUser(String groupId, String username) {
        this.id = new GroupUserId(groupId, username);
    }

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private GroupUserId id;

    public GroupUser(GroupUserId groupUserId) {
        this.id = groupUserId;
    }


    @Data
    @NoArgsConstructor
    @Embeddable
    public static class GroupUserId implements Serializable {
        private static final long serialVersionUID = 1L;
        private String groupId;
        private String username;

        public GroupUserId(String groupId, String username) {
            this.groupId = groupId;
            this.username = username;
        }

    }
}
