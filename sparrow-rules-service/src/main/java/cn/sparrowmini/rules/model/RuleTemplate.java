package cn.sparrowmini.rules.model;

import java.io.Serializable;
import java.util.List;


import cn.sparrowmini.common.model.BaseUuidEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

/**
 * 规则模版
 */
@Entity
@Data
public class RuleTemplate extends BaseUuidEntity implements Serializable {

	private String code;
	private String name;
	private String remark;
	@Embedded
	private RuleHead head;
	@Column(length = 5000)
	private String template;

	@OneToMany(mappedBy = "template")
	private List<Rule> rules;
}
