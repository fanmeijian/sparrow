package cn.sparrowmini.owl.solr.model;

import cn.sparrowmini.owl.solr.model.IConcept;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
// 替换为 SolrJ 原生注解
import org.apache.solr.client.solrj.beans.Field;

@JsonInclude(
        content = Include.NON_EMPTY
)
public abstract class Concept implements IConcept {

    // SolrJ 使用 @Field("fieldname") 来映射主键和普通字段
    @Field("id")
    protected String uri;

    @Field("code")
    protected String code;

    @Field("localName")
    protected String localName;

    @Field("nameSpace")
    protected String nameSpace;

    @Field("languages")
    protected Collection<String> languages;

    // 在 SolrJ 中，只要属性是 Map 类型，@Field 注解带有通配符（如 *_label）
    // 就会自动将其识别并处理为动态字段（Dynamic Field）
    @Field("*_label")
    protected Map<String, String> label;

    @Field("*_lowercaseLabel")
    protected Map<String, String> lowercaseLabel;

    @Field("*_alternate")
    protected Map<String, Collection<String>> alternateLabel;

    @Field("*_hidden")
    protected Map<String, Collection<String>> hiddenLabel;

    @Field("*_desc")
    protected Map<String, String> description;

    @Field("*_comment")
    protected Map<String, String> comment;

    @Field("basePlatform")
    private String basePlatform;

    public Concept() {
    }

    public static Concept buildNew() {
        SimpleConcept c = new SimpleConcept();
        return c;
    }

    public static Concept buildFrom(IConcept other) {
        SimpleConcept c = new SimpleConcept();
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

    public Collection<String> getLanguages() {
        return this.languages;
    }

    public void setLanguages(Collection<String> languages) {
        this.languages = languages;
    }

    public String getBasePlatform() {
        return this.basePlatform;
    }

    public void setBasePlatform(String basePlatform) {
        this.basePlatform = basePlatform;
    }

    public Map<String, String> getLabel() {
        return this.label;
    }

    public void setLabel(Map<String, String> labelMap) {
        if (labelMap != null) {
            for(String key : labelMap.keySet()) {
                this.addLabel(key, (String)labelMap.get(key));
                this.label.put(key+ "_label", labelMap.get(key));
                this.label.remove(key);
            }
        } else {
            this.label = null;
            this.lowercaseLabel = null;
        }
    }

    public void addLabel(String language, String label) {
        if (this.label == null) {
            this.label = new HashMap<>();
        }

        if (this.lowercaseLabel == null) {
            this.lowercaseLabel = new HashMap<>();
        }

        this.label.put(language, label);
        if (label != null) {
            String labelWithoutAccents = StringUtils.stripAccents(label);
            this.lowercaseLabel.put(language + "_label", labelWithoutAccents.toLowerCase());
        }

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

    public Map<String, String> getComment() {
        return this.comment;
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
            for(String key : commentMap.keySet()) {
                this.addComment(key, (String)commentMap.get(key));
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

    public Map<String, String> getDescription() {
        return this.description;
    }

    public void setDescription(Map<String, String> descMap) {
        if (descMap != null) {
            for(String key : descMap.keySet()) {
                this.addDescription(key, (String)descMap.get(key));
            }
        } else {
            this.description = null;
        }
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLocalName() {
        return this.localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getNameSpace() {
        return this.nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public Map<String, Collection<String>> getAlternateLabel() {
        return this.alternateLabel;
    }

    @SuppressWarnings("unchecked")
    public void setAlternateLabel(Map<String, Collection<String>> alternateLabel) {
        if (alternateLabel != null) {
            for(String lang : alternateLabel.keySet()) {
                for(String label : alternateLabel.get(lang)) {
                    this.addAlternateLabel(lang, label);
                }
            }
        } else {
            this.alternateLabel = null;
        }
    }

    public Map<String, Collection<String>> getHiddenLabel() {
        return this.hiddenLabel;
    }

    @SuppressWarnings("unchecked")
    public void setHiddenLabel(Map<String, Collection<String>> hiddenLabel) {
        if (hiddenLabel != null) {
            for(String lang : hiddenLabel.keySet()) {
                for(String label : hiddenLabel.get(lang)) {
                    this.addHiddenLabel(lang, label);
                }
            }
        } else {
            this.hiddenLabel = null;
        }
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    static class SimpleConcept extends Concept {
        SimpleConcept() {
        }
    }
}