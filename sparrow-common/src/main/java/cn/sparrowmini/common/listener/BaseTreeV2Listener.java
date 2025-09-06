package cn.sparrowmini.common.listener;

import cn.sparrowmini.common.model.BaseTreeV2;
import cn.sparrowmini.common.util.EntityManagerProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PrePersist;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;

public class BaseTreeV2Listener {

    @PrePersist
    public void prePersist(BaseTreeV2 entity) {
        EntityManagerFactory emf = EntityManagerProvider.entityManagerFactory;
        EntityManager entityManager = emf.createEntityManager();
        try {
            for (BaseTreeV2.ParentTree parentTree : entity.getParentIds()) {
                if (parentTree.getSeq() == null) {
                    String jpql = "SELECT MAX(p.seq) FROM " + entity.getClass().getSimpleName() + " e join e.parentIds p WHERE p.parentId = :parentId";
                    if (parentTree.getParentId() == null) {
                        jpql = "SELECT MAX(p.seq) FROM " + entity.getClass().getSimpleName() + " e join e.parentIds p WHERE p.parentId is null";
                    }
                    TypedQuery<BigDecimal> query = entityManager.createQuery(jpql, BigDecimal.class);
                    if (parentTree.getParentId() != null) {
                        query.setParameter("parentId", parentTree.getParentId());
                    }
                    BigDecimal maxSeq = query.getSingleResult();
                    parentTree.setSeq((maxSeq != null ? maxSeq.add(BigDecimal.ONE) : BigDecimal.ONE));
                }
            }

        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }

    }
}