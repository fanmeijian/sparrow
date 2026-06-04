package cn.sparrowmini.owl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class SkosRestriction extends Restriction implements Serializable {

    private String broader;

    private String scheme;

//    public SkosRestriction(String onProperty, String onClass, Boolean isRequired, String broader) {
//        super(onProperty, onClass, isRequired);
//        this.broader = broader;
//    }
//
//    public SkosRestriction(String onProperty, String onClass, Boolean isRequired, String broader, String scheme) {
//        super(onProperty, onClass, isRequired);
//        this.broader = broader;
//        this.scheme = scheme;
//    }
}
