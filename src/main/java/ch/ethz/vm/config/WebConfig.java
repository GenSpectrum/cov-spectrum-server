package ch.ethz.vm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    Logger logger = LoggerFactory.getLogger(WebConfig.class);


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if ("true".equals(System.getenv("VM_DEVELOPMENT"))) {
            logger.info("Server is running in development mode. CORS requests are enabled.");
            registry.addMapping("/**");
        }
    }
}
