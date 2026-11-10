package cn.sparrowmini.datapolicy.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import java.util.*;

/** Explicit public field registry. First version supports scalar properties, including inherited ones. */
public record JpaResource<T>(String code, Class<T> entityType, String id, Set<String> fields) {
    public JpaResource {
        Objects.requireNonNull(code); Objects.requireNonNull(entityType); Objects.requireNonNull(id);
        fields = Set.copyOf(fields);
        if (code.isBlank() || !fields.contains(id)) throw new IllegalArgumentException("Invalid resource registration");
    }
    public void validate(EntityManager em) {
        var type = em.getMetamodel().entity(entityType);
        if (!type.hasSingleIdAttribute() || !type.getId(type.getIdType().getJavaType()).getName().equals(id))
            throw new IllegalArgumentException("Single scalar ID required by this adapter");
        for (String field : fields) {
            var attribute = type.getAttribute(field);
            if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC)
                throw new IllegalArgumentException("Unregistered scalar field: " + field);
        }
    }
    public void requireField(String field) {
        if (!fields.contains(field)) throw new IllegalArgumentException("Unknown field: " + field);
    }
}
