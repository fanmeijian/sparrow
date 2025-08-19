package cn.sparrowmini.common.model;
public interface UpsertRepository<T, ID> {
    void upsert(T entity);
    void upsertAll(Iterable<T> entities);
}