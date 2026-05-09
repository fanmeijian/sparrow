package cn.sparrowmini.common.model.dynamic;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class ProviderDataValue implements Serializable {
    private String label;
    private Object value;
    private long childCount;
    private List<ProviderDataValue> children;
    public ProviderDataValue(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public ProviderDataValue(String label, Object value, long childCount) {
        this.label = label;
        this.value = value;
        this.childCount = childCount;
    }
}
