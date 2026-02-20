package cn.sparrowmini.common.model.pem.group;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@NoArgsConstructor
@Entity
@DiscriminatorValue(GroupRelation.DISCRIMINATOR)
public class GroupRelation extends GroupEntity implements Serializable {
    public static final String DISCRIMINATOR = "GROUP";

    public GroupRelation(String groupId, String groupRelationId) {
        super(groupId, groupRelationId);
    }
}
