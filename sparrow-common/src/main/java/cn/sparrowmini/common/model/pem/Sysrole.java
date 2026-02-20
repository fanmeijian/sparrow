package cn.sparrowmini.common.model.pem;

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
public class Sysrole extends BaseUuidEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;

	@Column(unique = true)
	private String code;
	private Boolean isSystem;

	@OneToMany(mappedBy = "sysrole", cascade = CascadeType.REMOVE)
	private Set<UserSysrole> userSysroles;

	public Sysrole(String name, String code) {
		super();
		this.name = name;
		this.code = code;
	}
}
