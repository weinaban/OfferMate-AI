package com.offermate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.dashscope")
public class AiProperties {

    private String apiKey;

    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private Chat chat = new Chat();

    @Data
    public static class Chat {

        private Options options = new Options();
    }

    @Data
    public static class Options {

        private String model = "qwen-plus";

        private Double temperature = 0.7;
    }
}
