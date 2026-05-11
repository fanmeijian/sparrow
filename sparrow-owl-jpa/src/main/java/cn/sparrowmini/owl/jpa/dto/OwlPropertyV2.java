package cn.sparrowmini.owl.jpa.dto;

import cn.sparrowmini.owl.jpa.model.OwlPropertyTypeEnum;
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
