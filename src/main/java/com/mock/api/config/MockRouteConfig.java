package com.mock.api.config;

import com.mock.api.config.props.GatewayMockConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@ConditionalOnProperty("com.mock.enabled")
public class MockRouteConfig {

    public static final String IS_RESPONSE_FILE = "IS_RESPONSE_FILE";
    public static final String MOCK_RESPONSE = "MOCK_RESPONSE";
    public static final String MOCK_CONFIG_NAME = "MOCK_CONFIG_NAME";
    public static final String MOCK_RESPONSE_STATUS = "MOCK_RESPONSE_STATUS";

    private final Logger log = LoggerFactory.getLogger(MockRouteConfig.class);

    @Bean
    public RouteLocator defaultRouteForApiGateway(
            RouteLocatorBuilder builder,
            GatewayMockConfigProperties mockConfigProperties
    ) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        log.info("MockConfiguration: " + mockConfigProperties.toString());
        final URI uri = URI.create(mockConfigProperties.getApiGatewayHost());
        routes
                .route("defaultApiRoute", p -> {
                    p.method("GET", "POST");
                    p.order(3);
                    return p.uri(uri);
                });

        return routes.build();
    }

    @Bean
    public RouteLocator routesForMock(
            RouteLocatorBuilder builder,
            GatewayMockConfigProperties mockConfiguration
    ) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        log.info("routesForMock logging mockConfiguration: " + mockConfiguration.toString());
        mockConfiguration.getApis().forEach(service -> {
            if (service.isMockEnabled()) {
                final URI mockRoute = URI.create(mockConfiguration.getMockServiceHost() != null
                        ? mockConfiguration.getMockServiceHost()
                        : mockConfiguration.getApiGatewayHost());
                routes
                        .route(service.getMockName(), p -> {
                            log.info("Processing mock route: " + service.getMockName());
                            p.path(service.getEndpointCheck())
                                .filters(f -> {
                                    f.addRequestHeader("MockGateway", "Mock Gateway (MockHeader)");
                                    f.addRequestHeader(IS_RESPONSE_FILE, Boolean.toString(service.isResponseFile()));
                                    f.addRequestHeader(MOCK_RESPONSE, service.getMockResponse());
                                    f.addRequestHeader(MOCK_CONFIG_NAME, service.getMockName());
                                    f.addRequestHeader(MOCK_RESPONSE_STATUS, Integer.toString(service.getMockResponseStatus()));
                                    f.rewritePath("/(?<segment>.*)", "/mockgateway?originalEndpoint=${segment}");
                                    return f;
                                });
                            p.order(service.getOrder());
                            return p.uri(mockRoute);
                        });
            }
        });

        return routes.build();
    }

}
