package cn.sparrowmini.owl.solr.model;


import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CaseFormat;
import org.springframework.util.StringUtils;

public interface DynamicName {
    static String getDynamicFieldPart(String... qualifier) {
        List<String> parts = new ArrayList();

        for(String part : qualifier) {
            parts.add(getDynamicFieldPart(part));
        }

        return getDynamicFieldPart(String.join("_", parts));
    }

    static String getDynamicFieldPart(String part) {
        if (!StringUtils.hasText(part)) {
            return "undefined";
        } else {
            String dynamicFieldPart = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, part);
            dynamicFieldPart = dynamicFieldPart.replaceAll("[^a-zA-Z0-9_ ]", "");
            dynamicFieldPart = dynamicFieldPart.trim().replaceAll(" ", "_").toUpperCase();
            dynamicFieldPart = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, dynamicFieldPart);
            return dynamicFieldPart;
        }
    }
}