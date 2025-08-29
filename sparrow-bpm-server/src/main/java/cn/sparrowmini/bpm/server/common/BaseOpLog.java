package cn.sparrowmini.bpm.server.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class BaseOpLog {
	@Column(name = "created_date", insertable = true, updatable = false,columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@CreationTimestamp
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private OffsetDateTime createdDate; // 创建时间

	@Column(name = "modified_date", insertable = true, updatable = true,columnDefinition = "TIMESTAMP WITH TIME ZONE")
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
		String username= SecurityContextHolder.getContext().getAuthentication()==null ?"Anonymous": SecurityContextHolder.getContext().getAuthentication().getName();
		this.createdBy = username;
		this.modifiedBy = username;
	}

	@PreUpdate
	private void preUpdate(){
        this.modifiedBy= SecurityContextHolder.getContext().getAuthentication()==null ?"Anonymous": SecurityContextHolder.getContext().getAuthentication().getName();
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
