package cn.sparrowmini.owl.solr.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
// 引入 SolrJ 注解
import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.Setter;
import org.apache.solr.client.solrj.beans.Field;

@Setter
@Getter
public class ClassType extends BaseMetadataObject implements IClassType {

    @Field("doctype")
    private String type = "class";

    @Field("properties")
    private Collection<String> properties;

    @Field("parents")
    private Collection<String> parents;

    @Field("children")
    private Collection<String> children;

    @Field("allParents")
    private Collection<String> allParents;

    @Field("allChildren")
    private Collection<String> allChildren;

    @Field("level")
    private Integer level;


    public ClassType() {
    }

    public void addProperty(String property) {
        if (this.properties == null) {
            this.properties = new HashSet<>();
        }
        this.properties.add(property);
    }

    public void addParent(String superClass) {
        if (this.parents == null) {
            this.parents = new HashSet<>();
        }
        this.parents.add(superClass);
    }

    public void addChild(String childClass) {
        if (this.children == null) {
            this.children = new HashSet<>();
        }
        this.children.add(childClass);
    }

    public void addAllParent(String superClass) {
        if (this.allParents == null) {
            this.allParents = new HashSet<>();
        }
        this.allParents.add(superClass);
    }

    public void addAllChild(String childClass) {
        if (this.allChildren == null) {
            this.allChildren = new HashSet<>();
        }
        this.allChildren.add(childClass);
    }

}