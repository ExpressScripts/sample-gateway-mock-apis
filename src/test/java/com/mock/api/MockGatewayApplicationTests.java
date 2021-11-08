package com.mock.api;


import com.mock.api.mock.controller.LocalMockController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MockGatewayApplicationTests {

	@Autowired
	private LocalMockController localController;

	@Test
	public void contextLoads() throws Exception {
		assertThat(localController).isNotNull();
	}

}

