package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.antlr.PredicateBuilder;
import cn.sparrowmini.common.service.SimpleJpaFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SimpleJpaFilterHelper {


    public static List<Predicate> getPredicates(Root<?> root, CriteriaBuilder cb, List<SimpleJpaFilter> filterList){
        // 构建动态 where 条件
        List<Predicate> predicates = new ArrayList<>();
        if (filterList != null && !filterList.isEmpty()) {
            for (SimpleJpaFilter filter : filterList) {
                String field = filter.getName();
                Object value = filter.getValue();
                String op = filter.getOperator().toLowerCase();

                Path<?> path = root.get(field);

                switch (op) {
                    case "=":
                        predicates.add(cb.equal(path, value));
                        break;
                    case "like":
                        predicates.add(cb.like(path.as(String.class), "%" + value + "%"));
                        break;
                    case ">":
                        predicates.add(cb.greaterThan(path.as(Comparable.class), (Comparable) value));
                        break;
                    case "<":
                        predicates.add(cb.lessThan(path.as(Comparable.class), (Comparable) value));
                        break;
                    // 你可以继续添加更多操作符支持
                    default:
                        throw new IllegalArgumentException("Unsupported operator: " + op);
                }
            }
        }
        return predicates;
    }

}
