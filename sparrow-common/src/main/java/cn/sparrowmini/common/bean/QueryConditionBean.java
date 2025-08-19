package cn.sparrowmini.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryConditionBean implements Serializable {
    private String type;
    private String name;
    private String op;
    private String value;
    private boolean not;
}
