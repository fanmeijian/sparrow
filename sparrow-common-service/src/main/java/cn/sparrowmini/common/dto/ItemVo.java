package cn.sparrowmini.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Builder
@Data
public class ItemVo {
    String name;
    String label;
    Long childCount;
}
