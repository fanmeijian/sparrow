package cn.sparrowmini.bpm.server.form;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Projection for {@link FormSchema}
 */
public interface FormSchemaView {
    OffsetDateTime getCreatedDate();

    OffsetDateTime getModifiedDate();

    String getCreatedBy();

    String getModifiedBy();

    String getId();

    String getName();

    String getRemark();


}