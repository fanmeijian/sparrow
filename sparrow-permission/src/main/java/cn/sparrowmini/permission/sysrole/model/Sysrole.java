package cn.sparrowmini.permission.sysrole.model;

import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "sysrole")
//@ModelPermission
public class Sysrole extends BaseUuidEntity implements Serializable {
	private static final long serialVersionUID = 1L;

//	@AttributePermission
	private String name;

//	@AttributePermission
	@Column(unique = true)
	private String code;
	private Boolean isSystem;

//	@JsonIgnore
//	@OneToMany(fetch = FetchType.LAZY, targetEntity = SysroleMenu.class, cascade = CascadeType.ALL, mappedBy = "sysrole")
//	private Set<SysroleMenu> sysroleMenus;

//	@JsonIgnore
//	@OneToMany(targetEntity = GroupSysrole.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "sysrole")
//	private Set<GroupSysrole> groupSysroles;

//	@JsonIgnore
	@OneToMany(mappedBy = "sysrole", cascade = CascadeType.REMOVE)
	private Set<UserSysrole> userSysroles;

	public Sysrole(String name, String code) {
		super();
		this.name = name;
		this.code = code;
	}
}
