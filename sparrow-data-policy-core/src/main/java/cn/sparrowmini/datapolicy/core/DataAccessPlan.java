package cn.sparrowmini.datapolicy.core;

import java.util.*;
import cn.sparrowmini.security.api.*;

/** Trusted per-request plan. The host resolves current identity and policy snapshots. */
public final class DataAccessPlan {
    private final FunctionDecision function;
    private final long policyRevision;
    private final List<DataRule> rules;
    private final List<Condition> restrictions;
    private final Map<FieldOperation, List<Condition>> fieldRestrictions;
    private record FieldOperation(String field, DataRule.Target target) { }

    public DataAccessPlan(FunctionDecision function, List<DataRule> rules) {
        this(function,rules,0);
    }
    public DataAccessPlan(FunctionDecision function,List<DataRule> rules,long policyRevision){
        this(function, rules, List.of(), Map.of(),policyRevision);
    }
    private DataAccessPlan(FunctionDecision function, List<DataRule> rules, List<Condition> restrictions,
                           Map<FieldOperation, List<Condition>> fieldRestrictions,long policyRevision) {
        if(policyRevision<0)throw new IllegalArgumentException("Invalid policy revision");this.policyRevision=policyRevision;
        this.function = Objects.requireNonNull(function);
        this.rules = List.copyOf(rules);
        this.restrictions = List.copyOf(restrictions);
        this.fieldRestrictions = Map.copyOf(fieldRestrictions);
    }
    public long policyRevision(){return policyRevision;}
    public PermissionRequest permission() { return function.request(); }
    public DataAccessPlan restrict(Condition condition) {
        var next = new ArrayList<>(restrictions);
        next.add(Objects.requireNonNull(condition));
        return new DataAccessPlan(function, rules, next, fieldRestrictions,policyRevision);
    }
    public DataAccessPlan restrictField(String field, DataRule.Target target, Condition condition) {
        Objects.requireNonNull(field); Objects.requireNonNull(target); Objects.requireNonNull(condition);
        if (field.isBlank() || target == DataRule.Target.ROW) throw new IllegalArgumentException("Invalid field restriction");
        var key = new FieldOperation(field, target);
        var next = new HashMap<>(fieldRestrictions);
        var terms = new ArrayList<>(next.getOrDefault(key, List.of()));
        terms.add(condition); next.put(key, List.copyOf(terms));
        return new DataAccessPlan(function, rules, restrictions, next,policyRevision);
    }
    public Condition rowCondition() {
        if (!function.allowed()) return new Condition.None();
        var terms = new ArrayList<>(restrictions);
        terms.add(policyCondition(DataRule.Target.ROW, null));
        return new Condition.And(terms);
    }
    public Condition fieldCondition(String field, DataRule.Target target) {
        Objects.requireNonNull(field); Objects.requireNonNull(target);
        if (target == DataRule.Target.ROW) throw new IllegalArgumentException("Expected field operation");
        var terms = new ArrayList<>(fieldRestrictions.getOrDefault(new FieldOperation(field, target), List.of()));
        terms.add(rowCondition()); terms.add(policyCondition(target, field));
        return new Condition.And(terms);
    }
    public boolean canAccess(Map<String, Object> row) { return rowCondition().matches(row); }
    public boolean canRead(Map<String, Object> row, String field) {
        return fieldCondition(field, DataRule.Target.FIELD_READ).matches(row);
    }
    public boolean canWrite(Map<String, Object> row, String field) {
        return fieldCondition(field, DataRule.Target.FIELD_WRITE).matches(row);
    }
    /** Conservative gate: conditional field visibility cannot safely authorize arbitrary filtering/sorting. */
    public boolean canQueryField(String field) {
        if (!function.allowed() || fieldRestrictions.containsKey(new FieldOperation(field, DataRule.Target.FIELD_READ))) return false;
        boolean allow = false;
        for (DataRule rule : applicable(DataRule.Target.FIELD_READ, field)) {
            if (rule.effect() == Effect.DENY) return false;
            if (eligible(rule) && rule.condition() instanceof Condition.All) allow = true;
        }
        return allow;
    }
    private List<DataRule> applicable(DataRule.Target target, String field) {
        return rules.stream().filter(r -> r.permission().equals(function.request()) && r.target() == target
                && r.subject().matches(function.actor()) && (target == DataRule.Target.ROW || r.fields().contains(field))).toList();
    }
    private boolean eligible(DataRule rule) {
        return rule.subject().kind() != Subject.Kind.ROLE || function.eligibleRoles().contains(rule.subject().id());
    }
    private Condition policyCondition(DataRule.Target target, String field) {
        var allow = new ArrayList<Condition>(); var deny = new ArrayList<Condition>();
        for (DataRule rule : applicable(target, field)) {
            if (rule.effect() == Effect.DENY) deny.add(rule.condition());
            else if (eligible(rule)) allow.add(rule.condition());
        }
        return new Condition.And(List.of(new Condition.Or(allow), new Condition.Not(new Condition.Or(deny))));
    }
}
