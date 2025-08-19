package cn.sparrowmini.common.model;

import cn.sparrowmini.common.constant.PermissionTypeEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public final class ErrMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;
    @Enumerated(EnumType.STRING)
    private PermissionTypeEnum type;
    private String msg;

    public ErrMessage(String field, PermissionTypeEnum type, String msg) {
        this.field = field;
        this.type = type;
        this.msg = msg;
    }
}
