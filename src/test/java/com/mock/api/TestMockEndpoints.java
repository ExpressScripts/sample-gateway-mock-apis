package com.mock.api;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class TestMockEndpoints {


    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testFileBasedResponse() {
        assertThat(this.restTemplate.getForObject(getEndpoint("/api/sample/file"), String.class))
                .contains("Default: This is a sample response");
    }

    @Test
    public void testKeyBasedResponse() {
        assertThat(this.restTemplate.getForObject(getEndpoint("/api/sample/config"), String.class))
                .contains("another sample response");
    }

    private String getEndpoint(String endpoint) {
        return "http://localhost:" + 8089 + endpoint;
    }
}
