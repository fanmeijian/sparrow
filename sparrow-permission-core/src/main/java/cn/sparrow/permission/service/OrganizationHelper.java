package cn.sparrow.permission.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import cn.sparrow.permission.model.organization.OrganizationRelation;

public class OrganizationHelper {
	@PersistenceContext
	EntityManager entityManager;

	public boolean isAbove(String ancestorId, String orgId) {
		List<OrganizationRelation> organizationRelations = entityManager
				.createNamedQuery("findByOrganizationId", OrganizationRelation.class)
				.setParameter("organizationId", orgId).getResultList();
		boolean flag = false;
		for (OrganizationRelation orgRelation : organizationRelations) {
			if (ancestorId.equals(orgRelation.getId().getParentId()))
				return true;
			else {
				flag = flag || isAbove(ancestorId, orgRelation.getId().getParentId());
			}
		}
		return flag;
	}

	public boolean isBelow(String descendantsId, String orgId) {
		List<OrganizationRelation> organizationRelations = entityManager
				.createNamedQuery("findByOrganizationId", OrganizationRelation.class).setParameter("parentId", orgId)
				.getResultList();
		boolean flag = false;
		for (OrganizationRelation orgRelation : organizationRelations) {
			if (descendantsId.equals(orgRelation.getId().getOrganizationId()))
				return true;
			else {
				flag = flag || isBelow(descendantsId, orgRelation.getId().getOrganizationId());
			}
		}
		return flag;
	}
}
