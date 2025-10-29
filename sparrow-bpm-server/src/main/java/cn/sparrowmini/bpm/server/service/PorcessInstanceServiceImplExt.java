package cn.sparrowmini.bpm.server.service;

import cn.sparrowmini.bpm.server.common.*;
import cn.sparrowmini.bpm.server.process.ProcessInstanceLogRepository;
import cn.sparrowmini.bpm.server.process.VariableArchiveRepository;
import cn.sparrowmini.bpm.server.util.SparrowCriteriaBuilderHelper;
import cn.sparrowmini.bpm.server.util.SparrowJpaFilter;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog_;
import org.jbpm.services.api.*;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.jbpm.services.task.impl.model.*;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.query.QueryContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PorcessInstanceServiceImplExt implements PorcessInstanceServiceExt {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private VariableArchiveRepository varcharRepository;;

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private UserTaskService userTaskService;

    @Autowired
    private ProcessService processService;
    @Autowired
    private ProcessInstanceLogRepository processInstanceLogRepository;

    @Autowired
    private DeploymentService deploymentService;



    @Override
    public PageImpl<ProcessInstanceDesc> MyProcessInstances(Pageable pageable, List<SparrowJpaFilter> filters) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        EntityManager em = this.entityManagerFactory.createEntityManager();

        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<ProcessInstanceLog> criteriaQuery = cb.createQuery(ProcessInstanceLog.class);
            Root<ProcessInstanceLog> root = criteriaQuery.from(ProcessInstanceLog.class);
            Predicate predicate = cb.and(new SparrowCriteriaBuilderHelper<ProcessInstanceLog>(filters).toPredicate(root, criteriaQuery, cb), cb.equal(root.get(ProcessInstanceLog_.identity), username));

            criteriaQuery.select(root).where(predicate);
            Query query = em.createQuery(criteriaQuery);

            // count
            CriteriaQuery<Long> count = cb.createQuery(Long.class);
            count.select(cb.count(count.from(ProcessInstanceLog.class)));
            count.where(predicate);
            Long counta = em.createQuery(count).getSingleResult();

            List<ProcessInstanceLog> processInstanceLogs = query.setFirstResult(pageable.getPageIndex() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();

            return new PageImpl<ProcessInstanceDesc>(counta, processInstanceLogs.stream().map(m -> runtimeDataService.getProcessInstanceById(m.getProcessInstanceId())).collect(Collectors.toList()), pageable.getPageIndex(), pageable.getPageSize());

        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public PageImpl<SparrowTaskInstance> MyTasks(Pageable pageable, boolean withInput, boolean withOutput, List<SparrowJpaFilter> filters) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Set<String> entityIds = new HashSet<>();
        entityIds.add(username);
        auth.getAuthorities().forEach(f -> {
            entityIds.add(f.getAuthority());
        });

        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<TaskImpl> criteriaQuery = cb.createQuery(TaskImpl.class);
            Root<TaskImpl> root = criteriaQuery.from(TaskImpl.class);
            Join<TaskImpl, OrganizationalEntityImpl> join = root.join(TaskImpl_.peopleAssignments).join(PeopleAssignmentsImpl_.POTENTIAL_OWNERS);

            Predicate predicate = new SparrowCriteriaBuilderHelper<TaskImpl>(filters).toPredicate(root, criteriaQuery, cb);

            criteriaQuery.select(root).distinct(true).where(cb.and(predicate), join.get(OrganizationalEntityImpl_.id).in(entityIds)).orderBy(cb.desc(root.get(TaskImpl_.id)));
            Query query = em.createQuery(criteriaQuery);
            List<TaskImpl> tasks = query.setFirstResult(pageable.getPageIndex() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();


            // count
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<TaskImpl> countRoot = countQuery.from(TaskImpl.class);
            Join<TaskImpl, OrganizationalEntityImpl> countJoin = countRoot.join(TaskImpl_.peopleAssignments).join(PeopleAssignmentsImpl_.POTENTIAL_OWNERS);

            countQuery.select(cb.countDistinct(countRoot));
            countQuery.where(cb.and(predicate, countJoin.get(OrganizationalEntityImpl_.id).in(entityIds)));
            Long counta = em.createQuery(countQuery).getSingleResult();

            List<SparrowTaskInstance> taskInstances = tasks.stream().map(task -> {
                Long processInstanceId = task.getTaskData().getProcessInstanceId();
                SparrowTaskInstance taskInstance = new SparrowTaskInstance();
                taskInstance.setProcessInstanceId(task.getTaskData().getProcessInstanceId());
                taskInstance.setActualOwner(task.getTaskData().getActualOwner() == null ? null : task.getTaskData().getActualOwner().getId());
                taskInstance.setCreatedOn(task.getTaskData().getCreatedOn());
                taskInstance.setId(task.getId());
                taskInstance.setTaskName(task.getFormName());
                taskInstance.setDeploymentId(task.getTaskData().getDeploymentId());
                taskInstance.setPotentialOwners(task.getPeopleAssignments().getPotentialOwners().stream().map(OrganizationalEntity::getId).collect(Collectors.toList()));
                taskInstance.setProcessId(task.getTaskData().getProcessId());
                taskInstance.setStatus(task.getTaskData().getStatus().name());
                ProcessInstanceDesc processInstance = this.runtimeDataService.getProcessInstanceById(processInstanceId);
                Collection<VariableDesc> variableDescs = this.runtimeDataService.getVariableHistory(processInstanceId,"title", new QueryContext(0,1,"date",false));
                String title = String.join("","请审批",processInstance.getInitiator(),"提交的",processInstance.getProcessName());
                if(!variableDescs.isEmpty()){
                    title = variableDescs.stream().findFirst().get().getNewValue();
                }
                taskInstance.setTitle(title);
                taskInstance.setProcessName(processInstance.getProcessName());
                taskInstance.setName(task.getName());
                if (withInput) {
                    taskInstance.setInputData(this.userTaskService.getTaskInputContentByTaskId(task.getId()));
                }
                if (withOutput) {
                    taskInstance.setOutputData(this.userTaskService.getTaskOutputContentByTaskId(task.getId()));
                }
                return taskInstance;
            }).collect(Collectors.toList());

            return new PageImpl<SparrowTaskInstance>(counta, taskInstances, pageable.getPageIndex(), pageable.getPageSize());

        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }


    }

    @Override
    public List<SparrowTaskInstance> taskInstancesByProcess(long processInstanceId, boolean withInput, boolean withOutput) {

        return runtimeDataService.getTasksByProcessInstanceId(processInstanceId).stream().map(taskId -> {
            Task task = this.userTaskService.getTask(taskId);
            SparrowTaskInstance taskInstance = new SparrowTaskInstance();
            taskInstance.setProcessInstanceId(task.getTaskData().getProcessInstanceId());
            taskInstance.setActualOwner(task.getTaskData().getActualOwner() == null ? null : task.getTaskData().getActualOwner().getId());
            taskInstance.setCreatedOn(task.getTaskData().getCreatedOn());
            taskInstance.setActivationTime(task.getTaskData().getActivationTime());
            taskInstance.setId(task.getId());
            taskInstance.setTaskName(task.getFormName());
            taskInstance.setDeploymentId(task.getTaskData().getDeploymentId());
            taskInstance.setPotentialOwners(task.getPeopleAssignments().getPotentialOwners().stream().map(OrganizationalEntity::getId).collect(Collectors.toList()));
            taskInstance.setProcessId(task.getTaskData().getProcessId());
            taskInstance.setStatus(task.getTaskData().getStatus().name());
            ProcessInstanceDesc processInstance = this.runtimeDataService.getProcessInstanceById(task.getTaskData().getProcessInstanceId());
            taskInstance.setProcessName(processInstance.getProcessName());
            taskInstance.setName(task.getName());
            if(withInput){
                taskInstance.setInputData(this.userTaskService.getTaskInputContentByTaskId(task.getId()));
            }
            if(withOutput){
                taskInstance.setOutputData(this.userTaskService.getTaskOutputContentByTaskId(task.getId()));
            }

            return taskInstance;
        }).collect(Collectors.toList());

    }

    @Transactional
    @Override
    public String saveProcessAsDraft(String deploymentId, String processId, Map<String, Object> body) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            ProcessDraft processDraft = new ProcessDraft(deploymentId, processId, body);
            em.persist(processDraft);
            return "{\"id\":\"" + processDraft.getId() + "\"}";
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }

    }

    @Override
    public ProcessDraft getProcessDraft(String id) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        return em.find(ProcessDraft.class, id);
    }

    @Override
    public PageImpl<ProcessDraft> getProcessDraftList(String deploymentId, String processId, boolean withInput, List<String> variableName, Pageable pageable) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProcessDraft> criteriaQuery = cb.createQuery(ProcessDraft.class);
        Root<ProcessDraft> root = criteriaQuery.from(ProcessDraft.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isNull(root.get("processInstanceId")));
        if (deploymentId != null) {
            predicates.add(cb.equal(root.get("deploymentId"), deploymentId));
        }
        if (processId != null) {
            predicates.add(cb.equal(root.get("processId"), processId));
        }
        criteriaQuery.select(root).distinct(true).where(cb.and(predicates.toArray(new Predicate[]{}))).orderBy(cb.desc(root.get("createdDate")));
        Query query = em.createQuery(criteriaQuery);
        List<ProcessDraft> processDrafts = query.setFirstResult(pageable.getPageIndex() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProcessDraft> countRoot = countQuery.from(ProcessDraft.class);

        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(cb.and(predicates.toArray(new Predicate[]{})));
        Long count = em.createQuery(countQuery).getSingleResult();

        if (!processDrafts.isEmpty()) {
            if (withInput) {
                if (variableName != null) {
                    Set<String> aa = processDrafts.get(0).getProcessData().keySet().stream().filter(f -> variableName.stream().noneMatch(a -> a.equals(f))).collect(Collectors.toSet());
                    processDrafts.forEach(f -> {
                        aa.forEach(a -> f.getProcessData().remove(a));
                    });
                }
            } else {

                processDrafts.forEach(f -> f.setProcessData(null));
            }
        }


        return new PageImpl<ProcessDraft>(count, processDrafts, pageable.getPageIndex(), pageable.getPageSize());
    }

    @Transactional
    @Override
    public void submitProcess(String id, Map<String, Object> body) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            Authentication a11 = SecurityContextHolder.getContext().getAuthentication();
            String username = a11.getName();
            Query queryCount = em.createQuery("select count(*) from ProcessDraft where id=:id and createdBy=:username");
            queryCount.setParameter("id", id);
            queryCount.setParameter("username", username);
            boolean isAuthor = queryCount.executeUpdate() > 0;

            if (username.equalsIgnoreCase("SUPER_SYSADMIN")
                    || a11.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPER_SYSADMIN")) || isAuthor) {
                ProcessDraft processDraft = em.find(ProcessDraft.class, id);
                Long processInstanceId = this.processService.startProcess(processDraft.getDeploymentId(), processDraft.getProcessId(), body);
                Query query = em.createQuery("delete from ProcessDraft where id = :id");
                query.setParameter("id", id);
                query.executeUpdate();
            }
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }


    }

    @Override
    public Map<String, Object> startProcess(String deploymentId, String processId, Map<String, Object> body) {
//        final KieServices kieServices = KieServices.Factory.get();
//        kieServices.getKieClasspathContainer().getKieBase().getKieSessions().forEach(kieSession -> {
//            kieSession.setGlobal("kieServices", kieServices);
//        });

        Authentication a11 = SecurityContextHolder.getContext().getAuthentication();
        String username = a11.getName();
        body.put("_initiator", username);
        Long id = this.processService.startProcess(deploymentId, processId, body);
        return Map.of("id", id);
    }

    @Override
    public void completeTask(Long taskId, Map<String, Object> body) {
        Authentication a11 = SecurityContextHolder.getContext().getAuthentication();
        String username = a11.getName();
        this.userTaskService.completeAutoProgress(taskId, username, body);
    }

    @Override
    public void addTaskComment(Long taskId, String comment) {
        Authentication a11 = SecurityContextHolder.getContext().getAuthentication();
        String username = a11.getName();
        this.userTaskService.addComment(taskId, comment, username, new Date());
    }

    @Override
    public List<TaskCommentBean> getTaskComments(List<Long> taskId) {
        List<TaskCommentBean> taskCommentBeans = new ArrayList<>();
        taskId.stream().forEach(id -> {
            Task task = this.userTaskService.getTask(id);
            List<Comment> comments = this.userTaskService.getCommentsByTaskId(id);
            comments.forEach(comment -> {
                TaskCommentBean taskCommentBean = new TaskCommentBean(comment.getId(), task.getId(), task.getName(), comment.getAddedBy().getId(), comment.getAddedAt(), comment.getText());
                taskCommentBeans.add(taskCommentBean);
            });
        });

        return taskCommentBeans;
    }

    @Override
    public List<TaskCommentBean> getTaskCommentsByProcessInstanceId(Long id) {
        return this.getTaskComments(runtimeDataService.getTasksByProcessInstanceId(id));
    }

    @Transactional
    @Override
    public void deleteDraft(Set<String> ids) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            Authentication a11 = SecurityContextHolder.getContext().getAuthentication();
            String username = a11.getName();
            if (username.equalsIgnoreCase("SUPER_SYSADMIN")
                    || a11.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPER_SYSADMIN"))) {
                Query query = em.createQuery("delete from ProcessDraft where id in (:ids)");
                query.setParameter("ids", ids);
                query.executeUpdate();
            } else {
                Query query = em.createQuery("delete from ProcessDraft where id in (:ids) and createdBy=:username");
                query.setParameter("ids", ids);
                query.setParameter("username", username);
                query.executeUpdate();
            }

        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Map<String, Object> getProcessVariables(VariableArchive.VariableArchiveId id) {

        ProcessInstanceLog processInstanceLog = processInstanceLogRepository.findProcessInstance(id.getDeploymentId(),id.getProcessId(), id.getProcessInstanceId()).orElseThrow();
        if (processInstanceLog.getStatus() == ProcessInstance.STATE_COMPLETED
                || processInstanceLog.getStatus() == ProcessInstance.STATE_ABORTED
        ) {
            VariableArchive variableArchive = varcharRepository.findById(id).orElse(null);
            if(variableArchive!=null){
               return variableArchive.getVariableJson();
            }else{
                return new HashMap<>();
            }

        } else {
            return this.processService.getProcessInstanceVariables(id.getDeploymentId(), id.getProcessInstanceId());
        }


    }
}
