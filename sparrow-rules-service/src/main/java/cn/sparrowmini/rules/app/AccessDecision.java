package cn.sparrowmini.rules.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessDecision {
    private String memberId;
    private String articleId;
    private String action; // "VIEW", "DOWNLOAD"
    private boolean allowed;
}