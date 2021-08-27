package com.tsb.cb;

import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.tsb.cb.config.CircuitBreakerAutoConfiguration;
import com.tsb.cb.config.CircuitBreakerConfigProperties;
import com.tsb.cb.config.CircuitBreakerProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CircuitBreakerApplication.class, CircuitBreakerAutoConfiguration.class })
@WebAppConfiguration
//@EnableAutoConfiguration()
@EnableConfigurationProperties(value = { CircuitBreakerProperties.class})
@TestPropertySource(locations="classpath:application-test.yml")

public class AppTests {

	/*
	 * public static void main(String[] args) {
	 * 
	 * }
	 */
}
