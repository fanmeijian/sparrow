package cn.sparrowmini.bpm.server.form;

import cn.sparrowmini.bpm.server.common.BaseOpLog;
import cn.sparrowmini.bpm.server.process.ProcessDesign;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table
public class ProcessForm extends BaseOpLog implements Serializable {

    @EmbeddedId
    private ProcessFormId id;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "processId",referencedColumnName = "processId", insertable = false, updatable = false),
            @JoinColumn(name = "processVersion",referencedColumnName = "processVersion", insertable = false, updatable = false),
            @JoinColumn(name = "packageName",referencedColumnName = "packageName", insertable = false, updatable = false)
    })
    private ProcessDesign processDesign;

    @JsonIgnore
    @JoinColumn(name = "formId", insertable = false, updatable = false)
    @ManyToOne
    private FormSchema formSchema;

    public ProcessForm() {
    }

    public ProcessForm(ProcessFormId id) {
        this.id = id;
    }

    public FormSchema getFormSchema() {
        return formSchema;
    }

    public void setFormSchema(FormSchema formSchema) {
        this.formSchema = formSchema;
    }

    public ProcessFormId getId() {
        return id;
    }

    public void setId(ProcessFormId id) {
        this.id = id;
    }

    @MappedSuperclass
    @Embeddable
    public static class ProcessFormId extends ProcessDesign.ProcessDesignId implements Serializable {
        private String formId;


        public ProcessFormId() {
        }

        public String getFormId() {
            return formId;
        }

        public void setFormId(String formId) {
            this.formId = formId;
        }
    }
}
