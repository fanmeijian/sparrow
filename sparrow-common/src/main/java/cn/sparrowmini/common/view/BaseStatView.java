package cn.sparrowmini.common.view;

import cn.sparrowmini.common.model.CommonStateEnum;

public interface BaseStatView {
    String getStat();
    CommonStateEnum getEntityStat();
    Boolean getEnabled();
}
