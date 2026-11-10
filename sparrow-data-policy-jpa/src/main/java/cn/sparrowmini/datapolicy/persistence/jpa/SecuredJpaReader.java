package cn.sparrowmini.datapolicy.persistence.jpa;

import cn.sparrowmini.data.jpa.query.JpaPredicateCompiler;

import cn.sparrowmini.data.jpa.query.JpaResource;

import cn.sparrowmini.core.query.Condition;

import cn.sparrowmini.datapolicy.core.*;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import java.util.*;

/** Returns sanitized value maps, never managed entities. Host supplies a freshly evaluated plan. */
public final class SecuredJpaReader<T> {
    private final EntityManager em;
    private final JpaResource<T> resource;
    private final org.springframework.data.jpa.domain.Specification<T> mandatory;
    public SecuredJpaReader(EntityManager em, JpaResource<T> resource) {this(em,resource,(root,query,cb)->cb.conjunction());}
    public SecuredJpaReader(EntityManager em, JpaResource<T> resource,org.springframework.data.jpa.domain.Specification<T> mandatory) {
        this.em = Objects.requireNonNull(em); this.resource = Objects.requireNonNull(resource); resource.validate(em);this.mandatory=Objects.requireNonNull(mandatory);
    }
    public Page<Map<String, Object>> read(DataAccessPlan plan, Condition filter, Pageable pageable, Set<String> requested) {
        if (!resource.code().equals(plan.permission().resource())) throw new SecurityException("Resource mismatch");
        if (!pageable.isPaged() || pageable.getPageSize() > 500 || pageable.getOffset() > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Bounded pagination required");
        requested.forEach(resource::requireField);
        Condition.fields(filter).forEach(f -> requireQueryable(plan, f));
        pageable.getSort().forEach(o -> requireQueryable(plan, o.getProperty()));
        var cb = em.getCriteriaBuilder(); var query = cb.createTupleQuery(); var root = query.from(resource.entityType());
        var columns = new TreeSet<>(requested); columns.addAll(Condition.fields(plan.rowCondition()));
        for (String f : requested) columns.addAll(Condition.fields(plan.fieldCondition(f, DataRule.Target.FIELD_READ)));
        // Compile field policies too: unknown properties/types fail even when no rows match.
        for (String f : requested) JpaPredicateCompiler.compile(resource, root, cb, plan.fieldCondition(f, DataRule.Target.FIELD_READ), 0);
        columns.add(resource.id()); columns.forEach(resource::requireField);
        query.multiselect(columns.stream().map(f -> root.get(f).alias(f)).toArray(Selection<?>[]::new));
        Condition combined = new Condition.And(List.of(plan.rowCondition(), filter));
        query.where(cb.and(JpaPredicateCompiler.specification(resource, combined).toPredicate(root, query, cb),required(root,query,cb)));
        var orders = new ArrayList<Order>();
        pageable.getSort().forEach(o -> orders.add(o.isAscending() ? cb.asc(root.get(o.getProperty())) : cb.desc(root.get(o.getProperty()))));
        orders.add(cb.asc(root.get(resource.id()))); query.orderBy(orders);
        var tuples = em.createQuery(query).setFirstResult((int)pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();
        List<Map<String,Object>> output = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Map<String,Object> row = new HashMap<>(); columns.forEach(f -> row.put(f, tuple.get(f)));
            Map<String,Object> visible = new LinkedHashMap<>();
            for (String f : new TreeSet<>(requested)) if (plan.canRead(row, f)) visible.put(f, row.get(f));
            output.add(Collections.unmodifiableMap(visible));
        }
        var count = cb.createQuery(Long.class); var countRoot = count.from(resource.entityType());
        count.select(cb.count(countRoot)).where(cb.and(JpaPredicateCompiler.specification(resource, combined).toPredicate(countRoot, count, cb),required(countRoot,count,cb)));
        return new PageImpl<>(output, pageable, em.createQuery(count).getSingleResult());
    }
    private Predicate required(Root<T> root,CriteriaQuery<?> query,CriteriaBuilder cb){
        return Objects.requireNonNull(mandatory.toPredicate(root,query,cb),"Mandatory predicate must not be null");
    }
    private void requireQueryable(DataAccessPlan plan, String field) {
        resource.requireField(field);
        if (!plan.canQueryField(field)) throw new SecurityException("Field cannot be filtered or sorted");
    }
}
