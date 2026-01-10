package cn.sparrowmini.bpm.api.model;

import lombok.Data;

import java.util.Set;

@Data
public class PermissionDto {
    private Set<String> users;
    private Set<String> roles;
}
