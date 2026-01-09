package cn.sparrowmini.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ZkViewConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 映射逻辑：访问 /common/user/xxx 会自动去找对应的视图
        // "**" 代表匹配多级目录
        registry.addViewController("/secure/**");
        registry.addViewController("/common/**");
        registry.addViewController("/admin/**");
    }
}