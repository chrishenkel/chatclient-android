package com.example.mobilechat.requests;

import android.content.res.Resources;

import com.example.mobilechat.R;
import com.example.mobilechat.response.SendMessageResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class SendMessageRequest extends SpringAndroidSpiceRequest<SendMessageResponse> {
	private String sessionToken;
	private String message;
	private Long timeStamp = System.currentTimeMillis();
	private Resources resources;

	public SendMessageRequest(Resources resources, String sessionToken, String message) {
		super(SendMessageResponse.class);
		this.resources = resources;
		this.sessionToken = sessionToken;
		this.message = message;
		
	}

	@Override
	public SendMessageResponse loadDataFromNetwork() throws Exception {
		String url = String.format(resources.getString(R.string.rest_endpoint) + "/message");
		
		SendMessageRequestBody body = new SendMessageRequestBody();
		body.sessiontoken = sessionToken;
		body.message = message;	

		return getRestTemplate().postForEntity(url, body, SendMessageResponse.class).getBody();
	}

	/**
	 * This method generates a unique cache key for this request. In this case
	 * our cache key depends just on the keyword.
	 * 
	 * @return
	 */
	public String createCacheKey() {
		return "sendMessage" + timeStamp;
	}
}
