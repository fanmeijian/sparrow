package cn.sparrowmini.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserInfo {
    private String username;
    private Set<String> roles;

    public UserInfo(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
    }
}
