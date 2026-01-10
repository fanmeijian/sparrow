package cn.sparrowmini.bpm.api.repository;

import cn.sparrowmini.bpm.api.model.PublishedProcess;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

@Repository
public interface ProcessRepository extends CrudRepository<PublishedProcess, Long> {
}
