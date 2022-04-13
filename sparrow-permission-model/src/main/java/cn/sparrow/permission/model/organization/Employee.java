package cn.sparrow.permission.model.organization;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import cn.sparrow.permission.model.common.AbstractSparrowEntity;
import cn.sparrow.permission.model.group.GroupEmployee;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity
@Table(name = "spr_employee")
@NamedQueries({
		@NamedQuery(name = "Employee.findByUsername", query = "SELECT e FROM Employee e WHERE e.username = :username") })
public class Employee extends AbstractSparrowEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@EqualsAndHashCode.Include
	@Id
	@GenericGenerator(name = "id-generator", strategy = "uuid")
	@GeneratedValue(generator = "id-generator")
	@Audited
	@JsonProperty(access = Access.READ_ONLY)
	private String id;

	@Audited
	private String name;
	@Column(unique = true)
	@Audited
	private String code;
	@Audited
	private Boolean isRoot;
	@Audited
	@Column(name = "organization_id")
	private String organizationId;

	private String username;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
	private Organization organization;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "employee", fetch = FetchType.LAZY)
	private Set<EmployeeOrganizationRole> employeeOrganizationRoles;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "employee", fetch = FetchType.LAZY)
	private Set<EmployeeOrganizationLevel> employeeOrganizationLevels;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "employee", fetch = FetchType.LAZY)
	private Set<GroupEmployee> groupEmployees;

	public Employee(String name, String code, String organizationId) {
		this.name = name;
		this.code = code;
		this.organizationId = organizationId;
	}

	public Employee(String name, String code, String organizationId, String username) {
		this.name = name;
		this.code = code;
		this.organizationId = organizationId;
		this.username = username;
	}

	@PrePersist
	private void preSave() {
		if (isRoot == null) {
			isRoot = true;
		}
	}

}
