package cn.sparrowmini.bpm.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvel2.MVEL;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("mvel")
public class MvelUtils {

    /**
     * 在 MVEL 中对 list 执行表达式
     *
     * @param list       要操作的列表
     * @param expression MVEL 表达式，比如 "foreach(c : list) { c.put('stat','CONFIRMED') }"
     */
    public void eval(String expression, List<?> ...list) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("list", list);
        MVEL.eval(expression, vars);
    }

    public <T> T copy(Object source, Class<T> clazz) {
        ObjectMapper objectMapper = JsonUtils.getMapper();
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(source);
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
