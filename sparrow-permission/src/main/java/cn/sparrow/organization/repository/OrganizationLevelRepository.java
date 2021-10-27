package cn.sparrow.organization.repository;

import java.util.List;

import javax.validation.constraints.NotBlank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cn.sparrow.model.organization.OrganizationLevel;
import cn.sparrow.model.organization.OrganizationLevelPK;

@RepositoryRestResource(exported = false)
public interface OrganizationLevelRepository extends JpaRepository<OrganizationLevel, OrganizationLevelPK> {

	List<OrganizationLevel> findByIdOrganizationId(@NotBlank String organizationId);

}
