package cn.sparrowmini.datapolicy.persistence.jpa;

import cn.sparrowmini.security.api.*;
import cn.sparrowmini.datapolicy.core.Condition;
public interface DataPolicyExtension {
    String code();
    Condition restrict(ActorContext actor,PermissionRequest permission);
}
