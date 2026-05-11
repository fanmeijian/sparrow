package cn.sparrowmini.owl.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class OwlPropertyV2Tree extends OwlPropertyV2 {
    private List<OwlClassV2Tree> children;
}
