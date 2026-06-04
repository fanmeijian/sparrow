package cn.sparrowmini.owl.solr.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
// 引入 SolrJ 注解替换 Spring Data Solr
import lombok.Getter;
import lombok.Setter;
import org.apache.solr.client.solrj.beans.Field;

@Getter
@Setter
public class PropertyType extends BaseMetadataObject implements IPropertyType {

    @Field("doctype")
    private String type = "property";

    @Field("range")
    private Collection<String> range;

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

    @Field("dependsOnProperty")
    private String dependsOnProperty;



    public PropertyType() {
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


    @SuppressWarnings("unchecked")
    public void addItemFieldName(String idxField) {
        if (this.itemFieldNames == null) {
            this.itemFieldNames = new HashSet<>();
        } else if (!(this.itemFieldNames instanceof Set)) {
            this.itemFieldNames = this.itemFieldNames.stream().collect(Collectors.toSet());
        }
        this.itemFieldNames.add(idxField);
    }



    @JsonIgnore
    public boolean isFacet() {
        return this.facet;
    }

    @JsonIgnore
    public Double getBoost() {
        return this.boost;
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

}