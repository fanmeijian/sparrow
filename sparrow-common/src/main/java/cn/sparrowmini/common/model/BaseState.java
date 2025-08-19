package cn.sparrowmini.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@MappedSuperclass
public abstract class BaseState extends BaseOpLog {

    /**
     * 业务状态
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected String stat;

    /**
     * 单据的状态
     */
//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Enumerated(EnumType.STRING)
    protected CommonStateEnum entityStat = CommonStateEnum.Draft;

//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected Boolean enabled = true;

    /**
     * 用于控制本条数据是否隐藏,也就是不显示
     */
//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected Boolean hidden = false;

}
