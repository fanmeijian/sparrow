package cn.sparrowmini.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Collection;

/**
 */
@Data
@Builder
public class ConceptTypeDto implements Serializable {
    String doctype;
    Collection<String> topConceptOf;
    Collection<String> broader;
    Collection<String> inScheme;
    Collection<String> memberOf;
    String id;
    String name;
    String label;
    String nameSpace;
}