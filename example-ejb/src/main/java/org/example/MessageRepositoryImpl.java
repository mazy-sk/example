package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class MessageRepositoryImpl implements MessageRepository {

	private List<String> messages;
	
	@Lock(LockType.WRITE)
	@Override
	public void addMessage(String message) {
		messages.add(message);
	}

	@Override
	public List<String> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	@Override
	public int getMessageCount() {
		return messages.size();
	}
	
	@PostConstruct
	void init() {
		messages = new ArrayList<String>();
	}

}
