package cn.sparrowmini.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class OwlClass {
    private String id;
    private String name;
    private String label;
    private List<String> children;
}
