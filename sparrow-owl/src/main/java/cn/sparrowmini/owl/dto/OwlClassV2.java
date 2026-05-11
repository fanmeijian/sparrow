package cn.sparrowmini.owl.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Setter
@Getter
@SuperBuilder
public class OwlClassV2 {
    private String name;
    private String label;
    private List<OwlPropertyV2> properties;
}
