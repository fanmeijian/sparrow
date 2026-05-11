package cn.sparrowmini.owl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class OwlPropertyV2Tree extends OwlPropertyV2 {
    private List<OwlClassV2Tree> children;
}
