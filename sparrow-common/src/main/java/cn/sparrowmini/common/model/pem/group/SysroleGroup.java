package cn.sparrowmini.common.model.pem.group;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import static cn.sparrowmini.common.model.pem.group.SysroleGroup.DISCRIMINATOR;

@NoArgsConstructor
@Entity
@DiscriminatorValue(DISCRIMINATOR)
public class SysroleGroup extends Group{
    public static final String DISCRIMINATOR = "SYSROLE";
}
