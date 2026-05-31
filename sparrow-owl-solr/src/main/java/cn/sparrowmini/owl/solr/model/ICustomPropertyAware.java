package cn.sparrowmini.owl.solr.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ICustomPropertyAware {
    String CUSTOM_KEY_FIELD = "*_key";
    String CUSTOM_STRING_PROPERTY = "*_ss";
    String CUSTOM_DOUBLE_PROPERTY = "*_ds";
    String CUSTOM_INTEGER_PROPERTY = "*_is";
    String CUSTOM_BOOLEAN_PROPERTY = "*_b";

    String getUri();

    /**
     * 获取当前实体对应的 Solr Collection 名称。
     * 移除旧的 Spring Data @SolrDocument 反射依赖。
     * 子类实体（如 PartyType）由于已经实现了该方法，这里直接声明为抽象方法即可无缝衔接。
     */
    String getCollection();

    default void addProperty(Double value, String... qualifier) {
        this.addProperty(value, (PropertyType) null, qualifier);
    }

    @SuppressWarnings("unchecked")
    default void addProperty(Double value, PropertyType customMeta, String... qualifier) {
        String key = DynamicName.getDynamicFieldPart(qualifier);
        this.getCustomPropertyKeys().put(key, String.join("@", qualifier));
        Collection<Double> values = this.getCustomDoubleValues().get(key);
        if (values == null) {
            values = new HashSet<>();
            this.getCustomDoubleValues().put(key, values);
        }

        values.add(value);
        if (customMeta != null) {
            this.getCustomProperties().put(key, customMeta);
        }
    }

    default void addProperty(Integer value, String... qualifier) {
        this.addProperty(value, (PropertyType) null, qualifier);
    }

    @SuppressWarnings("unchecked")
    default void addProperty(Integer value, PropertyType customMeta, String... qualifier) {
        String key = DynamicName.getDynamicFieldPart(qualifier);
        this.getCustomPropertyKeys().put(key, String.join("@", qualifier));
        Collection<Integer> values = this.getCustomIntValues().get(key);
        if (values == null) {
            values = new HashSet<>();
            this.getCustomIntValues().put(key, values);
        }

        values.add(value);
        if (customMeta != null) {
            this.getCustomProperties().put(key, customMeta);
        }
    }

    default void addProperty(String value, String... qualifier) {
        this.addProperty(value, (PropertyType) null, qualifier);
    }

    @SuppressWarnings("unchecked")
    default void setProperties(Collection<?> values, PropertyType customMeta, String... qualifier) {
        String key = DynamicName.getDynamicFieldPart(qualifier);
        this.getCustomPropertyKeys().put(key, String.join("@", qualifier));
        Optional<?> first = values.stream().filter(Objects::nonNull).findFirst();
        if (first.isPresent()) {
            Object firstObj = first.get();

            if (firstObj instanceof String) {
                List<String> strVal = values.stream()
                        .map(val -> (String) val)
                        .collect(Collectors.toList());
                this.getCustomStringValues().put(key, strVal);
            }

            if (firstObj instanceof Integer) {
                List<Integer> intVal = values.stream()
                        .map(val -> (Integer) val)
                        .collect(Collectors.toList());
                this.getCustomIntValues().put(key, intVal);
            }

            if (firstObj instanceof Double) {
                List<Double> doubleVal = values.stream()
                        .map(val -> (Double) val)
                        .collect(Collectors.toList());
                this.getCustomDoubleValues().put(key, doubleVal);
            }

            if (firstObj instanceof Boolean) {
                this.getCustomBooleanValue().put(key, (Boolean) firstObj);
            }

            if (customMeta != null) {
                this.getCustomProperties().put(key, customMeta);
            }
        }
    }

    default void setProperties(Collection<?> values, String... qualifier) {
        this.setProperties(values, (PropertyType) null, qualifier);
    }

    default <T> Optional<T> getProperty(Class<T> clazz, String... qualifier) {
        List<T> coll = this.getProperties(clazz, qualifier);
        return !coll.isEmpty() ? Optional.of(coll.get(0)) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    default <T> List<T> getProperties(Class<T> clazz, String... qualifier) {
        String key = DynamicName.getDynamicFieldPart(qualifier);

        if (Integer.class.equals(clazz)) {
            Collection<Integer> is = this.getCustomIntValues().get(key);
            if (is != null && !is.isEmpty()) {
                return is.stream()
                        .map(clazz::cast)
                        .collect(Collectors.toList());
            }
        } else if (Double.class.equals(clazz)) {
            Collection<Double> ds = this.getCustomDoubleValues().get(key);
            if (ds != null && !ds.isEmpty()) {
                return ds.stream()
                        .map(clazz::cast)
                        .collect(Collectors.toList());
            }
        } else if (Boolean.class.equals(clazz)) {
            Boolean b = this.getCustomBooleanValue().get(key);
            if (b != null) {
                return Collections.singletonList(clazz.cast(b));
            }
        } else {
            Collection<String> ss = this.getCustomStringValues().get(key);
            if (ss != null && !ss.isEmpty()) {
                return ss.stream()
                        .map(clazz::cast)
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    default void addProperty(String value, PropertyType customMeta, String... qualifier) {
        String key = DynamicName.getDynamicFieldPart(qualifier);
        this.getCustomPropertyKeys().put(key, String.join("@", qualifier));
        Collection<String> values = this.getCustomStringValues().get(key);
        if (values == null) {
            values = new HashSet<>();
            this.getCustomStringValues().put(key, values);
        }

        values.add(value);
        if (customMeta != null) {
            this.getCustomProperties().put(key, customMeta);
        }
    }

    default void setProperty(String value, String... qualifier) {
        this.setProperties(Collections.singletonList(value), (PropertyType) null, qualifier);
    }

    default void setProperty(String value, PropertyType meta, String... qualifier) {
        this.setProperties(Collections.singletonList(value), meta, qualifier);
    }

    default void setProperty(Integer value, String... qualifier) {
        this.setProperties(Collections.singletonList(value), (PropertyType) null, qualifier);
    }

    default void setProperty(Integer value, PropertyType meta, String... qualifier) {
        this.setProperties(Collections.singletonList(value), meta, qualifier);
    }

    default void setProperty(Double value, String... qualifier) {
        this.setProperties(Collections.singletonList(value), (PropertyType) null, qualifier);
    }

    default void setProperty(Double value, PropertyType meta, String... qualifier) {
        this.setProperties(Collections.singletonList(value), meta, qualifier);
    }

    default void setProperty(Boolean value, String... qualifier) {
        this.setProperties(Collections.singletonList(value), (PropertyType) null, qualifier);
    }

    default void setProperty(Boolean value, PropertyType meta, String... qualifier) {
        this.setProperties(Collections.singletonList(value), meta, qualifier);
    }

    Map<String, Collection<Integer>> getCustomIntValues();

    Map<String, Collection<Double>> getCustomDoubleValues();

    Map<String, Collection<String>> getCustomStringValues();

    Map<String, Boolean> getCustomBooleanValue();

    Map<String, String> getCustomPropertyKeys();

    Map<String, PropertyType> getCustomProperties();
}