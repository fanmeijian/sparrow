package cn.sparrowmini.owl.solr.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
// 引入 SolrJ 注解替换 Spring Data Solr
import org.apache.solr.client.solrj.beans.Field;

public class PropertyType extends Concept implements IPropertyType {

    @Field("doctype")
    private String type = "property";

    @Field("range")
    private String range;

    @Field("valueQualifier")
    private ValueQualifier valueQualifier;

    @Field("used_in")
    private Collection<String> product;

    @Field("used_by")
    private Collection<String> items;

    @Field("idxField")
    private Collection<String> itemFieldNames;

    @Field("isFacet")
    private boolean facet = true;

    @Field("isVisible")
    private boolean visible = true;

    @Field("isRequired")
    private boolean required = false;

    @Field("boost")
    private Double boost;

    @Field("propType")
    private String propertyType;

    @Field("codeList")
    private Collection<String> codeList;

    @Field("codeListId")
    private String codeListId;

    public PropertyType() {
    }

    public String getPropertyType() {
        return this.propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    @JsonIgnore
    public Collection<String> getUnits() {
        return this.codeList;
    }

    public void setUnits(Collection<String> unitsTypeList) {
        this.codeList = unitsTypeList;
    }

    public Collection<String> getCodeList() {
        if (this.codeList == null) {
            this.codeList = new HashSet<>();
        }
        return this.codeList;
    }

    public void setCodeList(Collection<String> valueCodesList) {
        this.codeList = valueCodesList;
    }

    public String getRange() {
        return this.range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Collection<String> getProduct() {
        if (this.product == null) {
            this.product = new HashSet<>();
        }
        return this.product;
    }

    public void addProduct(String className) {
        if (this.product == null) {
            this.product = new HashSet<>();
        }
        // 注意：反编译出的原代码此处未向集合 add 任何东西，如有业务遗漏可在此处补上：this.product.add(className);
    }

    public void setProduct(Collection<String> className) {
        this.product = className;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<String> getItemFieldNames() {
        return this.itemFieldNames;
    }

    public void setItemFieldNames(Collection<String> idxFieldNames) {
        this.itemFieldNames = idxFieldNames;
    }

    @SuppressWarnings("unchecked")
    public void addItemFieldName(String idxField) {
        if (this.itemFieldNames == null) {
            this.itemFieldNames = new HashSet<>();
        } else if (!(this.itemFieldNames instanceof Set)) {
            this.itemFieldNames = this.itemFieldNames.stream().collect(Collectors.toSet());
        }
        this.itemFieldNames.add(idxField);
    }

    public ValueQualifier getValueQualifier() {
        return this.valueQualifier;
    }

    public void setValueQualifier(ValueQualifier valueQualifier) {
        this.valueQualifier = valueQualifier;
    }

    @JsonIgnore
    public boolean isFacet() {
        return this.facet;
    }

    public void setFacet(boolean facet) {
        this.facet = facet;
    }

    @JsonIgnore
    public Double getBoost() {
        return this.boost;
    }

    public void setBoost(Double boost) {
        this.boost = boost;
    }

    public Collection<String> getItems() {
        return this.items;
    }

    public void setItems(Collection<String> items) {
        this.items = items;
    }

    public void addItem(String uri) {
        if (this.items == null) {
            this.items = new HashSet<>();
        }
        this.items.add(uri);
    }

    public boolean removeItem(String item) {
        return this.items == null ? false : this.items.remove(item);
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getCodeListId() {
        return this.codeListId;
    }

    public void setCodeListId(String codeListUri) {
        this.codeListId = codeListUri;
    }
}