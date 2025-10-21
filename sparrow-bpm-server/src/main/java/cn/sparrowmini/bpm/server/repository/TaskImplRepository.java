package cn.sparrowmini.bpm.server.repository;

import cn.sparrowmini.bpm.server.dto.TaskDataImplDto;
import cn.sparrowmini.bpm.server.dto.TaskDataImplInfo;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.kie.api.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface TaskImplRepository extends JpaRepository<TaskImpl, Integer> {
    @Query(value = "select task_id as id from peopleassignments_potowners where entity_id in (:entityIds)",
            countQuery = "select count(distinct t.id) from Task t " +
            "join PeopleAssignments_PotOwners p on p.task_id = t.id " +
            "where p.entity_id in (:entityIds)",
            nativeQuery = true)
    Page<TaskImpl> myTasks(Set<String> entityIds, Pageable pageable);

    @Query("select distinct t " +
            "from TaskImpl t " +
            "join t.peopleAssignments.potentialOwners po " +
            "where po.id in :entityIds " +
            "order by t.id desc")
    Page<Task> findMyTasks(Set<String> entityIds, Pageable pageable);


    @Query(
            value = "SELECT t.id, t.name, t.createdon as createdOn, t.deploymentid as deploymentId, t.parentid as parentId, t.skipable,t.formname as formName, " +
                    "t.status, t.workitemid as workitemId, t.processinstanceid processInstanceId, t.processid as processId, v.value AS title, pi.processname as processName " +
                    "FROM Task t " +
                    "join processinstancelog pi on pi.processinstanceid=t.processinstanceid " +
                    "JOIN variableinstancelog v " +
                    "  ON t.processinstanceid = v.processinstanceid " +
                    " AND v.variableid = 'title' " +
                    " AND v.log_date = ( " +
                    "     SELECT MAX(v2.log_date) " +
                    "     FROM variableinstancelog v2 " +
                    "     WHERE v2.processinstanceid = v.processinstanceid " +
                    "       AND v2.variableid = 'title' " +
                    " ) " +
                    "JOIN PeopleAssignments_PotOwners p " +
                    "  ON p.task_id = t.id " +
                    "WHERE p.entity_id IN (:entityIds) " +
                    "  AND t.status IN (:status) " +
                    "ORDER BY t.id DESC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM Task t " +
                    "JOIN variableinstancelog v " +
                    "  ON t.processinstanceid = v.processinstanceid " +
                    " AND v.variableid = 'title' " +
                    " AND v.log_date = ( " +
                    "     SELECT MAX(v2.log_date) " +
                    "     FROM variableinstancelog v2 " +
                    "     WHERE v2.processinstanceid = v.processinstanceid " +
                    "       AND v2.variableid = 'title' " +
                    " ) " +
                    "JOIN PeopleAssignments_PotOwners p " +
                    "  ON p.task_id = t.id " +
                    "WHERE p.entity_id IN (:entityIds) " +
                    "  AND t.status IN (:status)",
            nativeQuery = true
    )
    Page<TaskDataImplInfo> findTasksWithLatestTitle(
            Collection<String> entityIds,
           Collection<String> status,
            Pageable pageable);

//
//    default void findMyTask(){
//        String sql = "select t.id from Task t " +
//                "join PeopleAssignments_PotOwners p on p.task_id = t.id " +
//                "where p.entity_id in (:entityIds) and t.status in (:statuses) " +
//                "order by t.id desc";
//
//        Query query = em.createNativeQuery(sql);
//        query.setParameter("entityIds", entityIds);
//        query.setParameter("statuses", statuses);
//        query.setFirstResult((int) pageable.getOffset());
//        query.setMaxResults(pageable.getPageSize());
//        List<Long> ids = query.getResultList();
//
//    }
}
