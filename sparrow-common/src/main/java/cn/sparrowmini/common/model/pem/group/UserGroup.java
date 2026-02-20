package cn.sparrowmini.common.model.pem.group;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import static cn.sparrowmini.common.model.pem.group.UserGroup.DISCRIMINATOR;

@NoArgsConstructor
@Entity
@DiscriminatorValue(DISCRIMINATOR)
public class UserGroup extends Group{
    public static final String DISCRIMINATOR = "USER";
}
