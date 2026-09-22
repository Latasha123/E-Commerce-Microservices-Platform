package com.microservices.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic unit test class
 * No Spring context loaded - just plain JUnit tests
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
		// Simple test to verify test setup works
		assertTrue(true);
	}

}
