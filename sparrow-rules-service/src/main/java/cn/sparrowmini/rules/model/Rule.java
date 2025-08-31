package cn.sparrowmini.rules.model;

import java.io.Serializable;

import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = TablePrefix.NAME + "rule")
@Data
public class Rule extends BaseUuidEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "templateId", insertable = false, updatable = false)
	private RuleTemplate template;

	private String templateId;
	private String name;
	private String remark;

	@Column(columnDefinition = "TEXT")
	@Basic(fetch = FetchType.LAZY)
	private String content;

}
