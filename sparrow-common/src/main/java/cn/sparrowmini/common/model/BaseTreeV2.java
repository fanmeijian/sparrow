package cn.sparrowmini.common.model;

import cn.sparrowmini.common.listener.BaseTreeV2Listener;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(BaseTreeV2Listener.class)
public class BaseTreeV2 extends BaseUuidEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    protected Set<ParentTree> parentIds=new HashSet<>();;
    protected String name;
    @Column(unique = true)
    protected String code;
    protected String catalogId;

    /**
     * 当勾选这个选项, 则默认包含所有的child,包括以后新增的
     */
    protected Boolean includeAllChildren = false;
    protected String description;


    @Transient
    protected long childCount;

    @Transient
    protected long level;

    @JsonProperty("expandable")
    public boolean isExpandable() {
        return childCount>0;
    }

    @JsonProperty("expanded")
    public boolean isExpanded() {
        return childCount>0;
    }

    @Transient
    protected List<Object> children = new ArrayList<>();

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentTree implements Serializable {
        private String parentId;
        @Column(precision = 20, scale = 8)
        private BigDecimal seq;
    }

    public BigDecimal getSeq(String parentId) {
        return parentIds.stream().filter(p -> p.getParentId().equals(parentId)).findFirst().get().getSeq();
    }

    public void setSeq(String parentId, BigDecimal seq) {
        parentIds.stream().filter(p -> p.getParentId().equals(parentId)).findFirst().get().setSeq(seq);
    }

}
