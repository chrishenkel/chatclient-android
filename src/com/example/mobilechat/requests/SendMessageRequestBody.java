package com.example.mobilechat.requests;

public class SendMessageRequestBody {
	public String sessiontoken;
	public String message;

	public String getSessiontoken() {
		return sessiontoken;
	}

	public void setSessiontoken(String sessiontoken) {
		this.sessiontoken = sessiontoken;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
