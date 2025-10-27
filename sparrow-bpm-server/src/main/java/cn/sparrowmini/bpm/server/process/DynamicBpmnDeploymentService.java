package cn.sparrowmini.bpm.server.process;

import lombok.extern.slf4j.Slf4j;
import org.appformer.maven.integration.MavenRepository;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.mvel.builder.MVELDialect;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.definition.process.Process;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorImpl;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.impl.KieServerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

@Slf4j
@Service
public class DynamicBpmnDeploymentService {

    @Inject
    private DeploymentService deploymentService;

    @Autowired
    private CommonJpaService commonJpaService;


    private final KieServices kieServices = KieServices.Factory.get();
    private KieServerImpl kieServer;
    @Autowired
    private KieServer kieServer_;

    @PostConstruct
    public void init() {
        if (kieServer_ instanceof KieServerImpl) {
            this.kieServer = (KieServerImpl) kieServer_;
        }
    }

    public void unDeploy(String containerId) {
        DeployedUnit existing = deploymentService.getDeployedUnit(containerId);
        if (existing != null) {
            this.kieServer.disposeContainer(containerId, false);
        }
        this.kieServer.disposeContainer(containerId, false);
    }

    /**
     * 动态部署 BPMN 字符串并注册为 KIE 容器
     */
    public void deploy(Container.ContainerId containerId, List<String> bpmnXmls, List<String> svgs) {
        try {
            // 1. 构建 ReleaseId

            String artifactId = containerId.getArtifactId().replace("-", "_");
            String groupId = containerId.getGroupId();
            String version = containerId.getVersion();
            ReleaseId releaseId = kieServices.newReleaseId(groupId, artifactId, version);
            String containerId_ = String.join("-", releaseId.getArtifactId(), releaseId.getVersion());
            // 2. 构建 KieFileSystem
            KieFileSystem kfs = kieServices.newKieFileSystem();
            // 注册 BPMN 模块
            bpmnXmls.forEach(bpmnXml -> {
                List<Process> processes = BpmnHelper.getProcesses(bpmnXml);
                for (Process process : processes) {
                    kfs.write("src/main/resources/" + process.getId() + ".bpmn2", bpmnXml);
                    kfs.write("src/main/resources/" + process.getId() + "-svg.svg", svgs.get(bpmnXmls.indexOf(bpmnXml)));
                }
            });
            String drl =
                    String.join("\n",
                            "package rules;",
                            "global cn.sparrowmini.bpm.server.process.GlobalVariableMap global;",
                            "global cn.sparrowmini.bpm.server.util.MvelUtils mvel;",
                            "rule R1 when"
                            , "then",
                            "    System.out.println(\"Meta user = \" + mvel);",
                            "end");

            kfs.write("src/main/resources/global.drl", drl);
            kfs.write("src/main/resources/META-INF/kie-deployment-descriptor.xml", getDeploymentXml(containerId_).toXml());
//            kfs.write("src/main/resources/META-INF/deployment-descriptor.xml", getDeploymentXml().toXml());
            kfs.write("src/main/resources/META-INF/kmodule.xml", getKModuleXml());
            kfs.generateAndWritePomXML(releaseId);
            kfs.writeKModuleXML(getKModuleXml());
//            MVELProcessDialect.setActionbuilder(new MyMVELActionBuilder());
            // 编译前修改流程对象
            // 3. 编译并构建 InternalKieModule
            KieBuilder builder = kieServices.newKieBuilder(kfs);
            builder.buildAll();
            Results results = builder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                throw new RuntimeException("构建失败: " + results.getMessages());
            }
            InternalKieModule kieModule = (InternalKieModule) ((KieBuilderImpl) builder).getKieModule();

            // 4. 安装进本地 Maven 仓库
            MavenRepository repository = MavenRepository.getMavenRepository();
            repository.installArtifact(releaseId, kieModule.getBytes(), kfs.read("pom.xml"));

            // 5. 用 DeploymentService 注册
            DeploymentUnit unit = new KModuleDeploymentUnit(
                    releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion()
            );
            MVELDialect mveldialect = new MVELDialect();
            // 6. 热更新容器（不删除，不新建）

//            DeployedUnit existing = deploymentService.getDeployedUnit(containerId_);
//            if (existing != null) {
//                this.kieServer.disposeContainer(containerId_, false);
//            }
//            this.kieServer.disposeContainer(containerId_, false);
//            this.kieServer.createContainer(containerId_, new KieContainerResource(new org.kie.server.api.model.ReleaseId(releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion())));
            DeployedUnit existing = deploymentService.getDeployedUnit(containerId_);
            if (existing != null) {
                try {
                    // 1. 从 DeploymentService 卸载
                    deploymentService.undeploy(existing.getDeploymentUnit());
                    // 2. 确保 RuntimeManager 已被释放
                    deploymentService.getRuntimeManager(containerId_); // 确认状态
                } catch (Exception e) {
                    log.warn("旧部署卸载失败: {}", e.getMessage());
                }
            }

// 3. 同步清理掉 KieServer 中的容器
            try {
                kieServer.disposeContainer(containerId_, false);
            } catch (Exception e) {
                log.warn("KieServer 容器清理失败: {}", e.getMessage());
            }

// 4. 创建新容器
            kieServer.createContainer(
                    containerId_,
                    new KieContainerResource(
                            new org.kie.server.api.model.ReleaseId(
                                    releaseId.getGroupId(),
                                    releaseId.getArtifactId(),
                                    releaseId.getVersion()
                            )
                    )
            );

        } catch (Exception e) {
            throw new RuntimeException("部署 BPMN 流程失败", e);
        }
    }

    /**
     * KModule XML 模板
     */
    private String getKModuleXml() {
        return "<kmodule xmlns=\"http://www.drools.org/xsd/kmodule\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>";
    }

    private DeploymentDescriptor getDeploymentXml(String containerId) {
        DeploymentDescriptor descriptor = new DeploymentDescriptorImpl("org.jbpm.domain");

// 设置 runtime 策略
        descriptor.getBuilder().runtimeStrategy(RuntimeStrategy.PER_PROCESS_INSTANCE);

// 注册全局变量
        descriptor.getBuilder().addGlobal(new NamedObjectModel("spring", "mvel", "mvel"));
        descriptor.getBuilder().addGlobal(new NamedObjectModel("mvel", "global", "new cn.sparrowmini.bpm.server.process.GlobalVariableMap(\"" + containerId + "\")"));

        return descriptor;
    }
}