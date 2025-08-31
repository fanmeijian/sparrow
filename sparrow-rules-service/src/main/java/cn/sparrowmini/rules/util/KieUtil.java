package cn.sparrowmini.rules.util;

import cn.sparrowmini.common.service.CommonJpaService;
import cn.sparrowmini.common.service.SpringContextUtil;
import cn.sparrowmini.rules.controller.RuleController;
import cn.sparrowmini.rules.repository.DrlRepository;
import cn.sparrowmini.rules.service.RulesService;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class KieUtil {
    private static final Map<String, KieContainer> kieContainerMap = new HashMap<>();

    /**
     * 从resource目录下获取文件
     * @param drlPath
     * @return
     */
    public static StatelessKieSession getKieSession(String drlPath) {
        if (!kieContainerMap.containsKey(drlPath)) {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            kieFileSystem.write(ResourceFactory.newClassPathResource(drlPath));
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
            KieModule kieModule = kieBuilder.getKieModule();
            kieContainerMap.put(drlPath,kieServices.newKieContainer(kieModule.getReleaseId()));
        }
        return kieContainerMap.get(drlPath).newStatelessKieSession();
    }

    /**
     * 从resource目录下获取文件
     * @param drlPath
     * @return
     */
    public static KieSession getKieSession_(String drlPath) {
        if (!kieContainerMap.containsKey(drlPath)) {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            kieFileSystem.write(ResourceFactory.newClassPathResource(drlPath));

            // Step 1: buildAll first
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();

            // Step 2: get results after build
            Results results = kieBuilder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                for (Message message : results.getMessages(Message.Level.ERROR)) {
                    System.err.println("Error: " + message.getText());
                }
                throw new IllegalStateException("DRL 文件存在编译错误");
            }

            KieModule kieModule = kieBuilder.getKieModule();
            kieContainerMap.put(drlPath, kieServices.newKieContainer(kieModule.getReleaseId()));
        }

        return kieContainerMap.get(drlPath).newKieSession();
    }

    public static KieSession getSessionByCode(String code) {
        DrlRepository rulesService = SpringContextUtil.getBean(DrlRepository.class);
        String drl = rulesService.findByCode(code).orElseThrow().getContent();
        return getKieSessionByStr_(drl);
    }

    /**
     * 直接输入drl格式字符串
     * @param drl
     * @return
     */
    public static KieSession getKieSessionByStr_(String drl) {
        // 使用 KieHelper 直接加载 DRL 字符串
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drl, ResourceType.DRL);

        // 构建 KieSession
        return kieHelper.build().newKieSession();
    }

    /**
     * 直接输入drl格式字符串
     * @param drl
     * @return
     */
    public static StatelessKieSession getKieSessionByStr(String drl) {
        // 使用 KieHelper 直接加载 DRL 字符串
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drl, ResourceType.DRL);

        // 构建 KieSession
        return kieHelper.build().newStatelessKieSession();
    }

    /**
     * excel格式
     * @param excel
     * @return
     */
    public static StatelessKieSession getKieSessionByExcel(InputStream excel) {
        // 使用 KieHelper 直接加载 DRL 字符串
        KieHelper kieHelper = new KieHelper();
        // 动态加载 Excel 文件内容
        // 从文件系统加载，或从类路径加载，取决于你的需求

        // 将 Excel 文件的内容添加为资源
        kieHelper.addResource(ResourceFactory.newInputStreamResource(excel), ResourceType.DTABLE);

        // 构建 KieSession
        return kieHelper.build().newStatelessKieSession();
    }

    /**
     * 从resource目录加载不同类型的资源
     * @param path
     * @param resourceType
     * @return
     */
    public static StatelessKieSession getKieSession(String path, ResourceType resourceType) {
        // 使用 KieHelper 直接加载 DRL 字符串
        KieHelper kieHelper = new KieHelper();
        // 动态加载 Excel 文件内容
        // 从文件系统加载，或从类路径加载，取决于你的需求
//        InputStream excel = KieContainerUtil.class.getResourceAsStream(path);
        ;
        // 将 Excel 文件的内容添加为资源
        kieHelper.addResource(ResourceFactory.newClassPathResource(path), resourceType);


        // 构建 KieSession
        return kieHelper.build().newStatelessKieSession();
    }


    /**
     * 动态将json转换为fact，也就是载drl中定义的factype，这样可以不用定义java类。
     * @param factType
     * @param dataMap
     * @param kieBase
     * @return
     * @throws Exception
     */
    public static Object buildDeclareObject(FactType factType, Map<String, Object> dataMap, KieBase kieBase) throws Exception {
        Object instance = factType.newInstance();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            FactField field = factType.getField(entry.getKey());
            if (field != null && entry.getValue() instanceof Map) {
                // 嵌套 declare 类型
                Class<?> nestedType = field.getType();
                String nestedTypeName = nestedType.getSimpleName();
                FactType nestedFactType = kieBase.getFactType("rules", nestedTypeName);
                Object nestedInstance = buildDeclareObject(nestedFactType, (Map<String, Object>) entry.getValue(), kieBase);
                factType.set(instance, entry.getKey(), nestedInstance);
            } else {
                factType.set(instance, entry.getKey(), entry.getValue());
            }
        }
        return instance;
    }
}
