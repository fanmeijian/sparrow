package cn.sparrowmini.bpm.server.form;

import cn.sparrowmini.bpm.server.common.BaseOpLog;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table
public class TaskForm extends BaseOpLog implements Serializable {

    @EmbeddedId
    private TaskFormId id;

    @JsonIgnore
    @JoinColumn(name = "formId", insertable = false, updatable = false)
    @ManyToOne
    private FormSchema formSchema;

    public TaskForm() {
    }

    public TaskForm(TaskFormId id) {
        this.id = id;
    }

    public TaskFormId getId() {
        return id;
    }

    public void setId(TaskFormId id) {
        this.id = id;
    }

    public FormSchema getFormSchema() {
        return formSchema;
    }

    public void setFormSchema(FormSchema formSchema) {
        this.formSchema = formSchema;
    }

    @Embeddable
    public static class TaskFormId extends ProcessForm.ProcessFormId implements Serializable{
        private String taskName;

        public TaskFormId() {
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }
    }
}
