package cn.sparrowmini.form.model;

import java.io.Serializable;

import javax.persistence.*;

import cn.sparrowmini.common.model.BaseOpLog;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * 表单数据
 */
@Audited
@Entity
@Table(name = "spr_form_data")
public class SparrowFormData extends BaseOpLog implements Serializable{
	private static final long serialVersionUID = 1L;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Id
	@GenericGenerator(name = "id-generator", strategy = "uuid")
	@GeneratedValue(generator = "id-generator")
	private String id;

	@Column(name = "form_id")
	private String formId;

	@NotAudited
	@JoinColumn(name = "form_id",insertable = false, updatable = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@ManyToOne
	@LazyCollection(LazyCollectionOption.FALSE)
	private SparrowForm form;
	
	@Column(columnDefinition = "TEXT")
	private String data;
	
	

	public SparrowFormData() {
		super();
	}

	public SparrowFormData(String formId, String data) {
		this.formId = formId;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	
}
