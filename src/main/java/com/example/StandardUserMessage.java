package com.example;

import java.util.UUID;
import java.util.logging.Logger;

import com.example.core.StandardMessage;
import com.example.exceptions.MessageValidationException;
import com.example.user.User;

/**
 * A nicely wrapped protobuf class that takes care of common validation &
 * normalization for User
 * 
 * @author tbox
 *
 */
public class StandardUserMessage extends StandardMessage<User> {

	private static final Logger logger = Logger.getLogger(StandardUserMessage.class.getName());

	public StandardUserMessage(User message) throws MessageValidationException {
		super(message);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(User o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void validate(User message) throws MessageValidationException {
		if (!message.hasField(User.getDescriptor().findFieldByNumber(User.ID_FIELD_NUMBER))) {
			throw new MessageValidationException(String.format("Required field %s is unset!",
					User.getDescriptor().findFieldByNumber(User.ID_FIELD_NUMBER).getFullName()));
		}

		try {
			UUID.fromString(message.getId());
		} catch (Exception e) {
			throw new MessageValidationException(String.format("Id %s is not a valid UUID!", message.getId()));
		}

	}

	@Override
	public User normalize(final User message) {

		if (!message.hasField(User.getDescriptor().findFieldByNumber(User.ID_FIELD_NUMBER))) {
			User user = User.newBuilder(message).setId(UUID.randomUUID().toString()).build();
			logger.info(String.format("New User request! User Id was set to %s", user.getId()));
			return user;
		}
		return message;
	}

	@Override
	public UUID getKey() {
		return UUID.fromString(getMessage().getId());
	}

}
