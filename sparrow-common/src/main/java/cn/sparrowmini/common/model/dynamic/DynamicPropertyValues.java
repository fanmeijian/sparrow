//package cn.sparrowmini.common.model.dynamic;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 动态属性的值
// */
//@Getter
//@Setter
//@NoArgsConstructor
//@Embeddable
//public class DynamicPropertyValues<DT,ID> {
//
//    private List<DynamicPropertyValue<?,ID>> values = new ArrayList<>();
//
//    private List<DynamicPropertyValueString<DT,ID>> valueString;
//    private List<DynamicPropertyValueBoolean<DT,ID>> valueBoolean;
//    private List<DynamicPropertyValueNumber<DT,ID>> valueNumber;
//
//    public List<DynamicPropertyValue<?, ID>> getValues() {
//        values.addAll(valueString);
//        values.addAll(valueBoolean);
//        values.addAll(valueNumber);
//        return values;
//    }
//}
