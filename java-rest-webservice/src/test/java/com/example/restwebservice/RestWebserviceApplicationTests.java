package com.example.restwebservice;

import org.elasticsearch.client.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RestWebserviceApplicationTests {


	@Autowired
	Client client;

	@Test
	void contextLoads() {
		assertNotNull(client, "Elastic search client should not be null");
	}

}
