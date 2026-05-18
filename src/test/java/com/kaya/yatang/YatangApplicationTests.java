package com.kaya.yatang;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-refresh-token-secret-value-32chars")
class YatangApplicationTests {

	@Test
	void contextLoads() {
	}

}
