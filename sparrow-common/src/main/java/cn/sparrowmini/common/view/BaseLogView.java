package cn.sparrowmini.common.view;

import java.time.OffsetDateTime;

/**
 * 用于自定义entity graph
 */
public interface BaseLogView {
    OffsetDateTime getCreatedDate();
    OffsetDateTime getModifiedDate();
    String getCreatedBy();
    String getModifiedBy();
}
