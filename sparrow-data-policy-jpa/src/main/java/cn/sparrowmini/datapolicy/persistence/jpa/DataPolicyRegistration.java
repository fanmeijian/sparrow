package cn.sparrowmini.datapolicy.persistence.jpa;

import cn.sparrowmini.data.jpa.query.JpaResource;

import cn.sparrowmini.security.api.PermissionRequest;
import java.util.*;
/** Every declared framework resource has a concrete JPA schema and its required project restrictions. */
public record DataPolicyRegistration(Map<PermissionRequest,JpaResource<?>> resources,Map<PermissionRequest,Set<String>> requiredExtensions) {
    public DataPolicyRegistration {
        resources=Map.copyOf(resources);if(resources.isEmpty())throw new IllegalArgumentException("No framework resources registered");
        var copy=new HashMap<PermissionRequest,Set<String>>();requiredExtensions.forEach((permission,codes)->copy.put(permission,Set.copyOf(codes)));requiredExtensions=Map.copyOf(copy);
        if(!resources.keySet().containsAll(requiredExtensions.keySet()))throw new IllegalArgumentException("Extension for unregistered resource");
    }
}
