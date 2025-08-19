package cn.sparrowmini.common.model;

import cn.sparrowmini.common.constant.LevelTypeEnum;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 用于构建通用过滤器使用
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparrowJpaFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    private FilterTreeBean filterTreeBean;
    private List<SparrowJpaFilter> children;


    public SparrowJpaFilter(FilterTreeBean filterTreeBean) {
        super();
        this.filterTreeBean = filterTreeBean;
    }


    @Data
    public static class FilterTreeBean implements Serializable {
        private static final long serialVersionUID = 1L;
        private LevelTypeEnum type;
        private String name;
        private String op;
        private Object value;
        private boolean not;


        public FilterTreeBean(LevelTypeEnum type, String name, String op, Object value, boolean not) {
            super();
            this.type = type;
            this.name = name;
            this.op = op;
            this.value = value;
            this.not = not;
        }


        @SuppressWarnings({"unchecked"})
        public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            if (this.name == null || this.name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Path<?> path = null;
            if (this.name.contains(".")) {
                String[] pnames = this.name.split("\\.");
                path = root.get(pnames[0]);
                for (int i = 1; i < pnames.length; i++) {
                    path = path.get(pnames[i]);
                }
            } else {
                path = root.get(this.name);
            }
            Predicate predicate = null;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            System.out.println(path.getJavaType().getCanonicalName());
            if(path.getJavaType().isEnum()){
                value = Enum.valueOf((Class<Enum>) path.getJavaType(), value.toString());
            }
//            if(this.name.equals("entityStat")){
//                value = Enum.valueOf(CommonStateEnum.class,value.toString());
//            }
            switch (op) {
                case "!=":
                    predicate = value == null ? criteriaBuilder.isNotNull(path) : criteriaBuilder.notEqual(path, value);
                    break;
                case "=":
                    predicate = value == null ? criteriaBuilder.isNull(path) : criteriaBuilder.equal(path, value);
                    break;
                case "contain":
                    predicate = criteriaBuilder.like(path.as(String.class), "%" + value.toString() + "%");
                    break;
                case "end":
                    predicate = criteriaBuilder.like(path.as(String.class), "%" + value.toString());
                    break;
                case "start":
                    predicate = criteriaBuilder.like(path.as(String.class), value.toString() + "%");
                    break;
                case ">":
                    try {
                        predicate = path.getJavaType().getCanonicalName().equals("java.util.Date")
                                ? criteriaBuilder.greaterThan(path.as(Date.class), formatter.parse(value.toString()))
                                : criteriaBuilder.gt((Expression<? extends Number>) path, (Number) value);
                    } catch (ParseException e) {
                        log.error(e.getMessage(), e);
                    }
                    break;
                case ">=":
                    try {
                        predicate = path.getJavaType().getCanonicalName().equals("java.util.Date")
                                ? criteriaBuilder.greaterThanOrEqualTo(path.as(Date.class),
                                formatter.parse(value.toString()))
                                : criteriaBuilder.ge((Expression<? extends Number>) path, (Number) value);
                    } catch (ParseException e) {
                        log.error(e.getMessage(), e);
                    }
                    break;
                case "<":
                    try {
                        predicate = path.getJavaType().getCanonicalName().equals("java.util.Date")
                                ? criteriaBuilder.lessThan(path.as(Date.class), formatter.parse(value.toString()))
                                : criteriaBuilder.lt((Expression<? extends Number>) path, (Number) value);
                    } catch (ParseException e) {
                        log.error(e.getMessage(), e);
                    }
                    break;
                case "<=":
                    try {
                        predicate = path.getJavaType().getCanonicalName().equals("java.util.Date")
                                ? criteriaBuilder.lessThanOrEqualTo(path.as(Date.class), formatter.parse(value.toString()))
                                : criteriaBuilder.le((Expression<? extends Number>) path, (Number) value);
                    } catch (ParseException e) {
                        log.error(e.getMessage(), e);
                    }
                    break;
                case "in":
                    predicate = path.in(value);
                    break;
                default:
                    predicate = criteriaBuilder.conjunction();
                    break;
            }
//			predicate = this.type == LevelTypeEnum.AND ? criteriaBuilder.and(predicate) : criteriaBuilder.or(predicate);
            if (this.not) {
                return predicate.not();
            } else {
                return predicate;
            }
        }

        @Override
        public String toString() {
            switch (op) {
                case "!=":
                    if (this.value != null) {
                        return String.join(" != ", this.name, StringUtils.isNumeric(this.value.toString()) ? this.value.toString() : "'" + this.value.toString() + "'");
                    } else {
                        return this.name + " is not null";
                    }
                case "=":
                    if (this.value != null) {
                        return String.join(" = ", this.name, StringUtils.isNumeric(this.value.toString()) ? this.value.toString() : "'" + this.value.toString() + "'");
                    } else {
                        return this.name + " is null";
                    }
                case "contain":
                    return String.join(" LIKE ", this.name, "%" + value.toString() + "%");
                case "end":
                    return String.join(" LIKE ", this.name, "%" + value.toString());
                case "start":
                    return String.join(" LIKE ", this.name, value.toString() + "%");
                case ">":
                    return String.join(" > ", this.name, StringUtils.isNumeric(this.value.toString()) ? this.value.toString() : "'" + this.value.toString() + "'");
                case ">=":
                    return String.join(" >= ", this.name, StringUtils.isNumeric(this.value.toString()) ? this.value.toString() : "'" + this.value.toString() + "'");
                case "<":
                    return String.join(" < ", this.name, StringUtils.isNumeric(this.value.toString()) ? this.value.toString() : "'" + this.value.toString() + "'");
                case "<=":
                    return String.join(" <= ", this.name, StringUtils.isNumeric(this.value.toString()) ? this.value.toString() : "'" + this.value.toString() + "'");
                case "in":
                    return String.join(" in ", this.name, "(" + value.toString() + ")");
                default:
                    return "";
            }
        }
    }
}
