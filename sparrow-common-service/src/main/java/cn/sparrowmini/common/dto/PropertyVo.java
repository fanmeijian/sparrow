package cn.sparrowmini.common.dto;

import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
public class PropertyVo {
    String name;
    String label;
    String type;
    String valueType;
    boolean isFacet;
    boolean isRequired;
    boolean isVisible;
    String codeListId;
    List<String> ranges;
    String broader;
    String scheme;
    RestrictionDto restriction;
}
