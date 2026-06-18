package com.wildace.roulette.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfiguration {

	@Bean
	public OpenAPI defineOpenApi() {
		Server server = new Server();
		server.setUrl("http://192.168.1.127:9090");
		server.setDescription("LAB Roulette service API Server");
		
		Contact contact = new Contact();
		contact.setName("WildAce Team");
		contact.setEmail("ops@wildace.in");
		
		Info info = new Info()
				.title("WildAce EGM Roulette Service API")
				.description("RESET API for WildAce EGM Roulette Service")
				.version("1.0.0")
				.contact(contact);
		
		return new OpenAPI()
				.addServersItem(server)
				.info(info);
		
	}
}
