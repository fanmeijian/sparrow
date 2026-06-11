package cn.sparrowmini.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.util.Collection;

@Builder
@Data
public class RestrictionDto implements Serializable {
    String doctype;
    String id;
    String onProperty;
    String onClass;
    String valueProperty;
    Collection<String> value;
    Boolean isRequired;
}