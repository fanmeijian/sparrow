package cn.sparrowmini.owl.jpa.service;


import cn.sparrowmini.owl.jpa.model.OwlProperty;
import cn.sparrowmini.owl.jpa.model.OwlPropertyTypeEnum;
import cn.sparrowmini.owl.jpa.model.OwlRelation;
import cn.sparrowmini.owl.jpa.repository.OwlPropertyRepository;
import cn.sparrowmini.owl.jpa.repository.OwlRelationRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.sparrowmini.owl.jpa.model.OwlRelationType.DOMAIN_OF;


@Service
public class Owl2JsonSchema {
    @Resource
    OwlRelationRepository owlRelationRepository;

    @Resource
    OwlPropertyRepository owlPropertyRepository;

    @Autowired
    OwlClassTreeService owlClassTreeService;

    public Map<String, Object> buildSchemaBundle(String classCode) {

        Map<String, Object> result = new HashMap<>();

        result.put("jsonSchema", buildJsonSchema(classCode));
        result.put("uiSchema", buildUiSchema(classCode));

        return result;
    }

    public Map<String, Object> buildUiSchema(String classCode) {

        Map<String, Object> ui = new LinkedHashMap<>();

        List<OwlRelation> relations = owlRelationRepository
                .findByTargetCodeAndRelationType(classCode, DOMAIN_OF);

        for (OwlRelation r : relations) {

            OwlProperty p = owlPropertyRepository.findByCode(r.getSourceCode()).orElse(null);
            if (p == null) continue;

            ui.put(p.getCode(), buildUiField(p));
        }

        return ui;
    }

    private Map<String, Object> buildUiField(OwlProperty p) {

        Map<String, Object> ui = new HashMap<>();

        String range = p.getRange();

        // =========================
        // DATA FIELD UI
        // =========================
        if (p.getType() == OwlPropertyTypeEnum.DATA) {

            if (range != null && range.contains("date")) {
                ui.put("ui:widget", "date");
            } else if (range != null && (range.contains("int") || range.contains("double"))) {
                ui.put("ui:widget", "number");
            } else {
                ui.put("ui:widget", "text");
            }

            return ui;
        }

        // =========================
        // OBJECT FIELD UI
        // =========================
        if (p.getType() == OwlPropertyTypeEnum.OBJECT) {
            String[] ranges = range.split(",");
            ui.put("ui:widget", "treeSelect");
            if (ranges.length == 1) {

                ui.put("ui:options", Map.of(
                        "tree",
                        owlClassTreeService.buildTree(ranges[0])
                ));

            } else {

                List<Object> trees = new ArrayList<>();

                for (String r : ranges) {
                    trees.add(owlClassTreeService.buildTree(r));
                }

                ui.put("ui:options", Map.of("trees", trees));
            }


//            if (range != null && range.contains(",")) {
//
//                // union → multi select
//                ui.put("ui:widget", "multiSelect");
//
//                ui.put("ui:options", Map.of(
//                        "options", Arrays.asList(range.split(",")))
//                );
//
//            } else {
//
//                // 单对象引用
//                ui.put("ui:widget", "select");
//
//                ui.put("ui:options", Map.of(
//                        "entity", range,
//                        "searchable", true,
//                        "multiple", false
//                ));
//            }

            return ui;
        }

        return ui;
    }

    public Map<String, Object> buildJsonSchema(String classCode) {

        Map<String, Object> schema = new HashMap<>();

        schema.put("type", "object");
        schema.put("title", classCode);

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        // 1. 找这个 class 下所有 domain property
        List<OwlRelation> relations = owlRelationRepository
                .findByTargetCodeAndRelationType(classCode, DOMAIN_OF);

        for (OwlRelation r : relations) {

            OwlProperty prop = owlPropertyRepository.findByCode(r.getSourceCode()).get();

            if (prop == null) continue;

            properties.put(prop.getCode(), buildField(prop));
        }

        schema.put("properties", properties);
        schema.put("required", required);

        return schema;
    }

    private Map<String, Object> buildField(OwlProperty p) {

        Map<String, Object> field = new HashMap<>();

        // TYPE
        if (p.getType() == OwlPropertyTypeEnum.DATA) {
            field.putAll(buildDataField(p));
        } else {
            field.putAll(buildObjectField(p));
        }

        return field;
    }

    private Map<String, Object> buildDataField(OwlProperty p) {

        Map<String, Object> field = new HashMap<>();

        String range = p.getRange();

        if (range == null) {
            field.put("type", "string");
            return field;
        }

        if (range.contains("date")) {
            field.put("type", "string");
            field.put("format", "date-time");
        } else if (range.contains("int") || range.contains("double")) {
            field.put("type", "number");
        } else {
            field.put("type", "string");
        }

        return field;
    }


    private Map<String, Object> buildObjectField(OwlProperty p) {

        Map<String, Object> field = new HashMap<>();

        String range = p.getRange();
        if (range == null) {
            field.put("type", "string");
            return field;
        }

        Set<String> ranges = Arrays.stream(range.split(",")).filter(f->!isSystemClass(f)).collect(Collectors.toSet());

        // 👉 单值 or 多值
        if (ranges.size() == 1) {
            field.put("type", "string");
        } else {
            field.put("type", "array");
            field.put("items", Map.of("type", "string"));
        }

        return field;
    }

    private boolean isSystemClass(String code) {
        return Set.of(
                "Thing", "Nothing", "Resource",
                "Property", "Class", "Ontology"
        ).contains(code);
    }


}
