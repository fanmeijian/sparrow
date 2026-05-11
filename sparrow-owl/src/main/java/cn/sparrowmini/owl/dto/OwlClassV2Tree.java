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
public class OwlClassV2Tree extends OwlClassV2 {
    private List<OwlClassV2Tree> children;
}
