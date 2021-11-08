package com.mock.api.mock.controller;

import com.mock.api.config.props.GatewayMockConfigProperties;
import com.mock.api.config.props.MockResponseConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mock.api.config.MockRouteConfig.*;
import static java.nio.charset.Charset.defaultCharset;

@RestController
@RequestMapping("/mockgateway")
@Slf4j
public class LocalMockController {

    private Optional configLabel;
    private final GatewayMockConfigProperties mockConfiguration;
    private final MockResponseConfigProperties mockResponseConfig;

    public LocalMockController(
            GatewayMockConfigProperties mockConfiguration,
            MockResponseConfigProperties mockResponseConfig,
            @Value("${spring.cloud.config.label:}") Optional configLabel
    ) {
        this.mockConfiguration = mockConfiguration;
        this.mockResponseConfig = mockResponseConfig;
        this.configLabel = configLabel;
    }

    @GetMapping
    public ResponseEntity<Mono<String>> justMockResponse(
            @RequestParam("originalEndpoint") String originalEndpoint,
            @RequestHeader(IS_RESPONSE_FILE) Optional<Boolean> isResponseFile,
            @RequestHeader(MOCK_RESPONSE) Optional<String> mockResponse,
            @RequestHeader(MOCK_RESPONSE_STATUS) Optional<Integer> mockResponseStatus,
            @RequestHeader(MOCK_CONFIG_NAME) Optional<String> mockConfigName,
            @RequestParam Map<String, String> requestParams
    ) {
        log.info("GET params: " + requestParams);
        String mapperValue = getMapperValueIfApplicable(originalEndpoint, mockConfigName, requestParams);
        return getMonoResponseEntity(originalEndpoint, isResponseFile, mockResponse, mockResponseStatus, mapperValue, mockConfigName);
    }

    @PostMapping
    public ResponseEntity<Mono<String>> justMockResponsePost(
            @RequestParam("originalEndpoint") String originalEndpoint,
            @RequestHeader(IS_RESPONSE_FILE) Optional<Boolean> isResponseFile,
            @RequestHeader(MOCK_RESPONSE) Optional<String> mockResponse,
            @RequestHeader(MOCK_RESPONSE_STATUS) Optional<Integer> mockResponseStatus,
            @RequestHeader(MOCK_CONFIG_NAME) Optional<String> mockConfigName,
            @RequestParam Map<String, String> requestParams,
            @RequestBody Map<String, String> params
    ) {
        log.info("POST params: " + params);
        String mapperValue = getMapperValueIfApplicable(originalEndpoint, mockConfigName, params);
        return getMonoResponseEntity(originalEndpoint, isResponseFile, mockResponse, mockResponseStatus, mapperValue, mockConfigName);
    }

    private ResponseEntity<Mono<String>> getMonoResponseEntity(
            String originalEndpoint,
            Optional<Boolean> isResponseFile,
            Optional<String> mockResponse,
            Optional<Integer> mockResponseStatus,
            String mapperValue,
            Optional<String> mockConfigName) {
        String responseString = "";
        String mockConfigId = mockConfigName.orElse(null);
        String responseKey = mockResponse.orElse(null);
        String isResponseFileKey = !StringUtils.isEmpty(mapperValue)
                ? mockConfigId + "_" + mapperValue + "_responseFile"
                : null;
        Boolean isResponseFileCalculated = isResponseFileKey != null
                && !StringUtils.isEmpty(mockResponseConfig.getResponse().get(isResponseFileKey))
                ? Boolean.valueOf(mockResponseConfig.getResponse().get(isResponseFileKey))
                : isResponseFile.orElse(Boolean.FALSE);

        Duration delay = getDelay(mockConfigId);

        if (!isResponseFileCalculated) {
            responseString = getResponseStringFromKey(mapperValue, mockConfigId, responseKey);
        } else {
            // Get file data Step:
            String calculatedResponseKey = !StringUtils.isEmpty(mapperValue)
                    ? mockConfigId + "_" + mapperValue + "_mockResponse"
                    : null;
            String responseKeyCalculated = calculatedResponseKey != null
                    && mockResponseConfig.getResponse().get(calculatedResponseKey) != null
                    ? mockResponseConfig.getResponse().get(calculatedResponseKey)
                    : responseKey;
            String responsePath = configLabel.isPresent() && !StringUtils.isEmpty(configLabel.get())
                    ? "/" + configLabel.get() + responseKeyCalculated
                    : responseKeyCalculated;
            String defaultResponsePath = responsePath;
            // Add mapper value to file response path
            if (!StringUtils.isEmpty(mapperValue)) {
                int lastFileSeparatorIndex = responsePath.lastIndexOf("/") + 1;
                responsePath = responsePath.substring(0, lastFileSeparatorIndex)
                        + mapperValue + "_" + responsePath.substring(lastFileSeparatorIndex);
            }
            try {
                // Check if file response exists with mapper value
                boolean isResponseFileExistsWithMapperValue = new ClassPathResource(responsePath).exists();
                log.info("ConfigName: " + mockConfigId
                        + ", mapperValue: " + mapperValue
                        + ", Using default response: " + !isResponseFileExistsWithMapperValue);
                responseString = isResponseFileExistsWithMapperValue
                        ? StreamUtils.copyToString(new ClassPathResource(responsePath).getInputStream(), defaultCharset())
                        : StreamUtils.copyToString(new ClassPathResource(defaultResponsePath).getInputStream(), defaultCharset());
            } catch (Exception e) {
                log.error("Unable to read response resource."
                        + " Endpoint: " + originalEndpoint
                        + ", mockResponse: " + mockResponse, e);
            }
        }

        // calculate http response status
        String mapperResponseStatus = !StringUtils.isEmpty(mapperValue)
                ? mockConfigId + "_" + mapperValue + "_httpStatus"
                : null;
        int calculatedResponseStatus = mapperResponseStatus != null
                && !StringUtils.isEmpty(mockResponseConfig.getResponse().get(mapperResponseStatus))
                ? Integer.parseInt(mockResponseConfig.getResponse().get(mapperResponseStatus))
                : mockResponseStatus.orElse(200);
        HttpStatus status = HttpStatus.resolve(calculatedResponseStatus);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("content-type", "application/json");

        return ResponseEntity.status(status)
                .headers(responseHeaders)
                .body(Mono.just(responseString).delayElement(delay));
    }

