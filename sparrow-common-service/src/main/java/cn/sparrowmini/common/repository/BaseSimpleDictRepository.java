package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.SimpleDict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseSimpleDictRepository<T extends SimpleDict> extends BaseRepository<T, String> {

}
