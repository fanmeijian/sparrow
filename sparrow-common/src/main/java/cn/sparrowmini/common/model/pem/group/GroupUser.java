package cn.sparrowmini.common.model.pem.group;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static cn.sparrowmini.common.model.pem.group.GroupUser.DISCRIMINATOR;

@NoArgsConstructor
@Entity
@DiscriminatorValue(DISCRIMINATOR)
public class GroupUser extends GroupEntity implements Serializable {
    public static final String DISCRIMINATOR = "USER";

    public GroupUser(String groupId, String username) {
        super(groupId, username);
    }
}
