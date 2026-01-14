//package cn.sparrowmini.bpm.api.service;
//
//import cn.sparrowmini.bpm.api.ProcessService;
//import cn.sparrowmini.bpm.api.model.*;
//import cn.sparrowmini.bpm.api.model.Process;
//import cn.sparrowmini.bpm.api.repository.ProcessRepository;
//import jakarta.data.page.Page;
//import jakarta.data.page.PageRequest;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.inject.Inject;
//import jakarta.transaction.Transactional;
//
//import java.util.List;
//import java.util.Map;
//
//@ApplicationScoped
//public class ProcessServiceImpl implements ProcessService {
//
//    @Inject
//    ProcessRepository processRepository;
//
//    @Override
//    public Page<ProcessVlo> getProcessesByUser(PageRequest pageable, String filter, String username) {
//        return null;
//    }
//
//    @Override
//    public ProcessVdo getProcess(ProcessId processId) {
//        return null;
//    }
//
//    @Transactional
//    @Override
//    public void startProcess(ProcessId processId, Map<String, Object> variables) {
//        processRepository.save(new PublishedProcess());
//    }
//
//    @Override
//    public void stopProcess(ProcessId processId, String processInstanceId, String reason) {
//
//    }
//
//    @Override
//    public void publishProcess(List<Process> processes) {
//
//    }
//
//    @Override
//    public void unpublishProcess(List<ProcessId> processIds) {
//
//    }
//
//    @Override
//    public PermissionDto getProcessPermissions(ProcessId processId) {
//        return null;
//    }
//
//    @Override
//    public void grantProcessPermission(ProcessId processId, PermissionDto permission) {
//
//    }
//
//    @Override
//    public void removeProcessPermission(ProcessId processId, PermissionDto permission) {
//
//    }
//}
