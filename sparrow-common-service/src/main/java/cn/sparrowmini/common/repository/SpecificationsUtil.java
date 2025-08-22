package cn.sparrowmini.common.repository;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class SpecificationsUtil {

    public static <T, V extends Comparable<? super V>> Specification<T> dateBetween(String dateFieldName, V startDate, V endDate) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get(dateFieldName), startDate));
            }
            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get(dateFieldName), endDate));
            }
            return predicate;
        };
    }

}
