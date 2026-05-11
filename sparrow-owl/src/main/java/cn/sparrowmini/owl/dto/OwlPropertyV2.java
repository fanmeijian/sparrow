package cn.sparrowmini.owl.dto;

import cn.sparrowmini.owl.model.OwlPropertyTypeEnum;
import lombok.Builder;
import lombok.Data;
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
    private OwlPropertyTypeEnum type;
    private List<String> ranges;
}
