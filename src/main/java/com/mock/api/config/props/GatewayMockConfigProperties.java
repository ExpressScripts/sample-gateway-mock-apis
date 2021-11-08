package com.mock.api.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "mockgateway")
public class GatewayMockConfigProperties {

    private String name;

    private String apiGatewayHost;
    private String mockServiceHost;

    private List<Api> apis;

    @Data
    public static class Api {
        private String mockName;
        private int order;
        private int delay;
        private boolean mockEnabled;
        private boolean responseFile;
        private String mockResponse;
        private int mockResponseStatus;
        private String endpointCheck;

        private MockResponseMapper mockResponseMapper;
    }

    @Data
    public static class MockResponseMapper {
        // body, param, path
        private String type;
        private String mapperKey;
        private String path;
    }
}
