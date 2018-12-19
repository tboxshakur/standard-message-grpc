package com.example.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.StandardUserMessage;
import com.example.dao.UserDao;
import com.example.exceptions.MessageValidationException;
import com.example.grpc.GrpcClient;
import com.example.user.GenericResponse;
import com.example.user.GetUserRequest;
import com.example.user.User;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
		User user = GrpcClient.getSingleton().get(GetUserRequest.newBuilder().setId(id).build(), null);

		if (user == null) {
			throw new IllegalArgumentException(String.format("User %s not found", id));
		}

		return JsonFormat.printer().print(user);

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
			if (UserDao.upsert(new StandardUserMessage(user_builder.build()))) {
				return JsonFormat.printer()
						.print(GenericResponse.newBuilder().setDescription("Upsert Successful").setStatus(200).build());
			} else {
				throw new UpsertFailedException("Upsert failed");
			}
		} catch (MessageValidationException e) {
			throw new UpsertFailedException("I hate your JSON data");
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

		GenericResponse response = GrpcClient.getSingleton().upsert(user_builder.build(), null);

		if (response == null) {
			throw new UpsertFailedException("I hate your JSON data");
		}

		return JsonFormat.printer().print(response);
	}

}
