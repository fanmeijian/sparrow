package cn.sparrowmini.common.model.dynamic;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ProviderDataValue implements Serializable {
    private String label;
    private Object value;

    public ProviderDataValue(String label, Object value) {
        this.label = label;
        this.value = value;
    }
}
