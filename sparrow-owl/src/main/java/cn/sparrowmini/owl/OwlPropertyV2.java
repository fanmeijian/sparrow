package cn.sparrowmini.owl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Setter
@Getter
@SuperBuilder
public class OwlPropertyV2 {
    private String name;
    private String label;
    private String uiType;
    private OwlPropertyTypeEnum type;
    private List<?> ranges;
    private List<?> domains;
    /**
     * 是否是当前类定义的属性,否则是推理出来的属性
     */
    private boolean direct;
}
