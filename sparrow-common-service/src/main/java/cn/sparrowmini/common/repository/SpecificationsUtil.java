package cn.sparrowmini.common.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

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
