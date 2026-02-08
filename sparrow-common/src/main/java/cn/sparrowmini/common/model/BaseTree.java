package cn.sparrowmini.common.model;

import cn.sparrowmini.common.JsonUtils;
import cn.sparrowmini.common.listener.BaseTreeListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(BaseTreeListener.class)
public class BaseTree extends BaseUuidEntity {
    protected String parentId;
    protected String name;
    @Column(unique = true)
    protected String code;
    protected String catalogId;

    /**
     * 当勾选这个选项, 则默认包含所有的child,包括以后新增的
     */
    protected Boolean includeAllChildren = false;
    protected String description;

    @Column(precision = 20, scale = 8)
    protected BigDecimal seq;

    @Transient
    protected long childCount;

    @Transient
    protected long level;

    @JsonProperty("expandable")
    @Transient
    protected boolean expandable;

    public boolean isExpandable() {
        return childCount>0;
    }

    @Transient
    protected List<Object> children = new ArrayList<>();

    public BaseTree(BaseTree baseTree, long childCount){
        try {
            JsonUtils.getMapper().updateValue(baseTree,this);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }

        this.childCount = childCount;
        if(this.childCount>0){
            expandable=true;
        }
    }

}
