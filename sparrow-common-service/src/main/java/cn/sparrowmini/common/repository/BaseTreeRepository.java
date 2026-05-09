package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.dto.BaseTreeDto;
import cn.sparrowmini.common.model.BaseTree;
import cn.sparrowmini.common.model.BaseTree_;
import cn.sparrowmini.common.model.BaseUuidEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
        return getChildren(parentId, pageable, null);
    }



    default Page<S> getChildren(ID parentId_, Pageable pageable, String filter) {
        Pageable pageable__= PageRequest.of(0,Integer.MAX_VALUE);
        pageable__.getSort().and(Sort.by(BaseTree_.seq.getName()));
        Pageable pageable_ = pageable.getPageSize() >= 2000 ? pageable__ : pageable;
        ID parentId = parentId_;

        if(parentId_ != null && existsByCode(parentId_.toString())) {
            parentId = (ID)findByCode(parentId_.toString()).get().getId();
        }

        Page<S> children = filter==null? this.findByParentId(parentId,pageable_) : this.findByParentId(parentId,pageable_, filterSpecification(filter));
        children.forEach(child -> {
            long count = countByParentId((ID) child.getId());
            child.setChildCount(count);
        });
        return children;
    }

    Optional<S> findByCode(String code);

    /**
     * 根据代码或ID获取
     * @param parent
     * @return
     */
    default Page<S> findByParent(ID parentId, Pageable pageable){
        ID parentId_ = parentId;
        if(existsByCode(parentId_.toString())) {
            S parent = findByCode(parentId_.toString()).get();
            parentId_ = (ID)parent.getId();
        }
        return findByParentId(parentId_, pageable);
    }

    Page<S> findByParentId(ID parentId, Pageable pageable);

    Page<S> findByParentId(ID parentId, Pageable pageable, Specification<S> spec);

    default <P extends BaseTreeDto> Page<P> findByParentIdProjection(ID parentId, Pageable pageable_, Class<P> projectionClass) {
        Pageable pageable__= PageRequest.of(0,Integer.MAX_VALUE);
        pageable__.getSort().and(Sort.by(BaseTree_.seq.getName()));
        Pageable pageable = pageable_ == null || pageable_.getPageSize() >= 2000
                ? pageable__
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
                deleteCascade((Collection<ID>) getChildren(id, PageRequest.of(0,Integer.MAX_VALUE)).getContent().stream()
                        .filter(f -> f.getChildCount() > 0)
                        .map(BaseUuidEntity::getId)
                        .toList());
                deleteByParentId(id);
            }
        });

    }

    default Page<S> getAllChildren(ID parentId_, Pageable pageable_) {
        Pageable unPage= PageRequest.of(0,Integer.MAX_VALUE).withSort(Sort.by(Sort.Order.asc(BaseTree_.SEQ)));
        Pageable pageable = pageable_ == null || pageable_.isUnpaged() || pageable_.getPageSize() >= 2000 ? unPage : pageable_;
        ID parentId = parentId_;

        if(parentId_ != null && existsByCode(parentId_.toString())) {
            parentId = (ID)findByCode(parentId_.toString()).get().getId();
        }
        Page<S> rootPage = findByParentId(parentId, pageable);
        List<S> root = rootPage.getContent();
        root.forEach(r -> {
            if (countByParentId((ID) r.getId()) > 0) {
                getAllChildren_(r);
//                List<S> children = getAllChildren((ID) r.getId(), pageable).getContent();
//                r.getChildren().addAll(children);
//                r.setChildCount(children.size());

            }
        });
        return new PageImpl<>(root, pageable, rootPage.getTotalElements());
    }

    private void getAllChildren_(S parent){
        ID parentId_ = (ID)parent.getId();
        ID parentId = parentId_;
        Pageable unPage= PageRequest.of(0,Integer.MAX_VALUE).withSort(Sort.by(Sort.Order.asc(BaseTree_.SEQ)));

        if(parentId_ != null && existsByCode(parentId_.toString())) {
            parentId = (ID)findByCode(parentId_.toString()).get().getId();
        }
        Page<S> rootPage = findByParentId(parentId, unPage);
        List<S> root = rootPage.getContent();
        parent.getChildren().addAll(root);
        root.forEach(r -> {
            if (countByParentId((ID) r.getId()) > 0) {
                getAllChildren_(r);
            }
        });
    }


    @Query("select t.code from #{#entityName} t left join #{#entityName} p on p.id=t.parentId where p.code=:parentCode")
    List<String> getChildrenCode(String parentCode);

    default List<String> getAllChildrenCode(String parentCode) {
        List<String> allChildren = new ArrayList<>();

        // 1. 获取当前层级的直接子节点
        List<String> directChildren = getChildrenCode(parentCode);

        if (directChildren != null && !directChildren.isEmpty()) {
            allChildren.addAll(directChildren);

            // 2. 递归获取每个子节点的子节点
            for (String childCode : directChildren) {
                allChildren.addAll(getAllChildrenCode(childCode));
            }
        }
        return allChildren;
    }

    @Query("select t.id from #{#entityName} t where t.parentId=:parentId ")
    List<ID> getChildrenId(ID parentId);

    default List<ID> getAllChildrenId(ID parentId) {
        List<ID> allChildren = new ArrayList<>();

        // 1. 获取当前层级的直接子节点
        List<ID> directChildren = getChildrenId(parentId);

        if (directChildren != null && !directChildren.isEmpty()) {
            allChildren.addAll(directChildren);

            // 2. 递归获取每个子节点的子节点
            for (ID childId : directChildren) {
                allChildren.addAll(getAllChildrenId(childId));
            }
        }
        return allChildren;
    }

    @Query("select t.parentId from #{#entityName} t where t.id=:id ")
    ID getParentId(ID id);

    default List<ID> getAllParentId(ID id) {
        List<ID> allParentId = new ArrayList<>();

        // 1. 获取当前层级的直接子节点
        ID directParentId = getParentId(id);

        if (directParentId != null) {
            allParentId.addAll(List.of(directParentId));
            allParentId.addAll(getAllParentId(directParentId));
        }
        return allParentId;
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
