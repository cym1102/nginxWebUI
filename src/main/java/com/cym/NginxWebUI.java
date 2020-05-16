package com.cym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class NginxWebUI {

	public static void main(String[] args) {
		SpringApplication.run(NginxWebUI.class, args);
	}
}
