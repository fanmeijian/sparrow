package cn.sparrowmini.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public class BaseMetaData {
    private String id;
    private String name;
    private String label;
    private List<String> children;
    private List<String> allParents;
}
