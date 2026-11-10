package cn.sparrowmini.data.jpa.query;

import cn.sparrowmini.core.query.Condition;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
import java.util.function.Supplier;

/** Project-owned authorization must supply a fresh mandatory constraint for each call. */
public final class JpaQueryReader<T> {
    private final EntityManager em;
    private final JpaResource<T> resource;
    private final Supplier<Specification<T>> constraints;
    public JpaQueryReader(EntityManager em, JpaResource<T> resource, Supplier<Specification<T>> constraints) {
        this.em = Objects.requireNonNull(em);
        this.resource = Objects.requireNonNull(resource);
        this.constraints = Objects.requireNonNull(constraints);
        resource.validate(em);
    }
    public Page<Map<String,Object>> read(Condition filter, Pageable page, Set<String> requested) {
        Objects.requireNonNull(filter); Objects.requireNonNull(page); Objects.requireNonNull(requested);
        if (!page.isPaged() || page.getPageSize() > 500 || page.getOffset() > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Bounded pagination required");
        if (requested.isEmpty()) throw new IllegalArgumentException("At least one output field required");
        requested.forEach(resource::requireField);
        Condition.fields(filter).forEach(resource::requireField);
        page.getSort().forEach(o -> resource.requireField(o.getProperty()));
        Specification<T> mandatory = Objects.requireNonNull(constraints.get(), "Mandatory constraint required");
        var cb = em.getCriteriaBuilder();
        var query = cb.createTupleQuery(); var root = query.from(resource.entityType());
        var columns = new TreeSet<>(requested);
        query.multiselect(columns.stream().map(f -> root.get(f).alias(f)).toArray(Selection<?>[]::new));
        query.where(cb.and(JpaPredicateCompiler.specification(resource,filter).toPredicate(root,query,cb),
            Objects.requireNonNull(mandatory.toPredicate(root,query,cb), "Mandatory predicate required")));
        var order = new ArrayList<Order>();
        page.getSort().forEach(o -> order.add(o.isAscending() ? cb.asc(root.get(o.getProperty())) : cb.desc(root.get(o.getProperty()))));
        order.add(cb.asc(root.get(resource.id()))); query.orderBy(order);
        var rows = em.createQuery(query).setFirstResult((int)page.getOffset()).setMaxResults(page.getPageSize()).getResultList();
        List<Map<String,Object>> result = new ArrayList<>();
        for (Tuple tuple : rows) {
            Map<String,Object> values = new LinkedHashMap<>();
            columns.forEach(f -> values.put(f,tuple.get(f)));
            result.add(Collections.unmodifiableMap(values));
        }
        var count = cb.createQuery(Long.class); var countRoot = count.from(resource.entityType());
        count.select(cb.count(countRoot)).where(cb.and(JpaPredicateCompiler.specification(resource,filter).toPredicate(countRoot,count,cb),
            Objects.requireNonNull(mandatory.toPredicate(countRoot,count,cb), "Mandatory predicate required")));
        return new PageImpl<>(result,page,em.createQuery(count).getSingleResult());
    }
}
