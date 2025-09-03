package cn.sparrowmini.rules.util;

import cn.sparrowmini.rules.model.Dsl;
import cn.sparrowmini.rules.model.Dslr;
import lombok.extern.slf4j.Slf4j;
import org.drools.compiler.builder.impl.resources.DslrResourceHandler;
import org.drools.drl.parser.lang.dsl.DSLMappingEntry;
import org.drools.drl.parser.lang.dsl.DSLTokenizedMappingFile;
import org.drools.drl.parser.lang.dsl.DefaultExpander;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

@Slf4j
public class DSLExtractor {

    public static List<DSLMappingEntry> extractDSL(String dsl) {
        DSLTokenizedMappingFile dslFile = new DSLTokenizedMappingFile();
        boolean parsed = false;
        try {
            parsed = dslFile.parseAndLoad(
                    new InputStreamReader(new ByteArrayInputStream(dsl.getBytes())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!parsed) {
            throw new RuntimeException("DSL 文件解析失败");
        }

        List<DSLMappingEntry> entries = dslFile.getMapping().getEntries();
        for (DSLMappingEntry entry : entries) {
            System.out.println(
                    "[" + entry.getSection() + "] " + entry.getMappingKey()
            );
        }
        return entries;
    }

    public static String getDrl(Dsl dsl, List<Dslr> dslrs) {

        // 创建 DefaultExpander
        DefaultExpander expander = new DefaultExpander();


        // 1. 加载 DSL 文件
        DSLTokenizedMappingFile dslFile = new DSLTokenizedMappingFile();
        boolean parsed = false;
        try {
            parsed = dslFile.parseAndLoad(
                    new InputStreamReader(new ByteArrayInputStream(dsl.getContent().getBytes())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!parsed) {
            throw new RuntimeException("DSL 文件解析失败");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("rule \"auto_generated\"\n");
        sb.append("when\n");
        for (Dslr dslr : dslrs) {
            sb.append("    ").append(dslr.getName()).append("\n");
        }
        sb.append("then\n");
        for (Dslr dslr : dslrs) {
            sb.append("    ").append(dslr.getContent()).append("\n");
        }
        sb.append("end\n");

        String dslrText = sb.toString();
        log.info("拼接的DSLR:\n{}", dslrText);

// 交给 expander.expand 处理
        try {
            dslFile.getMapping().setOptions(List.of("usage", "result"));
            expander.addDSLMapping(dslFile.getMapping());
            String drl = dsl.getHead() + "\n" + expander.expand(new StringReader(dslrText));
            log.info("drl {}", drl);
            KieHelper kieHelper = new KieHelper();
            kieHelper.verify();
            kieHelper.addContent(drl, ResourceType.DRL);

            Results results = kieHelper.verify();

            if (results.hasMessages(Message.Level.ERROR)) {
                System.out.println("编译错误:");
                results.getMessages(Message.Level.ERROR).forEach(m -> System.out.println(m.getText()));
                throw new RuntimeException(results.getMessages(Message.Level.ERROR).stream().map(Message::getText).toList().toString());
            } else {
                System.out.println("DRL 编译通过，可以执行!");
            }
            return drl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
