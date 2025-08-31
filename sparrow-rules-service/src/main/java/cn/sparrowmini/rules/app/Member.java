package cn.sparrowmini.rules.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    private String id;
    private String level;
    private List<MemberGroup> groups;
}