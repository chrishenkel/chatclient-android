package com.example.mobilechat.requests;

import android.content.res.Resources;

import com.example.mobilechat.R;
import com.example.mobilechat.response.SignUpResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class SignUpRequest  extends SpringAndroidSpiceRequest<SignUpResponse>  {
	private String username;
	private String password;
	private Resources resources;

	public SignUpRequest(Resources resources, String username, String password) {
		super(SignUpResponse.class);
		this.resources = resources;
		this.username = username;
		this.password = password;
	}

	@Override
	public SignUpResponse loadDataFromNetwork() throws Exception {
		String url = String.format(resources.getString(R.string.rest_endpoint) + "/register");
		
		SignupRequestBody body = new SignupRequestBody();
		body.username = username;
		body.password = password;

		return getRestTemplate().postForEntity(url, body, SignUpResponse.class).getBody();
	}

	/**
	 * This method generates a unique cache key for this request. In this case
	 * our cache key depends just on the keyword.
	 * 
	 * @return
	 */
	public String createCacheKey() {
		return "signuprequest" + username + password;
	}
}
