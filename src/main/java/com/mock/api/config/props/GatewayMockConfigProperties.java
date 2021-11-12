package com.mock.api.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "mockgateway")
public class GatewayMockConfigProperties {

    private String name;

    private String apiGatewayHost;
    private String mockServiceHost;

    private List<Api> apis;

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

        public String getMockName() {
            return mockName;
        }

        public void setMockName(String mockName) {
            this.mockName = mockName;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public boolean isMockEnabled() {
            return mockEnabled;
        }

        public void setMockEnabled(boolean mockEnabled) {
            this.mockEnabled = mockEnabled;
        }

        public boolean isResponseFile() {
            return responseFile;
        }

        public void setResponseFile(boolean responseFile) {
            this.responseFile = responseFile;
        }

        public String getMockResponse() {
            return mockResponse;
        }

        public void setMockResponse(String mockResponse) {
            this.mockResponse = mockResponse;
        }

        public int getMockResponseStatus() {
            return mockResponseStatus;
        }

        public void setMockResponseStatus(int mockResponseStatus) {
            this.mockResponseStatus = mockResponseStatus;
        }

        public String getEndpointCheck() {
            return endpointCheck;
        }

        public void setEndpointCheck(String endpointCheck) {
            this.endpointCheck = endpointCheck;
        }

        public MockResponseMapper getMockResponseMapper() {
            return mockResponseMapper;
        }

        public void setMockResponseMapper(MockResponseMapper mockResponseMapper) {
            this.mockResponseMapper = mockResponseMapper;
        }
    }

    public static class MockResponseMapper {
        // body, param, path
        private String type;
        private String mapperKey;
        private String path;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMapperKey() {
            return mapperKey;
        }

        public void setMapperKey(String mapperKey) {
            this.mapperKey = mapperKey;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiGatewayHost() {
        return apiGatewayHost;
    }

    public void setApiGatewayHost(String apiGatewayHost) {
        this.apiGatewayHost = apiGatewayHost;
    }

    public String getMockServiceHost() {
        return mockServiceHost;
    }

    public void setMockServiceHost(String mockServiceHost) {
        this.mockServiceHost = mockServiceHost;
    }

    public List<Api> getApis() {
        return apis;
    }

    public void setApis(List<Api> apis) {
        this.apis = apis;
    }
}
