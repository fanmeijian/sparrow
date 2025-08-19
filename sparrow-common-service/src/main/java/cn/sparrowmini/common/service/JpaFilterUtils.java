package cn.sparrowmini.common.service;

import cn.sparrowmini.common.dto.FilterType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class JpaFilterUtils {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // 支持 LocalDate
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /**
     * 将 JSON 字符串转为 List<SimpleJpaFilter>
     */
    public static List<SimpleJpaFilter> parseJsonFilters(String json) {
        try {
            return Arrays.asList(mapper.readValue(json, SimpleJpaFilter[].class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse filters JSON", e);
        }
    }

    /**
     * 根据 Filter 构建 Predicate
     */
    public static List<Predicate> getPredicates(Root<?> root, CriteriaBuilder cb, List<SimpleJpaFilter> filters) {
        List<Predicate> predicates = new ArrayList<>();

        for (SimpleJpaFilter filter : filters) {
            String field = filter.getName();
            String op = filter.getOperator().toLowerCase();
            String typeStr = filter.getFilterType();
            Object rawValue = filter.getValue();

            FilterType filterType = FilterType.fromName(typeStr);
            Class<?> javaType = filterType.getJavaType();
            Path<?> path = root.get(field);
            Object value = convertValue(rawValue, filterType);

            switch (op) {
                case "=":
                    predicates.add(cb.equal(path, value));
                    break;
                case "!=":
                    predicates.add(cb.notEqual(path, value));
                    break;
                case ">":
                    predicates.add(cb.greaterThan((Path<? extends Comparable>) path, (Comparable) value));
                    break;
                case "<":
                    predicates.add(cb.lessThan((Path<? extends Comparable>) path, (Comparable) value));
                    break;
                case ">=":
                    predicates.add(cb.greaterThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) value));
                    break;
                case "<=":
                    predicates.add(cb.lessThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) value));
                    break;
                case "like":
                    predicates.add(cb.like(path.as(String.class), "%" + value + "%"));
                    break;
                case "in":
                    if (!(value instanceof Collection<?>)) {
                        throw new IllegalArgumentException("IN 需要集合类型");
                    }
                    CriteriaBuilder.In<Object> inClause = cb.in(path);
                    for (Object item : (Collection<?>) value) {
                        inClause.value(item);
                    }
                    predicates.add(inClause);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的操作符: " + op);
            }
        }

        return predicates;
    }

    /**
     * 根据类型手动转换值
     */
    private static Object convertValue(Object value, FilterType type) {
        if (value == null) return null;
        switch (type) {
            case STRING:
                return value.toString();
            case INTEGER:
                return Integer.parseInt(value.toString());
            case LONG:
                return Long.parseLong(value.toString());
            case DOUBLE:
                return Double.parseDouble(value.toString());
            case BOOLEAN:
                return Boolean.parseBoolean(value.toString());
            case DATE:
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(value.toString());
                } catch (ParseException e) {
                    throw new RuntimeException("日期格式错误，应为 yyyy-MM-dd", e);
                }
            case LOCAL_DATE:
                return LocalDate.parse(value.toString());
            case LOCAL_DATE_TIME:
                return LocalDateTime.parse(value.toString());
            default:
                return value;
        }
    }
}