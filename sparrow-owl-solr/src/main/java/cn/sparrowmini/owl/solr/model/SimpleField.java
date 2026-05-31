package cn.sparrowmini.owl.solr.model;


import org.springframework.util.ObjectUtils;

/**
 * The most trivial implementation of a Field
 *
 * @author Christoph Strobl
 * @author Francisco Spaeth
 */
public class SimpleField implements Field {

    private final String name;

    public SimpleField(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.name);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SimpleField)) {
            return false;
        }
        SimpleField that = (SimpleField) other;
        return ObjectUtils.nullSafeEquals(this.name, that.name);
    }

}