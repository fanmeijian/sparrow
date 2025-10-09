package cn.sparrowmini.bpm.server.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.criteria.Path;
import java.util.Collection;

@NoRepositoryBean
public interface BaseRepository<T,ID> extends JpaRepository<T,ID>, JpaSpecificationExecutor<T> {
    default Specification<T> in(String field, Collection<?> collection) {
        return (root, query, cb) -> {
            Path<?> path = root;
            // 支持 a.b.c 形式的路径
            for (String part : field.split("\\.")) {
                path = path.get(part);
            }
            return path.in(collection);
        };
    }


    default Specification<T> equal(String field, Object value) {
        return (root, query, cb) -> {
            Path<?> path = root;
            // 支持 a.b.c 形式的路径
            for (String part : field.split("\\.")) {
                path = path.get(part);
            }
            return cb.equal(path, value);
        };
    }
}
