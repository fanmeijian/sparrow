package cn.sparrowmini.rules.util;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.api.KieBase;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieSession;

import java.util.Map;

public class DroolsHelper {


    /**
     * 动态通过map 来创建fact
     * @param kieSession
     * @param factName
     * @param factData
     * @return
     */
    public static Object createFact(KieSession kieSession, String factName, Map<String, Object> factData) {
        ObjectMapper objectMapper = JsonUtils.getMapper();
        int lastDotIndex = factName.lastIndexOf('.');
        String packageName = factName.substring(0, lastDotIndex);
        String className = factName.substring(lastDotIndex + 1);

        FactType factType = kieSession.getKieBase().getFactType(packageName, className);
        if (factType == null) {
            throw new IllegalArgumentException("未找到 FactType：" + factName);
        }

        Object factInstance;
        try {
            factInstance = factType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("创建 Fact 实例失败", e);
        }

        // 通过 ObjectMapper 填充属性
        try {
            objectMapper.updateValue(factInstance, factData);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        return factInstance;
    }

    public Object buildDeclareObject(String factName, Map<String, Object> dataMap, KieSession kieSession) throws Exception {
        ObjectMapper objectMapper = JsonUtils.getMapper();
        int lastDotIndex = factName.lastIndexOf('.');
        String packageName = factName.substring(0, lastDotIndex);
        String className = factName.substring(lastDotIndex + 1);
        KieBase kieBase = kieSession.getKieBase();
        FactType factType = kieSession.getKieBase().getFactType(packageName, className);
        if (factType == null) {
            throw new IllegalArgumentException("未找到 FactType：" + factName);
        }
        Object instance = factType.newInstance();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            FactField field = factType.getField(entry.getKey());
            if (field != null && entry.getValue() instanceof Map) {
                // 嵌套 declare 类型
                Class<?> nestedType = field.getType();
                String nestedTypeName = nestedType.getSimpleName();
                FactType nestedFactType = kieBase.getFactType(packageName, nestedTypeName);
                Object nestedInstance = buildDeclareObject(nestedFactType.getFactClass().getName(), (Map<String, Object>) entry.getValue(), kieSession);
                factType.set(instance, entry.getKey(), nestedInstance);
            } else {
                factType.set(instance, entry.getKey(), entry.getValue());
            }
        }
        return instance;
    }
}
