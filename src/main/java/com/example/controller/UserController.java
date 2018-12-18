package com.example.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.exceptions.MessageValidationException;
import com.example.grpc.GrpcClient;
import com.example.user.GenericResponse;
import com.example.user.GetUserRequest;
import com.example.user.User;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {

	private static final Logger logger = Logger.getLogger(UserController.class.getName());

	/**
	 * Gets a User
	 * 
	 * @param id
	 * @return simple json representation of the User protobuf
	 * @throws InvalidProtocolBufferException
	 */
	@GetMapping("/user/{id}")
	public String getUser(@PathVariable String id) throws InvalidProtocolBufferException {

		logger.info(String.format("Attempting to retrieve User:%s", UUID.fromString(id)));

		User user = GrpcClient.getSingleton().get(GetUserRequest.newBuilder().setId(id).build(),
				User.getDefaultInstance());
		return JsonFormat.printer().print(user);

	}

	/**
	 * Upserts a User
	 * 
	 * @param json
	 * @return simple json representation of the GenericResponse protobuf
	 * @throws IOException
	 * @throws MessageValidationException
	 */
	@PostMapping("/user")
	public String upsert(@RequestBody String json) throws IOException, MessageValidationException {
		User.Builder user_builder = User.newBuilder();
		JsonFormat.parser().merge(json, user_builder);
		GenericResponse response = GrpcClient.getSingleton().upsert(user_builder.build());
		return JsonFormat.printer().print(response);
	}

}
