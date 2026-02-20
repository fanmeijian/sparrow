package cn.sparrowmini.common.model;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.listener.BasOpLogListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@EntityListeners({BasOpLogListener.class})
@MappedSuperclass
public abstract class BaseOpLog {
	@Column(name = "created_date", insertable = true, updatable = false)
	@CreationTimestamp
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private OffsetDateTime createdDate; // 创建时间

	@Column(name = "modified_date", insertable = true, updatable = true)
	@UpdateTimestamp
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private OffsetDateTime modifiedDate; // 最后更新时间

    @Setter
	@Column(name = "created_by", insertable = true, updatable = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String createdBy;

    @Setter
	@Column(name = "modified_by", insertable = true, updatable = true)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String modifiedBy;

	@JsonProperty("isAuthor")
	public boolean isAuthor(){
		return CurrentUser.get()!=null && CurrentUser.get().equals(createdBy);
	}

	public OffsetDateTime getCreatedDate() {
		return createdDate;
	}
	public OffsetDateTime getModifiedDate() {
		return modifiedDate;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}


}
