package cn.sparrowmini.common.dto;

import cn.sparrowmini.common.model.CommonStateEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * DTO for {@link cn.sparrowmini.common.model.BaseTreeV2}
 */
@NoArgsConstructor
@Data
public class BaseTreeV2Dto implements Serializable {
    String stat;
    CommonStateEnum entityStat;
    Boolean enabled;
    Boolean hidden;
    String id;
    Set<ParentTreeDto> parentIds;
    String name;
    String code;
    String catalogId;
    String description;
    List<BaseTreeV2Dto> children;

    public BaseTreeV2Dto(String stat, CommonStateEnum entityStat, Boolean enabled, Boolean hidden, String id, Set<ParentTreeDto> parentIds, String name, String code, String catalogId, String description) {
        this.stat = stat;
        this.entityStat = entityStat;
        this.enabled = enabled;
        this.hidden = hidden;
        this.id = id;
        this.parentIds = parentIds;
        this.name = name;
        this.code = code;
        this.catalogId = catalogId;
        this.description = description;
    }

    /**
     * DTO for {@link cn.sparrowmini.common.model.BaseTreeV2.ParentTree}
     */
    @NoArgsConstructor
    @Data
    public static class ParentTreeDto implements Serializable {
        String parentId;
        BigDecimal seq;

        public ParentTreeDto(String parentId, BigDecimal seq) {
            this.parentId = parentId;
            this.seq = seq;
        }
    }
}