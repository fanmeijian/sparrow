package cn.sparrowmini.common.model;

import cn.sparrowmini.common.CurrentUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class BaseOpLog {
	@Column(name = "created_date", insertable = true, updatable = false,columnDefinition = "TIMESTAMP")
	@CreationTimestamp
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private OffsetDateTime createdDate; // 创建时间

	@Column(name = "modified_date", insertable = true, updatable = true,columnDefinition = "TIMESTAMP")
	@UpdateTimestamp
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private OffsetDateTime modifiedDate; // 最后更新时间

	@Column(name = "created_by", insertable = true, updatable = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String createdBy;

	@Column(name = "modified_by", insertable = true, updatable = true)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String modifiedBy;

	@PrePersist
	private void preSave(){
		this.createdBy = CurrentUser.get();
		this.modifiedBy = CurrentUser.get();
	}

	@PreUpdate
	private void preUpdate(){
		this.modifiedBy=CurrentUser.get();
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
