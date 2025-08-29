package cn.sparrowmini.bpm.server.form;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * Projection for {@link FormSchema}
 */
public interface FormSchemaInfo extends FormSchemaView{
    List<Object> getSchema();
    Set<ProcessFormInfo> getProcessForms();
    Set<TaskFormInfo> getTaskForms();
}