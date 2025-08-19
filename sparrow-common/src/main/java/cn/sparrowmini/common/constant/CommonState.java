package cn.sparrowmini.common.constant;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;

/**
 * 用于控制通用的单据状态，非业务状态
 */

@Embeddable
public class CommonState implements Serializable {
	private static final long serialVersionUID = 1L;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private CommonStateEnum state = CommonStateEnum.Draft;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Boolean enabled = true;

	public CommonStateEnum getState() {
		return state;
	}

	public void setState(CommonStateEnum state) {
		this.state = state;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
