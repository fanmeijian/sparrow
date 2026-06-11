package cn.sparrowmini.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@SuperBuilder
@Data
public class OwlProperty extends BaseMetaData{
    private Collection<String> range;
    private String propType;

}
