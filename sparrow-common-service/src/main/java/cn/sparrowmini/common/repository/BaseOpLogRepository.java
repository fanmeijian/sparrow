package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.BaseOpLog;
import cn.sparrowmini.common.model.BaseOpLog_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.OffsetDateTime;
import java.util.Date;

@NoRepositoryBean
public interface BaseOpLogRepository<S extends BaseOpLog, ID> extends BaseRepository<S, ID> {

    Page<S> findByCreatedBy(String username, Pageable pageable);

    default <P> Page<P> findByCreatedBy(String username, Pageable pageable, Class<P> projectionClass) {
        return findCreatedBy(username,pageable,null,projectionClass);
    }

    default <P> Page<P> findCreatedBy(String username, Pageable pageable, String filter, Class<P> projectionClass) {
        Specification<S> specification = Specification.where(null);
        if (filter != null) {
            specification = Specification
                    .where(createdBySpecification(username))
                    .and(filterSpecification(filter));
        }

        return findBy(
                specification,
                query -> query.as(projectionClass).page(pageable)
        );
    }

    Page<S> findByModifiedBy(String username, Pageable pageable);

    default <P> Page<P> findModifiedBy(String username, Pageable pageable, Class<P> projectionClass) {
        return findModifiedBy(username,pageable,null,projectionClass);
    }

    default <P> Page<P> findModifiedBy(String username, Pageable pageable, String filter, Class<P> projectionClass) {
        Specification<S> specification = Specification.where(null);
        if (filter != null) {
            specification = Specification
                    .where(modifiedBySpecification(username))
                    .and(filterSpecification(filter));
        }

        return findBy(
                specification,
                query -> query.as(projectionClass).page(pageable)
        );
    }

    Page<S> findByModifiedBy(String username, Pageable pageable, String filter);

    Page<S> findByCreatedDateBetween(OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);

    default <U> Page<U> findCreatedDateBetweenProjection(OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable, Class<U> projectionClass) {
        Specification<S> specification = createdDateBetweenSpecification(startDate, endDate);
        return findBy(
                specification,
                query -> query.as(projectionClass).page(pageable)
        );
    }

    default <U> Page<U> findModifiedDateBetweenProjection(OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable, Class<U> projectionClass) {
        Specification<S> specification = modifiedBetweenSpecification(startDate,endDate);
        return findBy(
                specification,
                query -> query.as(projectionClass).page(pageable)
        );
    }

    Page<S> findByModifiedDateBetween(Date startDate, Date endDate, Pageable pageable);

    default Specification<S> createdBySpecification(String username) {
        return new Specification<S>() {
            @Override
            public Predicate toPredicate(Root<S> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get(BaseOpLog_.CREATED_BY), username);
            }
        };
    }

    default Specification<S> modifiedBySpecification(String username) {
        return new Specification<S>() {
            @Override
            public Predicate toPredicate(Root<S> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get(BaseOpLog_.MODIFIED_BY), username);
            }
        };
    }

    /**
     * >=start <=end
     * @param start
     * @param end
     * @return
     */
    default Specification<S> createdDateBetweenSpecification(OffsetDateTime start, OffsetDateTime end) {
        return new Specification<S>() {
            @Override
            public Predicate toPredicate(Root<S> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(start!=null){
                    predicate = criteriaBuilder.and(predicate,criteriaBuilder.greaterThanOrEqualTo(root.get(BaseOpLog_.CREATED_DATE), start));
                }

                if(end !=null){
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get(BaseOpLog_.CREATED_DATE),end));
                }
                return predicate;
            }
        };
    }


    /**
     * >=start <=end
     * @param start
     * @param end
     * @return
     */
    default Specification<S> modifiedBetweenSpecification(OffsetDateTime start, OffsetDateTime end) {
        return new Specification<S>() {
            @Override
            public Predicate toPredicate(Root<S> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(start!=null){
                    predicate = criteriaBuilder.and(predicate,criteriaBuilder.greaterThanOrEqualTo(root.get(BaseOpLog_.MODIFIED_DATE), start));
                }

                if(end !=null){
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get(BaseOpLog_.MODIFIED_DATE),end));
                }
                return predicate;
            }
        };
    }
}
