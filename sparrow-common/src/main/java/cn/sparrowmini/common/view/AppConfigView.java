package cn.sparrowmini.common.view;

import cn.sparrowmini.common.model.CommonStateEnum;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Projection for {@link cn.sparrowmini.common.model.AppConfig}
 */
public interface AppConfigView extends BaseEntityView {


    String getCode();

    String getName();

    String getDescription();
}