package cn.sparrowmini.common.dto;

import lombok.Value;

import java.io.Serializable;
import java.util.Collection;

@Value
public class RestrictionDto implements Serializable {
    String id;
    Boolean isRequired;
    String valueProperty;
    Collection<String> value;
}