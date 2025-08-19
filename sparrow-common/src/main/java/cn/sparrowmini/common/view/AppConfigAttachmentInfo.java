package cn.sparrowmini.common.view;

import java.io.Serializable;

/**
 * Projection for {@link cn.sparrowmini.common.model.AppConfigAttachment}
 */
public interface AppConfigAttachmentInfo extends BaseLogView {
    String getId();

    String getAppConfigId();

    String getFileName();

    long getSize();
}