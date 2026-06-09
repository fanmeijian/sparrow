package cn.sparrowmini.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
public class PropertyVo {
    String name;
    String label;
    String type;
    String valueType;
    boolean isFacet;
    boolean isRequired;
    boolean isVisible;
    List<String> ranges;
    RestrictionDto restriction;
}
