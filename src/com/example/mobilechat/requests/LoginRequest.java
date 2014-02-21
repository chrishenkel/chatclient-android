package com.example.mobilechat.requests;

import android.content.res.Resources;

import com.example.mobilechat.LoginBody;
import com.example.mobilechat.R;
import com.example.mobilechat.response.LoginResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class LoginRequest  extends SpringAndroidSpiceRequest<LoginResponse>  {
	private String username;
	private String password;
	private String gcmId;
	private Resources resources;

	public LoginRequest(Resources resources, String username, String password, String gcmId) {
		super(LoginResponse.class);
		this.resources = resources;
		this.username = username;
		this.password = password;
		this.gcmId = gcmId;
	}

	@Override
	public LoginResponse loadDataFromNetwork() throws Exception {
		String url = String.format(resources.getString(R.string.rest_endpoint) + "/login");
		
		LoginBody body = new LoginBody();
		body.username = username;
		body.password = password;
		body.gcmRegId = gcmId;

		return getRestTemplate().postForEntity(url, body, LoginResponse.class).getBody();
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
