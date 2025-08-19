package cn.sparrowmini.common.repository;


import cn.sparrowmini.common.model.PageElement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PageElementRepository extends JpaRepository<PageElement, String> {
   List<PageElement> findByPageId(String pageId);

   List<PageElement> findByIdIn(Collection<String> elementIds);
}
