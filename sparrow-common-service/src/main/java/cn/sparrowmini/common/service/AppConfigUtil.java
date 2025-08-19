package cn.sparrowmini.common.service;

import cn.sparrowmini.common.repository.AppConfigRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class AppConfigUtil {
    public static List<Map<String, Object>> getAppConfig(String code) {
        AppConfigRepository appConfigRepository = SpringContextUtil.getBean(AppConfigRepository.class);
        return appConfigRepository.findById(code).orElseThrow().getConfigJson();
    }

    public static Map<String, Object> getAppConfigSingle(String code) {
        AppConfigRepository appConfigRepository = SpringContextUtil.getBean(AppConfigRepository.class);
        return appConfigRepository.findById(code).orElseThrow().getConfigJson().get(0);
    }

    public static <T> T getAppConfigObject(String code, TypeReference<T> toValueTypeRef) {
        return new ObjectMapper().convertValue(getAppConfig(code), toValueTypeRef);
    }

    public static <T> T getAppConfigSingleObject(String code, Class<T> clazz) {
        return new ObjectMapper().convertValue(getAppConfig(code), clazz);
    }
}
