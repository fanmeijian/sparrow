package cn.sparrowmini.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Builder
@Data
public class ItemVo {
    String name;
    String label;
    Long childCount;
    List<ItemVo> children;
}
