package cn.sparrowmini.common.antlr;
import jakarta.persistence.criteria.*;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * String expr = "(status = 'SENT' and createdAt > '2024-07-01') or category in ('A', 'B') and expireAt is not null";
 * Predicate predicate = PredicateBuilder.buildPredicate(expr, cb, root);
 */

public class ExprToPredicateVisitor extends ExprBaseVisitor<Predicate> {

    private final CriteriaBuilder cb;
    private final Root<?> root;

    public ExprToPredicateVisitor(CriteriaBuilder cb, Root<?> root) {
        this.cb = cb;
        this.root = root;
    }

    @Override
    public Predicate visitAndExpr(ExprParser.AndExprContext ctx) {
        Predicate left = visit(ctx.expr(0));
        Predicate right = visit(ctx.expr(1));
        return cb.and(left, right);
    }

    @Override
    public Predicate visitOrExpr(ExprParser.OrExprContext ctx) {
        Predicate left = visit(ctx.expr(0));
        Predicate right = visit(ctx.expr(1));
        return cb.or(left, right);
    }

    @Override
    public Predicate visitNotExpr(ExprParser.NotExprContext ctx) {
        return cb.not(visit(ctx.expr()));
    }

    @Override
    public Predicate visitParenExpr(ExprParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }


    @Override
    public Predicate visitCompareExpr(ExprParser.CompareExprContext ctx) {
        String fieldName = ctx.field().getText(); // 支持嵌套字段，如 "id.saleDate"
        String operator = ctx.comparator().getText();
        Object value = parseValue(ctx.value());

        Path<?> path = resolvePath(root, fieldName); // 处理嵌套字段路径

        if (value instanceof String str) {
            return switch (operator) {
                case "=" -> cb.equal(path, str);
                case "!=" -> cb.notEqual(path, str);
                case "like" -> cb.like(path.as(String.class), "%" + str + "%");
                default -> throw new IllegalArgumentException("Unsupported string operator: " + operator);
            };
        } else if (value instanceof LocalDate date) {
            return switch (operator) {
                case ">" -> cb.greaterThan(path.as(LocalDate.class), date);
                case "<" -> cb.lessThan(path.as(LocalDate.class), date);
                case ">=" -> cb.greaterThanOrEqualTo(path.as(LocalDate.class), date);
                case "<=" -> cb.lessThanOrEqualTo(path.as(LocalDate.class), date);
                case "=" -> cb.equal(path.as(LocalDate.class), date);
                case "!=" -> cb.notEqual(path.as(LocalDate.class), date);
                default -> throw new IllegalArgumentException("Unsupported LocalDate operator: " + operator);
            };
        } else if (value instanceof LocalDateTime dateTime) {
            return switch (operator) {
                case ">" -> cb.greaterThan(path.as(LocalDateTime.class), dateTime);
                case "<" -> cb.lessThan(path.as(LocalDateTime.class), dateTime);
                case ">=" -> cb.greaterThanOrEqualTo(path.as(LocalDateTime.class), dateTime);
                case "<=" -> cb.lessThanOrEqualTo(path.as(LocalDateTime.class), dateTime);
                case "=" -> cb.equal(path.as(LocalDateTime.class), dateTime);
                case "!=" -> cb.notEqual(path.as(LocalDateTime.class), dateTime);
                default -> throw new IllegalArgumentException("Unsupported LocalDateTime operator: " + operator);
            };
        } else if (value instanceof OffsetDateTime offsetDateTime) {
            return switch (operator) {
                case ">" -> cb.greaterThan(path.as(OffsetDateTime.class), offsetDateTime);
                case "<" -> cb.lessThan(path.as(OffsetDateTime.class), offsetDateTime);
                case ">=" -> cb.greaterThanOrEqualTo(path.as(OffsetDateTime.class), offsetDateTime);
                case "<=" -> cb.lessThanOrEqualTo(path.as(OffsetDateTime.class), offsetDateTime);
                case "=" -> cb.equal(path.as(OffsetDateTime.class), offsetDateTime);
                case "!=" -> cb.notEqual(path.as(OffsetDateTime.class), offsetDateTime);
                default -> throw new IllegalArgumentException("Unsupported OffsetDateTime operator: " + operator);
            };
        } else if (value instanceof Number number) {
            return switch (operator) {
                case ">" -> cb.gt(path.as(Number.class), number);
                case "<" -> cb.lt(path.as(Number.class), number);
                case ">=" -> cb.ge(path.as(Number.class), number);
                case "<=" -> cb.le(path.as(Number.class), number);
                case "=" -> cb.equal(path.as(Number.class), number);
                case "!=" -> cb.notEqual(path.as(Number.class), number);
                default -> throw new IllegalArgumentException("Unsupported Number operator: " + operator);
            };
        } else {
            throw new IllegalArgumentException("不支持的值类型: " + value.getClass());
        }
    }

    private Object parseValue(ExprParser.ValueContext ctx) {
        if (ctx.STRING() != null) {
            String raw = ctx.STRING().getText(); // e.g. 'abc-123'
            String unquoted = raw.substring(1, raw.length() - 1).replace("\\'", "'").trim();

            // 优先尝试日期时间类型
            try {
                return OffsetDateTime.parse(unquoted, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (DateTimeParseException ignored) {}

            try {
                return LocalDateTime.parse(unquoted, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ignored) {}

            try {
                return LocalDate.parse(unquoted, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) {}

            // 布尔值（true/false，不区分大小写）
            if (unquoted.equalsIgnoreCase("true")) return true;
            if (unquoted.equalsIgnoreCase("false")) return false;

            // 注意：UUID 结构不做转换，保持为字符串
            // 你可在调用处通过字段类型判断后决定是否 UUID.fromString()

            // 默认当成普通字符串返回
            return unquoted;

        } else if (ctx.NUMBER() != null) {
            String number = ctx.NUMBER().getText();
            try {
                if (number.contains(".")) {
                    return Double.parseDouble(number);
                } else {
                    try {
                        return Integer.parseInt(number);
                    } catch (NumberFormatException e) {
                        return Long.parseLong(number); // 比如很大的ID
                    }
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("无法解析数值: " + number);
            }
        }

        return null; // 没匹配到
    }

    @Override
    public Predicate visitIsNullExpr(ExprParser.IsNullExprContext ctx) {
        String field = ctx.field().getText();
        return cb.isNull(root.get(field));
    }

    @Override
    public Predicate visitIsNotNullExpr(ExprParser.IsNotNullExprContext ctx) {
        String field = ctx.field().getText();
        return cb.isNotNull(root.get(field));
    }

    @Override
    public Predicate visitInExpr(ExprParser.InExprContext ctx) {
        String fieldName = ctx.field().getText();
        Path<Object> path = root.get(fieldName);

        // 获取字段的 Java 类型
        Class<?> javaType = path.getJavaType();

        // 如果是集合类型（如 Set、List 等）
        if (Collection.class.isAssignableFrom(javaType)) {
            List<Predicate> predicates = new ArrayList<>();
            for (ExprParser.ValueContext valueCtx : ctx.valueList().value()) {
                Object val = parseValue(valueCtx);
                predicates.add(cb.isMember(val, root.get(fieldName)));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        }

        // 普通字段处理
        CriteriaBuilder.In<Object> in = cb.in(path);
        for (ExprParser.ValueContext valueCtx : ctx.valueList().value()) {
            in.value(parseValue(valueCtx));
        }
        return in;
    }

    private Path<?> resolvePath(Root<?> root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

}