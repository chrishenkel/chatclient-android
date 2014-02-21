package com.example.mobilechat;

public class MessageSingleton {
	private static MessageSingleton singleton;
	
	public static final String MESSAGE_NEEDS_UPDATE = "MESSAGE_NEEDS_UPDATE";
	public static final String MESSAGE_UPDATED = "MESSAGE_UPDATED";
	
	private String message = MESSAGE_NEEDS_UPDATE;

	private MessageSingleton() {

	}

	public static MessageSingleton getInstance() {
		if (singleton == null) {
			singleton = new MessageSingleton();
		}
		return singleton;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
