package cn.sparrowmini.owl.jpa.service;

import cn.sparrowmini.owl.jpa.model.OwlClass;
import cn.sparrowmini.owl.jpa.model.OwlClassTree;
import cn.sparrowmini.owl.jpa.repository.OwlClassRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class OwlClassTreeService {

    @Resource
    private OwlClassRepository owlClassRepository;

    public OwlClassTree buildTree(String rootCode) {

        Map<String, OwlClass> allMap = new HashMap<>();
        for (OwlClass c : owlClassRepository.findAll()) {
            allMap.put(c.getCode(), c);
        }

        return buildSubTree(rootCode, allMap, new HashSet<>());
    }

    private OwlClassTree buildSubTree(
            String code,
            Map<String, OwlClass> allMap,
            Set<String> visited
    ) {
        // 防循环
        if (visited.contains(code)) return null;
        visited.add(code);

        // 过滤系统类
        if (isSystemClass(code)) return null;

        OwlClass c = allMap.get(code);
        if (c == null) return null;

        OwlClassTree node = new OwlClassTree();
        node.setCode(c.getCode());
        node.setLabel(c.getName());

        Set<String> childCodes = new HashSet<>();

        for (OwlClass child : allMap.values()) {

            if (code.equals(child.getParentId())) {

                // 🔥 去重
                if (!childCodes.add(child.getCode())) continue;

                OwlClassTree childNode = buildSubTree(
                        child.getCode(),
                        allMap,
                        visited
                );

                if (childNode != null) {
                    node.getChildren().add(childNode);
                }
            }
        }

        return node;
    }

//    public OwlClassTree buildTree(String rootCode) {
//        List<OwlClass> all = owlClassRepository.findAll();
//        Map<String, OwlClassTree> map = new HashMap<>();
//
//        // 1. 初始化所有节点，注意：先不设置 children
//        for (OwlClass c : all) {
//            // 🔥 加这里：过滤系统类
//            if (isSystemClass(c.getCode())) continue;
//
//            OwlClassTree node = new OwlClassTree();
//            node.setCode(c.getCode());
//            node.setLabel(c.getName());
//            // 确保 getChildren() 初始化为 ArrayList，避免空指针
//            map.put(c.getCode(), node);
//        }
//
//        OwlClassTree root = map.get(rootCode);
//        if (root == null || isSystemClass(rootCode)) return null;
//
//        // 2. 建立父子关系，并增加防循环检测
//        // 我们使用一个 Set 来记录已经分配了父节点的子节点，但这不足以防止深层循环
//        // 真正的防溢出需要在序列化或构建时检测路径
//        for (OwlClass c : all) {
//            if (c.getParentId() == null) continue;
//
//            // 防止自引用：A 的父类是 A
//            if (c.getCode().equals(c.getParentId())) continue;
//
//            OwlClassTree parent = map.get(c.getParentId());
//            OwlClassTree child = map.get(c.getCode());
//
//            if (parent != null && child != null) {
//                // 关键点：在添加子节点前，检查是否会造成循环
//                if (!isAncestor(child, parent)) {
//                    parent.getChildren().add(child);
//                }
//            }
//        }
//
//        return root;
//    }

    private boolean isSystemClass(String code) {
        return Set.of(
                "Thing", "Nothing", "Resource",
                "Property", "Class", "Ontology"
        ).contains(code);
    }

    /**
     * 判断 potentialAncestor 是否是 node 的子孙节点
     * 如果是，说明添加 node 为 potentialAncestor 的子节点会造成循环
     */
    private boolean isAncestor(OwlClassTree node, OwlClassTree potentialAncestor) {
        if (node == null || potentialAncestor == null) return false;
        if (node.getCode().equals(potentialAncestor.getCode())) return true;

        for (OwlClassTree child : node.getChildren()) {
            if (isAncestor(child, potentialAncestor)) {
                return true;
            }
        }
        return false;
    }
}