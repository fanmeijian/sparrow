package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.dto.BaseTreeDto;
import cn.sparrowmini.common.model.BaseTreeV2;
import cn.sparrowmini.common.model.BaseTreeV2_;
import cn.sparrowmini.common.model.BaseUuidEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支持多父级的树结构
 */
@NoRepositoryBean
public interface BaseTreeV2Repository<S extends BaseTreeV2, ID> extends BaseStateRepository<S, ID> {

    // ===== 修正：必须从 ParentTree 里取 seq，而不是 t.seq =====
    @Query("select max(p.seq) from #{#entityName} t join t.parentIds p where p.parentId is null")
    BigDecimal getRootMaxSeq();

    @Query("select max(p.seq) from #{#entityName} t join t.parentIds p where p.parentId=:parentId")
    BigDecimal getMaxSeqByParentId(ID parentId);

    @Query("select max(p.seq) from #{#entityName} t join t.parentIds p where p.parentId=:parentId and p.seq<:seq")
    BigDecimal getPreSeqByParentId(ID parentId, BigDecimal seq);

    @Query("select max(p.seq) from #{#entityName} t join t.parentIds p where p.parentId is null and p.seq<:seq")
    BigDecimal getRootPreSeq(BigDecimal seq);

    @Query("select min(p.seq) from #{#entityName} t join t.parentIds p where p.parentId is null")
    BigDecimal getRootFirstSeq();

    @Query("select min(p.seq) from #{#entityName} t join t.parentIds p where p.parentId=:parentId")
    BigDecimal getFirstSeqByParentId(ID parentId);

    // ========= 查询子节点 =========
    @Query("select t from #{#entityName} t join t.parentIds p where p.parentId=:parentId")
    Page<S> findByParentId(ID parentId, Pageable pageable);

    @Query("select t from #{#entityName} t join t.parentIds p where p.parentId is null")
    Page<S> findRoot(Pageable pageable);


    @Query("select count(t) from #{#entityName} t join t.parentIds p where p.parentId=:parentId")
    long countByParentId(ID parentId);

    default Page<S> getChildren(ID parentId, Pageable pageable) {
        Pageable pageable_ = pageable != null && pageable.getPageSize() < 2000 ? pageable : Pageable.unpaged();
        Page<S> children = parentId==null? findRoot(pageable_) : this.findByParentId(parentId,pageable_);

        // ====== 批量统计子节点数，避免 N+1 ======
        List<ID> ids = children.stream().map(c -> (ID) c.getId()).toList();
        Map<ID, Long> counts = countByParentIds(ids);
        children.forEach(child -> child.setChildCount(counts.getOrDefault((ID) child.getId(), 0L)));

        return children;
    }

    // 批量统计子节点数
    @Query("select p.parentId, count(t) from #{#entityName} t join t.parentIds p where p.parentId in :ids group by p.parentId")
    List<Object[]> countByParentIdsRaw(Collection<ID> ids);

    default Map<ID, Long> countByParentIds(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return countByParentIdsRaw(ids).stream()
                .collect(Collectors.toMap(r -> (ID) r[0], r -> (Long) r[1]));
    }

    // ========= Projection 查询 =========
    default <P extends BaseTreeDto> Page<P> findByParentIdProjection(ID parentId, Pageable pageable_, Class<P> projectionClass) {
        Pageable pageable = (pageable_ == null || pageable_.getPageSize() >= 2000)
                ? Pageable.unpaged(Sort.by("parentIds.seq"))
                : pageable_;

        Page<P> children = findBy(
                parentIdSpecification(parentId),
                query -> query.as(projectionClass).page(pageable));

        // Projection 必须是 BaseTreeDto 子类，否则不能调用 setChildCount
        if (BaseTreeDto.class.isAssignableFrom(projectionClass)) {
            List<ID> ids = children.stream().map(c -> (ID) ((BaseTreeDto) c).getId()).toList();
            Map<ID, Long> counts = countByParentIds(ids);
            children.forEach(child -> {
                Long cnt = counts.getOrDefault((ID) ((BaseTreeDto) child).getId(), 0L);
                ((BaseTreeDto) child).setChildCount(cnt);
            });
        }

        return children;
    }

    // ========= 移动节点排序 =========
    @Transactional
    default void move(ID currentId, ID nextId, String parentId) {
        BigDecimal step = BigDecimal.valueOf(0.0001);
        BigDecimal two = BigDecimal.valueOf(2);
        S current = this.getReferenceById(currentId);
        S next = nextId == null ? null : this.getReferenceById(nextId);
        BigDecimal newSeq;

        if (next != null) {
            BigDecimal nextSeq = Optional.ofNullable(next.getSeq(parentId)).orElse(BigDecimal.ONE);
            BigDecimal preSeq = (parentId == null)
                    ? getRootPreSeq(nextSeq)
                    : getPreSeqByParentId((ID) parentId, nextSeq);

            if (preSeq == null) {
                newSeq = nextSeq.subtract(step);
            } else {
                BigDecimal distance = nextSeq.subtract(preSeq);
                newSeq = distance.compareTo(BigDecimal.ZERO) > 0
                        ? nextSeq.subtract(distance.divide(two))
                        : nextSeq.subtract(step);
            }
        } else {
            // 插到最后
            newSeq = (parentId == null)
                    ? Optional.ofNullable(getRootMaxSeq()).orElse(BigDecimal.ZERO).add(step)
                    : Optional.ofNullable(getMaxSeqByParentId((ID) parentId)).orElse(BigDecimal.ZERO).add(step);
        }

        current.setSeq(parentId, newSeq);
        this.save(current);
    }

    // ========= 删除 =========

    // ⚠️ 修正：JPQL delete 不支持 join，改成先查再删
    @Transactional
    default void deleteByParentId(ID parentId) {
        List<S> children = findByParentId(parentId, Pageable.unpaged()).getContent();
        this.deleteAll(children);
    }

    @Transactional
    default void deleteCascade(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) return;

        ids.forEach(id -> {
            // 先递归删子节点
            List<S> children = getChildren(id, Pageable.unpaged()).getContent();
            if (!children.isEmpty()) {
                deleteCascade(children.stream().map(m->(ID)m.getId()).toList());
            }
            // 再删当前节点
            this.deleteById(id);
        });
    }

    // ========= 查所有子孙 =========
    default Page<S> getAllChildren(ID parentId, Pageable pageable_) {
        Pageable pageable = (pageable_ == null || pageable_.isUnpaged() || pageable_.getPageSize() >= 2000)
                ? Pageable.unpaged()
                : pageable_;

        Page<S> rootPage = findByParentId(parentId, pageable);
        List<S> root = rootPage.getContent();

        root.forEach(r -> {
            List<S> children = getAllChildren((ID) r.getId(), pageable).getContent();
            r.getChildren().addAll(children);
            r.setChildCount(children.size());
        });

        return new PageImpl<>(root, pageable, rootPage.getTotalElements());
    }

    // ========= 其他 =========
    S getReferenceByCode(String code);
    boolean existsByCode(String code);

    default Specification<S> parentIdSpecification(ID parentId) {
        return (Root<S> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                parentId == null
                        ? cb.isEmpty(root.get(BaseTreeV2_.PARENT_IDS))
                        : cb.equal(root.join(BaseTreeV2_.PARENT_IDS).get("parentId"), parentId);
    }
}
