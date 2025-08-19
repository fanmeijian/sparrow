package cn.sparrowmini.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntity extends BaseState {

	@Transient
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected String modelName = this.getClass().getName();

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Column(name = "data_permission_token_id")
	protected String dataPermissionTokenId;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Transient
	protected List<ErrMessage> errMessages = new ArrayList<>();
}
