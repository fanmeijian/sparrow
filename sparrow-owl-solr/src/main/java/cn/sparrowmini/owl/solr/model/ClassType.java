package cn.sparrowmini.owl.solr.model;

import cn.sparrowmini.owl.solr.model.IClassType;
import java.util.Collection;
import java.util.HashSet;
// 引入 SolrJ 注解
import org.apache.solr.client.solrj.beans.Field;

public class ClassType extends Concept implements IClassType {

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

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<String> getProperties() {
        return this.properties;
    }

    public void addProperty(String property) {
        if (this.properties == null) {
            this.properties = new HashSet<>();
        }
        this.properties.add(property);
    }

    public void setProperties(Collection<String> properties) {
        this.properties = properties;
    }

    public void addParent(String superClass) {
        if (this.parents == null) {
            this.parents = new HashSet<>();
        }
        this.parents.add(superClass);
    }

    public Collection<String> getParents() {
        return this.parents;
    }

    public void setParents(Collection<String> parent) {
        this.parents = parent;
    }

    public void addChild(String childClass) {
        if (this.children == null) {
            this.children = new HashSet<>();
        }
        this.children.add(childClass);
    }

    public Collection<String> getChildren() {
        return this.children;
    }

    public void setChildren(Collection<String> child) {
        this.children = child;
    }

    public void addAllParent(String superClass) {
        if (this.allParents == null) {
            this.allParents = new HashSet<>();
        }
        this.allParents.add(superClass);
    }

    public Collection<String> getAllParents() {
        return this.allParents;
    }

    public void setAllParents(Collection<String> parent) {
        this.allParents = parent;
    }

    public void addAllChild(String childClass) {
        if (this.allChildren == null) {
            this.allChildren = new HashSet<>();
        }
        this.allChildren.add(childClass);
    }

    public Collection<String> getAllChildren() {
        return this.allChildren;
    }

    public void setAllChildren(Collection<String> child) {
        this.allChildren = child;
    }

    public Integer getLevel() {
        return this.level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}