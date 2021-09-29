package com.ec.quickbooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.cempresariales.servicio.commons.model.entity"})
//public class ServicioClientesApplication extends SpringBootServletInitializer{
public class ServicioQuickBooksApplication {

	/*@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ServicioClientesApplication.class);
	}*/
	
	
	
	public static void main(String[] args) {
		SpringApplication.run(ServicioQuickBooksApplication.class, args);
	}

}
