package com.example.controller;

import org.springframework.web.bind.annotation.RestController;
import com.example.StandardUserMessage;
import com.example.dao.UserDao;
import com.example.exceptions.MessageValidationException;
import com.example.grpc.GrpcClient;
import com.example.user.GetUserRequest;
import com.example.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {

	private static final Logger logger = Logger.getLogger(UserController.class.getName());

	/**
	 * This method demonstrates sending the protocolbuffer bytes via REST instead of
	 * GRPC. We will probably not need this.
	 * 
	 * @param id
	 * @return
	 * @throws InvalidProtocolBufferException
	 */
	@GetMapping("/protobuf/user/{id}")
	public User getUserProtobuf(@PathVariable String id) throws InvalidProtocolBufferException {

		User user = UserDao.getUser(id, null);
		if (user == null) {
			throw new IllegalArgumentException(String.format("User %s not found", id));
		}

		return user;
	}

	/**
	 * Gets a User directly from DAO
	 * 
	 * @param id
	 * @return simple json representation of the User protobuf
	 * @throws InvalidProtocolBufferException
	 */
	@GetMapping("/user/{id}")
	public String getUser(@PathVariable String id) throws InvalidProtocolBufferException {
		User user = UserDao.getUser(id, null);
		if (user == null) {
			throw new IllegalArgumentException(String.format("User %s not found", id));
		}

		return JsonFormat.printer().print(user);
	}

	/**
	 * Gets a User whilst testing grpc
	 * 
	 * @param id
	 * @return simple json representation of the User protobuf
	 * @throws InvalidProtocolBufferException
	 */
	@GetMapping("/grpc/user/{id}")
	public String getUserGrpc(@PathVariable String id) throws InvalidProtocolBufferException {
		try {
			return JsonFormat.printer()
					.print(GrpcClient.getSingleton().get(GetUserRequest.newBuilder().setId(id).build()));
		} catch (StatusRuntimeException e) {
			throw new IllegalArgumentException(e.getStatus().getDescription());
		}
	}

	/**
	 * Upserts a User
	 * 
	 * @param json
	 * @return simple json representation of the GenericResponse protobuf
	 * @throws IOException
	 * @throws MessageValidationException
	 * @throws UpsertFailedException
	 */
	@PostMapping("/user")
	public String upsert(@RequestBody String json)
			throws IOException, MessageValidationException, UpsertFailedException {
		User.Builder user_builder = User.newBuilder();
		JsonFormat.parser().merge(json, user_builder);

		try {
			StandardUserMessage my_wrapped_message_with_validation = new StandardUserMessage(user_builder.build());
			if (UserDao.upsert(my_wrapped_message_with_validation)) {
				return JsonFormat.printer().print(my_wrapped_message_with_validation.getMessage());
			} else {
				throw new UpsertFailedException("Upsert failed");
			}
		} catch (MessageValidationException e) {
			throw new UpsertFailedException(e.getMessage());
		}

	}

	/**
	 * Upserts a User whilst using grpc
	 * 
	 * @param json
	 * @return simple json representation of the GenericResponse protobuf
	 * @throws IOException
	 * @throws MessageValidationException
	 * @throws UpsertFailedException
	 */
	@PostMapping("/grpc/user")
	public String upsertGrpc(@RequestBody String json)
			throws IOException, MessageValidationException, UpsertFailedException {
		User.Builder user_builder = User.newBuilder();
		JsonFormat.parser().merge(json, user_builder);

		try {
			return JsonFormat.printer().print(GrpcClient.getSingleton().upsert(user_builder.build()));
		} catch (StatusRuntimeException e) {
			throw new UpsertFailedException(e.getStatus().getDescription());
		}
	}

	@ExceptionHandler
	void handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response) throws IOException {
		response.sendError(HttpStatus.NOT_FOUND.value());
	}

	private class UpsertFailedException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7507847615680980052L;

		public UpsertFailedException(String msg) {
			super(msg);
		}
	}

	@ExceptionHandler
	void handleUpsertFailedException(UpsertFailedException e, HttpServletResponse response) throws IOException {
		response.sendError(HttpStatus.BAD_REQUEST.value());
	}
}
