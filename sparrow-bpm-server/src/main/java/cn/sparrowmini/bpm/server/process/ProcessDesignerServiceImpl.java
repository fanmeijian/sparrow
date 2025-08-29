package cn.sparrowmini.bpm.server.process;

import org.kie.api.definition.process.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProcessDesignerServiceImpl implements ProcessDesignerService {
    @Autowired
    private ContainerRepository containerRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private DynamicBpmnDeploymentService deploymentService;

    @Autowired
    private ProcessDesignRepository processDesignRepository;


    @Transactional
    @Override
    public void saveContainer(Container container) {
        Container containerRef=containerRepository.findById(container.getId()).orElse(container);
        containerRef.setName(container.getName());
        containerRef.setRemark(container.getRemark());
        this.containerRepository.save(container);
    }

    @Transactional
    @Override
    public void deleteContainer(Container.ContainerId containerId) {
        this.containerRepository.deleteById(containerId);
    }

    @Override
    public Page<Container> getContainerList(Pageable pageable) {
        return this.containerRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public void saveProcessDesign(ProcessDesign processDesign) {

        // 注册 BPMN 模块
        List<Process> processes = BpmnHelper.getProcesses(processDesign.getBpmnXml());
        for (Process process : processes) {
            ProcessDesign.ProcessDesignId processDesignId = new ProcessDesign.ProcessDesignId(
                    process.getId(),
                    process.getVersion(),
                    process.getPackageName()
            );
            processDesign.setId(processDesignId);
            processDesign.setProcessName(process.getName());
            processDesignRepository.save(processDesign);
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Override
    public Page<ProcessDesignView> getProcessDesignList(Container.ContainerId containerId,Pageable pageable) {
       return this.processDesignRepository.findByContainerId(containerId,pageable);
    }

    @Transactional
    @Override
    public ProcessDesign getProcessDesign(ProcessDesign.ProcessDesignId id) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            return em.find(ProcessDesign.class, id);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Transactional
    @Override
    public void deployProcessDesign(Container.ContainerId containerId) {
        Page<ProcessDesign> processDesigns = this.processDesignRepository.findByContainerId(containerId, Pageable.unpaged());
        List<String> svgs=new ArrayList<String>();
        List<String> bpmnXmlList = new ArrayList<>();
        processDesigns.stream().forEach(processDesign -> {
            svgs.add(processDesign.getSvg());
            bpmnXmlList.add(processDesign.getBpmnXml());
        });

        deploymentService.deploy(containerId, bpmnXmlList,svgs);

    }

    @Transactional
    @Override
    public void unDeployProcessDesign(String containerId) {
        deploymentService.unDeploy(containerId);
    }
}
