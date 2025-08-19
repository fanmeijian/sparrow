package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.dto.BaseTreeDto;
import cn.sparrowmini.common.model.BaseTree;
import cn.sparrowmini.common.model.BaseTree_;
import cn.sparrowmini.common.model.BaseUuidEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoRepositoryBean
public interface BaseTreeRepository<S extends BaseTree, ID> extends BaseStateRepository<S, ID> {

    @Query("select max(t.seq) from #{#entityName} t where t.parentId is null")
    BigDecimal getRootMaxSeq();

    @Query("select max(t.seq) from #{#entityName} t where t.parentId=:parentId")
    BigDecimal getMaxSeqByParentId(String parentId);

    @Query("select max(t.seq) from #{#entityName} t where t.parentId=:parentId and t.seq<:seq")
    BigDecimal getPreSeqByParentId(String parentId, BigDecimal seq);

    @Query("select max(t.seq) from #{#entityName} t where t.parentId is null and t.seq<:seq")
    BigDecimal getRootPreSeq(BigDecimal seq);

    @Query("select min(t.seq) from #{#entityName} t where t.parentId is null")
    BigDecimal getRootFirstSeq();

    @Query("select min(t.seq) from #{#entityName} t where t.parentId=:parentId")
    BigDecimal getFirstSeqByParentId(String parentId);


    default Page<S> getChildren(ID parentId, Pageable pageable) {
        Page<S> children = this.findByParentId(parentId, pageable.getPageSize() >= 2000 ? Pageable.unpaged(Sort.by(BaseTree_.seq.getName())) : pageable);
        children.forEach(child -> {
            long count = countByParentId((ID) child.getId());
            child.setChildCount(count);
        });
        return children;
    }

    Page<S> findByParentId(ID parentId, Pageable pageable);

    default <P extends BaseTreeDto> Page<P> findByParentIdProjection(ID parentId, Pageable pageable_, Class<P> projectionClass) {
        Pageable pageable = pageable_ == null || pageable_.getPageSize() >= 2000
                ? Pageable.unpaged(Sort.by(BaseTree_.SEQ))
                : pageable_;
        Page<P> children = findBy(
                parentIdSpecification(parentId),
                query -> query.as(projectionClass).page(pageable));
        children.forEach(child -> {
            long count = countByParentId((ID) child.getId());
            child.setChildCount(count);
        });
        return children;
    }

    long countByParentId(ID parentId);

    /***
     * 移动当前节点重新排序
     * @param currentId
     * @param nextId
     */
    @Transactional
    default void move(ID currentId, ID nextId) {
        BigDecimal step = BigDecimal.valueOf(0.0001);
        BigDecimal two = BigDecimal.valueOf(2);
        S current = this.getReferenceById(currentId);
        S next = nextId == null ? null : this.getReferenceById(nextId);
        BigDecimal newSeq = null;

        if (next != null) {
            //目标节点的seq
            final BigDecimal nextSeq = next.getSeq() == null ? BigDecimal.ONE : next.getSeq();
            if (current.getParentId() != null && next.getParentId() != null && current.getParentId().equals(next.getParentId())) {
                //目标节点的前一个节点的排序
                BigDecimal preSeq = getPreSeqByParentId(current.getParentId(), next.getSeq());
                if (preSeq == null) {
                    // move to first node
                    newSeq = nextSeq.subtract(step);
                } else {
                    // insert to middle
                    BigDecimal distance = nextSeq.subtract(preSeq);
                    if (distance.compareTo(BigDecimal.ZERO) > 0) {
                        newSeq = nextSeq.subtract(distance.divide(two));
                    } else {
                        newSeq = nextSeq.subtract(step);
                    }

                }


            } else if (current.getParentId() == null && next.getParentId() == null) {
                BigDecimal preSeq = getRootPreSeq(nextSeq);
                if (preSeq == null) {
                    // move to root first node
                    newSeq = nextSeq.subtract(step);
                } else {
                    // insert to middle
                    BigDecimal distance = nextSeq.subtract(preSeq);
                    if (distance.compareTo(BigDecimal.ZERO) > 0) {
                        newSeq = nextSeq.subtract(distance.divide(two));
                    } else {
                        newSeq = nextSeq.subtract(step);
                    }
                }


            } else {
                throw new RuntimeException(String.format("不能插入到不同层级的前后节点 前 %s 后 %s", "无", next.getName() + nextId));
            }
        }

        if (next == null) {
            // move the last
            if (current.getParentId() == null) {
                newSeq = getRootMaxSeq().add(step);
            } else {
                newSeq = getMaxSeqByParentId(current.getParentId()).add(step);
            }

        }
        current.setSeq(newSeq);
        this.save(current);
    }

    void deleteByParentId(ID parentId);

    @Transactional
    default void deleteCascade(Collection<ID> ids) {
        deleteAllById(ids);
        ids.forEach(id -> {
            if (countByParentId(id) > 0) {
                deleteCascade((Collection<ID>) getChildren(id, Pageable.unpaged()).getContent().stream()
                        .filter(f -> f.getChildCount() > 0)
                        .map(BaseUuidEntity::getId)
                        .toList());
                deleteByParentId(id);
            }
        });

    }

    default Page<S> getAllChildren(ID parentId, Pageable pageable_) {
        Pageable pageable = pageable_ == null || pageable_.isUnpaged() || pageable_.getPageSize() >= 2000 ? Pageable.unpaged(Sort.by(BaseTree_.SEQ)) : pageable_;
        Page<S> rootPage = findByParentId(parentId, pageable);
        List<S> root = rootPage.getContent();
        root.forEach(r -> {
            if (countByParentId((ID) r.getId()) > 0) {
                List<?> children = getAllChildren((ID) r.getId(), pageable).getContent();
                r.getChildren().addAll(children);
                r.setChildCount(children.size());

            }
        });
        return new PageImpl<>(root, pageable, rootPage.getTotalElements());
    }

    S getReferenceByCode(String code);

    boolean existsByCode(String code);

    default Specification<S> parentIdSpecification(ID parentId) {
        return new Specification<S>() {
            @Override
            public Predicate toPredicate(Root<S> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return parentId == null
                        ? criteriaBuilder.and(criteriaBuilder.isNull(root.get(BaseTree_.PARENT_ID)))
                        : criteriaBuilder.and(criteriaBuilder.equal(root.get(BaseTree_.PARENT_ID), parentId));
            }
        };
    }
}
