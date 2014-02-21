package com.example.mobilechat.response;

public class LoginResponse {
	public String message;
	public String sessiontoken;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSessiontoken() {
		return sessiontoken;
	}

	public void setSessiontoken(String sessiontoken) {
		this.sessiontoken = sessiontoken;
	}

}
