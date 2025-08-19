package cn.sparrowmini.common.dto;
public enum FilterType {
    STRING(String.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    BOOLEAN(Boolean.class),
    DATE(java.util.Date.class),
    LOCAL_DATE(java.time.LocalDate.class),
    LOCAL_DATE_TIME(java.time.LocalDateTime.class);

    private final Class<?> javaType;

    FilterType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public static FilterType fromName(String name) {
        return FilterType.valueOf(name.toUpperCase());
    }
}