    private String getResponseStringFromKey(String mapperValue, String mockConfigId, String responseKey) {
        String responseString;
        String mapperResponseKey = !StringUtils.isEmpty(mapperValue)
                ? mockConfigId + "_" + mapperValue + "_mockResponse"
                : responseKey;
        responseString = !StringUtils.isEmpty(mockResponseConfig.getResponse().get(mapperResponseKey))
                ? mockResponseConfig.getResponse().get(mapperResponseKey)
                : mockResponseConfig.getResponse().get(responseKey);
        return responseString;
    }

    private Duration getDelay(String mockConfigId) {
        Duration delay = Duration.ZERO;
        Optional<GatewayMockConfigProperties.Api> serviceConfig = getServiceConfig(Optional.of(mockConfigId));
        if (serviceConfig.isPresent() && serviceConfig.get().getDelay() > 0) {
            int delayInSec = serviceConfig.get().getDelay();
            delay = Duration.ofSeconds(delayInSec);
        }
        return delay;
    }

    private String getMapperValueIfApplicable(String originalEndpoint,
                                              Optional<String> mockCongigName,
                                              Map<String, String> params) {
        String mapperValue = null;
        Optional<GatewayMockConfigProperties.Api> serviceConfig = getServiceConfig(mockCongigName);
        if (serviceConfig.isPresent() && serviceConfig.get().getMockResponseMapper() != null) {
            GatewayMockConfigProperties.MockResponseMapper mockResponseMapper = serviceConfig.get().getMockResponseMapper();
            if (mockResponseMapper.getType().equals("param") && originalEndpoint.contains("?")) {
                Map<String, String> oParams = new HashMap<>();
                String paramString = originalEndpoint.substring(originalEndpoint.indexOf("?") + 1);
                String[] split = paramString.split("&");
                Arrays.asList(split).stream().forEach(s -> {
                    String[] pKeyValue = s.split("=");
                    oParams.put(pKeyValue[0], pKeyValue[1]);
                });
                mapperValue = params.containsKey(mockResponseMapper.getMapperKey())
                        ? params.get(mockResponseMapper.getMapperKey())
                        : oParams.get(mockResponseMapper.getMapperKey());
            } else if (mockResponseMapper.getType().equals("body")) {
                mapperValue = params.get(mockResponseMapper.getMapperKey());
            } else if (mockResponseMapper.getType().equals("path")) {
                mapperValue = getPathParamValue(mockResponseMapper.getMapperKey(), originalEndpoint);
            }
            log.info("Found mapper key: " + mapperValue);
        }
        return mapperValue;
    }

    private Optional<GatewayMockConfigProperties.Api> getServiceConfig(Optional<String> mockCongigName) {
        return mockConfiguration.getApis()
                .stream()
                .filter(s -> s.getMockName().equals(mockCongigName.get()))
                .findFirst();
    }

    private String getPathParamValue(String pathParamKey, String url) {
        String regex = pathParamKey.substring(0, pathParamKey.indexOf("{")) + "(.*)" +
                (pathParamKey.indexOf("}") + 1 == pathParamKey.length()
                        ? ""
                        : pathParamKey.substring(pathParamKey.indexOf("}") + 1, pathParamKey.length()));
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        m.find();
        try {
            return m.group(1);
        } catch (Exception ex) {
            log.error("Unable to get path param value. URL: " + url + ", mapperKey: " + pathParamKey, ex);
            return "";
        }
    }
}
