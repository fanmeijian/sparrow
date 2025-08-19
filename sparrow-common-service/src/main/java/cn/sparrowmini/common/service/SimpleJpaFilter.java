package cn.sparrowmini.common.service;

import cn.sparrowmini.common.bean.SparrowJpaFilter;
import cn.sparrowmini.common.constant.LevelTypeEnum;
import lombok.Data;

@Data
public class SimpleJpaFilter {
    private String name;
    private String operator; // 支持 "=", "like", ">", "<" 等
    private Object value;
    private LevelTypeEnum type;
    private String filterType;
}
