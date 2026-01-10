package cn.sparrowmini.bpm.api.model;

import lombok.Data;

@Data
public class Process {
//    private String id;
    private String name;
    private String description;
    private String code;
    private String categoryId;
}
