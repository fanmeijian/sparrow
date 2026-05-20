package cn.sparrowmini.owl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@SuperBuilder
public class OwlClassV2Tree extends OwlClassV2 {
    private List<OwlClassV2Tree> children;
}
