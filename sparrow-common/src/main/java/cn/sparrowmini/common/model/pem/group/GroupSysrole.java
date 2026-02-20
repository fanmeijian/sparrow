package cn.sparrowmini.common.model.pem.group;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

import static cn.sparrowmini.common.model.pem.group.GroupSysrole.DISCRIMINATOR;

@NoArgsConstructor
@Entity
@DiscriminatorValue(DISCRIMINATOR)
public class GroupSysrole extends GroupEntity implements Serializable {
    public static final String DISCRIMINATOR = "SYSROLE";

    public GroupSysrole(String groupId, String sysroleId) {
        super(groupId, sysroleId);
    }
}
