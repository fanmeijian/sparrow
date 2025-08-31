package cn.sparrowmini.rules.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 规则的头部
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RuleHead implements Serializable {
    private String packageName;

    @Column(columnDefinition = "TEXT")
    private String imports;

    public String toDrl(){
        String pkg = String.join("","package ",this.packageName);
        return String.join("\n", pkg,imports);
    }
}
