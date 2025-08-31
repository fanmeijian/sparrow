package cn.sparrowmini.rules.util;

import cn.sparrowmini.rules.model.Dslr;
import lombok.extern.slf4j.Slf4j;
import org.drools.compiler.builder.impl.resources.DslrResourceHandler;
import org.drools.drl.parser.lang.dsl.DSLMappingEntry;
import org.drools.drl.parser.lang.dsl.DSLTokenizedMappingFile;
import org.drools.drl.parser.lang.dsl.DefaultExpander;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public static String getDrl(String dsl, List<Dslr> dslrs){

        // 创建 DefaultExpander
        DefaultExpander expander = new DefaultExpander();

        // 1. 加载 DSL 文件
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
        String whenStr = "when \n";
        String thenStr = "then \n";
        for(Dslr dslr: dslrs){
            whenStr = whenStr + dslr.getName();
            thenStr = thenStr + dslr.getContent();
        }

        expander.addDSLMapping(dslFile.getMapping());
        final String dslr = String.join("\n", whenStr, thenStr, "end");
        log.info(dslr);

        // 2. 加载 DSLR 文件并应用映射
        InputStreamReader dslrReader = new InputStreamReader(new ByteArrayInputStream(dslr.getBytes()));
        try {
            return expander.expand(dslrReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
