package cn.sparrowmini.common.listener;

import cn.sparrowmini.common.model.BaseTree;
import cn.sparrowmini.common.util.EntityManagerProvider;
import jakarta.persistence.*;

import java.math.BigDecimal;

public class BaseTreeListener {

    @PrePersist
    public void prePersist(BaseTree entity) {
        EntityManagerFactory emf = EntityManagerProvider.entityManagerFactory;
        EntityManager entityManager = emf.createEntityManager();
        try {
            if (entity.getSeq() == null) {
                String jpql = "SELECT MAX(e.seq) FROM " + entity.getClass().getSimpleName() + " e WHERE e.parentId = :parentId";
                if (entity.getParentId() == null) {
                    jpql = "SELECT MAX(e.seq) FROM " + entity.getClass().getSimpleName() + " e WHERE e.parentId is null";
                }
                TypedQuery<BigDecimal> query = entityManager.createQuery(jpql, BigDecimal.class);
                if (entity.getParentId() != null) {
                    query.setParameter("parentId", entity.getParentId());
                }
                BigDecimal maxSeq = query.getSingleResult();
                entity.setSeq((maxSeq != null ? maxSeq.add(BigDecimal.ONE) : BigDecimal.ONE));
            }
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }

    }
}