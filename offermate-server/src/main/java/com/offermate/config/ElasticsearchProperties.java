package com.offermate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {

    private String uris;

    private String host;

    private Integer port;

    private String scheme;

    private String username;

    private String password;
}
