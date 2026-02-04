package cn.sparrowmini.common.model.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态属性的值
 */
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class DynamicPropertyValueAll<DT,ID> {
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "businessId", insertable = false, updatable = false)
    private DT businessObject;

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

//    private DynamicPropertyValueString1<DT,ID> valueString;

}
