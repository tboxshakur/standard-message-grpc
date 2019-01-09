package com.example;

import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

import com.example.grpc.GrpcServer;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws InterruptedException, IOException {
		SpringApplication.run(Application.class, args);
		GrpcServer.startup();
	}

	@Bean
	ProtobufHttpMessageConverter protobufHttpMessageConverter() {
		return new ProtobufHttpMessageConverter();
	}
}
