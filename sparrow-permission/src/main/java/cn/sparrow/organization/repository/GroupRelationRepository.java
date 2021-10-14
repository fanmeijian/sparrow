package cn.sparrow.organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cn.sparrow.model.organization.GroupRelation;
import cn.sparrow.model.organization.GroupRelationPK;

@RepositoryRestResource(exported = false)
public interface GroupRelationRepository extends JpaRepository<GroupRelation, GroupRelationPK> {

}