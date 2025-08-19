package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.service.CommonJpaService;
import cn.sparrowmini.common.service.SimpleJpaFilter;
import cn.sparrowmini.common.util.JpaUtils;
import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("common-jpa")
public class CommonJpaController {

    @Autowired
    private CommonJpaService commonJpaService;

    @PostMapping("")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.CREATED)
    public ApiResponse<?> saveEntity(String className, @RequestBody List<Map<String, Object>> body) {
        Class<?> domainClass = null;
        try {
            domainClass = Class.forName(className);
            // 关键：构造一个 List<clazz> 的 JavaType
//            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        List<?> ids = commonJpaService.upsertEntity(domainClass, body);
        return new ApiResponse<>(ids);
    }

    @DeleteMapping("")
    @ResponseBody
    @Transactional
    public ApiResponse<Long> deleteEntity(String className, @RequestParam("id") String idsJson) {
        Class<?> domainClass = null;
        try {
            domainClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 解析 JSON
        ObjectMapper mapper = JsonUtils.getMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(idsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<Object> ids = new ArrayList<>();

        if (node.isArray()) {
            for (JsonNode element : node) {
                if (element.isValueNode()) {
                    // 基本值（string/number/boolean）
                    ids.add(mapper.convertValue(element, Object.class));
                } else if (element.isObject()) {
                    // 复合键
                    ids.add(mapper.convertValue(element, Map.class));
                } else {
                    ids.add(element.toString()); // 兜底
                }
            }
        } else {
            // 如果前端传的不是数组，就包一层，兼容
            if (node.isValueNode()) {
                ids.add(mapper.convertValue(node, Object.class));
            } else if (node.isObject()) {
                ids.add(mapper.convertValue(node, Map.class));
            }
        }
        return new ApiResponse<>(commonJpaService.deleteEntity(domainClass, ids));

    }

    @GetMapping("")
    @ResponseBody
    public Object getEntity(String className, @RequestParam("id") Object id) {
        try {
            Class<?> clazz = Class.forName(className);
            return commonJpaService.getEntity(clazz, id);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/filter")
    @ResponseBody
    public Page<?> getEntityList(String className, Pageable pageable, String filter) {
        try {
            Class<?> clazz = Class.forName(className);
            return commonJpaService.getEntityList(clazz, pageable, filter);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
