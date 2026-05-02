package com.offermate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "langchain4j.qwen")
public class LangChain4jProperties {

    private String apiKey;

    private String baseUrl;

    private String modelName = "qwen-plus";

    private Double temperature = 0.7;

    private Integer maxTokens = 2000;
}
