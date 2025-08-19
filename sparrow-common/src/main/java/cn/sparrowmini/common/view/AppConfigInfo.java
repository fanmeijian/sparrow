package cn.sparrowmini.common.view;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Projection for {@link cn.sparrowmini.common.model.AppConfig}
 */
public interface AppConfigInfo extends BaseLogView {
    String getCode();

    String getName();

    String getDescription();

    List<Map<String, Object>> getConfigJson();

    List<AppConfigAttachmentInfo> getAttachments();
}