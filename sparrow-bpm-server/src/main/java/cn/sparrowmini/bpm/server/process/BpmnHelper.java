package cn.sparrowmini.bpm.server.process;

import org.drools.core.xml.SemanticModules;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.process.svg.processor.SVGProcessor;
import org.kie.api.definition.process.Node;
import org.jbpm.workflow.core.WorkflowProcess;
import org.kie.api.definition.process.Process;
import org.jbpm.workflow.core.node.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jbpm.process.svg.processor.SVGProcessorFactory;


public class BpmnHelper {

    public static List<Process> getProcesses(String bpmnXml) {
        // 注册 BPMN 模块
        SemanticModules modules = new SemanticModules();
        modules.addSemanticModule(new BPMNSemanticModule());
        XmlProcessReader reader = new XmlProcessReader(
                modules,
                Thread.currentThread().getContextClassLoader()
        );

        List<Process> processes = null;
        try {
            processes = reader.read(new StringReader(bpmnXml));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        return processes;
    }

    public static Map<String, List<HumanTaskNode>> parseBpmnTasks(List<Process> processes) {
        Map<String, List<HumanTaskNode>> map = new HashMap<>();
        for (Process process : processes) {
            if (process instanceof WorkflowProcess) {
                map.putIfAbsent(process.getId(), new ArrayList<>());
                List<HumanTaskNode> tasks = map.get(process.getId());
                WorkflowProcess wfProcess = (WorkflowProcess) process;
                tasks.addAll(traverseNodes(wfProcess.getNodes()));
            }
        }
        return map;
    }

    private static List<HumanTaskNode> traverseNodes(Node[] nodes) {
        List<HumanTaskNode> tasks = new ArrayList<>();
        for (Node node : nodes) {
            HumanTaskNode task = extractTask(node);
            if (task != null) {
                tasks.add(task);
            }

        }
        return tasks;
    }

    private static HumanTaskNode extractTask(Node node) {
        if (node instanceof HumanTaskNode) {
            HumanTaskNode humanTask = (HumanTaskNode) node;
            System.out.println("用户任务: " + humanTask.getName());
            return humanTask;
        } else if (node instanceof WorkItemNode) {
            WorkItemNode workItemNode = (WorkItemNode) node;
            System.out.println("服务任务: " + workItemNode.getWork().getName());
        } else if (node instanceof ActionNode) {
            ActionNode actionNode = (ActionNode) node;
            System.out.println("脚本任务: " + actionNode.getName());
        } else if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;
            System.out.println("进入子流程节点: " + compositeNode.getName());
            traverseNodes(compositeNode.getNodes()); // ✅ 递归子流程
        } else if (node instanceof SubProcessNode) {
            SubProcessNode subProcessNode = (SubProcessNode) node;
            System.out.println("调用子流程（SubProcess）: " + subProcessNode.getProcessId());
            // 注意：这是调用外部流程的引用（不能递归内部节点），可另外加载该流程并递归
        } else {
            System.out.println("其他节点: " + node.getName() + " 类型: " + node.getClass().getSimpleName());
        }
        return null;
    }
}
