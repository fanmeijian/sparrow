package cn.sparrowmini.rules;

import cn.sparrowmini.common.view.BaseEntityView;

/**
 * Projection for {@link cn.sparrowmini.rules.model.Drl}
 */
public interface DrlView extends BaseEntityView {
    String getId();

    String getName();

    String getCode();

    String getRemark();
}