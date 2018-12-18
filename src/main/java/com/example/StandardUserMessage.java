package com.example;

import java.util.UUID;

import com.example.core.StandardMessage;
import com.example.exceptions.MessageValidationException;
import com.example.user.User;

public class StandardUserMessage extends StandardMessage<User> {

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
		// TODO Auto-generated method stub
	}

	@Override
	public UUID getKey() {
		return UUID.fromString(getMessage().getId());
	}

}
