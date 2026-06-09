package cn.sparrowmini.owl.solr.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
// 替换为 SolrJ 原生注解
import org.apache.solr.client.solrj.beans.Field;

@JsonInclude(
        content = Include.NON_EMPTY
)
public abstract class BaseMetadataObject implements IMetadataObject {
    protected final static ObjectMapper mapper = new ObjectMapper();

    // SolrJ 使用 @Field("fieldname") 来映射主键和普通字段
    @Getter
    @Setter
    @Field("id")
    protected String uri;


    @Setter
    @Getter
    @Field("code")
    protected String code;

    @Getter
    @Setter
    @Field("localName")
    protected String localName;

    @Getter
    @Setter
    @Field("nameSpace")
    protected String nameSpace;

    @Setter
    @Getter
    @Field("languages")
    protected Collection<String> languages;

    // 在 SolrJ 中，只要属性是 Map 类型，@Field 注解带有通配符（如 *_label）
    // 就会自动将其识别并处理为动态字段（Dynamic Field）
    @Getter
    @Field("*_label")
    protected Map<String, String> label;

    // 1. 核心：必须加上 @Getter 和通配符，SolrJ 才能正确识别并提交
    @Getter
    @Field("*_lowercaseLabel")
    protected Map<String, String> lowercaseLabel;

    @Getter
    @Field("*_alternate")
    protected Map<String, Collection<String>> alternateLabel;

    @Getter
    @Field("*_hidden")
    protected Map<String, Collection<String>> hiddenLabel;

    @Getter
    @Field("*_desc")
    protected Map<String, String> description;

    @Getter
    @Field("*_comment")
    protected Map<String, String> comment;


    public BaseMetadataObject() {
    }

    public static BaseMetadataObject buildNew() {
        SimpleMetadataObject c = new SimpleMetadataObject();
        return c;
    }

    public static BaseMetadataObject buildFrom(IMetadataObject other) {
        SimpleMetadataObject c = new SimpleMetadataObject();
        c.setUri(other.getUri());
        c.setCode(other.getCode());
        c.setLabel(other.getLabel());
        c.setDescription(other.getDescription());
        c.setComment(other.getComment());
        c.setLanguages(other.getLanguages());
        c.setNameSpace(other.getNameSpace());
        c.setLocalName(other.getLocalName());
        return c;
    }

    public void setLabel(Map<String, String> labelMap) {


        if (labelMap != null) {
            if (this.lowercaseLabel == null) {
                this.lowercaseLabel = new HashMap<>();
            }

            for (String key : labelMap.keySet()) {
                this.addLabel(key, (String) labelMap.get(key));
                this.label.put(key + "_label", labelMap.get(key));
                this.label.remove(key);

                // 3. 存入处理后的小写单值（如 Key="zh", Value="itu"）-> 触发生成 zh_lowercaseLabel: "itu"
                String labelWithoutAccents = StringUtils.stripAccents(labelMap.get(key));
                this.lowercaseLabel.put(key + "_lowercaseLabel", labelWithoutAccents.toLowerCase());
            }
        } else {
            this.label = null;
        }
    }


    public void addLabel(String language, String label) {
        if (this.label == null) {
            this.label = new HashMap<>();
        }

        // 让 Map 的 Key 保持为纯语言代码（如 "en", "zh"）
        this.label.put(language, label);
        this.addLanguage(language);
    }
    @SuppressWarnings("unchecked")
    public void addAlternateLabel(String language, String alternate) {
        if (this.alternateLabel == null) {
            this.alternateLabel = new HashMap<>();
        }

        if (!this.alternateLabel.containsKey(language)) {
            this.alternateLabel.put(language, new HashSet<>());
        }

        if (!this.alternateLabel.get(language).contains(alternate)) {
            this.alternateLabel.get(language).add(alternate);
        }

        this.addLanguage(language);
    }

    @SuppressWarnings("unchecked")
    public void addHiddenLabel(String language, String hidden) {
        if (this.hiddenLabel == null) {
            this.hiddenLabel = new HashMap<>();
        }

        if (!this.hiddenLabel.containsKey(language)) {
            this.hiddenLabel.put(language, new HashSet<>());
        }

        if (!this.hiddenLabel.get(language).contains(hidden)) {
            this.hiddenLabel.get(language).add(hidden);
        }

        this.addLanguage(language);
    }

    protected void addLanguage(String language) {
        if (this.languages == null) {
            this.languages = new HashSet<>();
        }

        if (!this.languages.contains(language)) {
            this.languages.add(language);
        }
    }

    public void addComment(String language, String comment) {
        if (this.comment == null) {
            this.comment = new HashMap<>();
        }

        this.comment.put(language, comment);
        this.addLanguage(language);
    }

    public void setComment(Map<String, String> commentMap) {
        if (commentMap != null) {
            for (String key : commentMap.keySet()) {
                this.addComment(key, (String) commentMap.get(key));
            }
        } else {
            this.comment = null;
        }
    }

    public void addDescription(String language, String desc) {
        if (this.description == null) {
            this.description = new HashMap<>();
        }

        this.description.put(language, desc);
        this.addLanguage(language);
    }

    public void setDescription(Map<String, String> descMap) {
        if (descMap != null) {
            for (String key : descMap.keySet()) {
                this.addDescription(key, (String) descMap.get(key));
            }
        } else {
            this.description = null;
        }
    }

    @SuppressWarnings("unchecked")
    public void setAlternateLabel(Map<String, Collection<String>> alternateLabel) {
        if (alternateLabel != null) {
            for (String lang : alternateLabel.keySet()) {
                for (String label : alternateLabel.get(lang)) {
                    this.addAlternateLabel(lang, label);
                }
            }
        } else {
            this.alternateLabel = null;
        }
    }

    @SuppressWarnings("unchecked")
    public void setHiddenLabel(Map<String, Collection<String>> hiddenLabel) {
        if (hiddenLabel != null) {
            for (String lang : hiddenLabel.keySet()) {
                for (String label : hiddenLabel.get(lang)) {
                    this.addHiddenLabel(lang, label);
                }
            }
        } else {
            this.hiddenLabel = null;
        }
    }

    static class SimpleMetadataObject extends BaseMetadataObject {
        SimpleMetadataObject() {
        }
    }
}