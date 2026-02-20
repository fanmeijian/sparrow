package cn.sparrowmini.common.model.pem.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupEntityDto {
    private List<String> usernames;
    private List<String> sysroleIds;
    private List<String> groupIds;
}
