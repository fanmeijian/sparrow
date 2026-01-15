package cn.sparrowmini.audit;

import java.io.Serializable;
import java.util.Date;


import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity implementation class for Entity: RequestAuditLog
 *
 */
@Entity
@Table(name = TablePrefix.NAME + "request_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestAuditLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@EqualsAndHashCode.Include
	@Id
	@GenericGenerator(name = "id-generator", strategy = "uuid")
	@GeneratedValue(generator = "id-generator")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String id;

	@Column(name = "created_date", insertable = true, updatable = false)
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Date createdDate; // 创建时间

//	@GeneratorType(type = LoggedUserGenerator.class, when = GenerationTime.INSERT)
	@Column(name = "created_by", insertable = true, updatable = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String createdBy;

	private String origin;

	@Column(length = 255)
	private String method;

	@Lob
	private String args;

	public RequestAuditLog(String createdBy, String origin, String method, String args) {
		this.origin = origin;
		this.method = method;
		this.args = args;
	}

}
