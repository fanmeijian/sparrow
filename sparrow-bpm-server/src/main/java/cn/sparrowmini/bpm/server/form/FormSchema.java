package cn.sparrowmini.bpm.server.form;

import cn.sparrowmini.bpm.server.common.BaseOpLog;
import cn.sparrowmini.bpm.server.common.JsonArrayConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;


@Entity
@Table
public class FormSchema extends BaseOpLog implements Serializable {
    @Id
    @GenericGenerator(name = "id-generator", strategy = "uuid")
    @GeneratedValue(generator = "id-generator")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    private String name;
    private String remark;

    @Lob
    @Column(columnDefinition = "TEXT") // 可选：PostgreSQL/MySQL 可用 columnDefinition="json"
    @Convert(converter = JsonArrayConverter.class)
    private List<Object> schema;

    @OneToMany(mappedBy = "formSchema", fetch = FetchType.EAGER)
    private Set<ProcessForm> processForms;

    @OneToMany(mappedBy = "formSchema", fetch = FetchType.EAGER)
    private Set<TaskForm> taskForms;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<Object> getSchema() {
        return schema;
    }

    public void setSchema(List<Object> schema) {
        this.schema = schema;
    }

    public Set<ProcessForm> getProcessForms() {
        return processForms;
    }

    public void setProcessForms(Set<ProcessForm> processForms) {
        this.processForms = processForms;
    }

    public Set<TaskForm> getTaskForms() {
        return taskForms;
    }

    public void setTaskForms(Set<TaskForm> taskForms) {
        this.taskForms = taskForms;
    }
}
