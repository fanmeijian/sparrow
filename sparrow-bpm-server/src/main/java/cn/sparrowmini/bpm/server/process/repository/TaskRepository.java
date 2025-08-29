package cn.sparrowmini.bpm.server.process.repository;

import org.jbpm.services.task.impl.model.TaskImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<TaskImpl,Long> {

}
