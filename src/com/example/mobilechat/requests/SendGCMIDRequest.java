package com.example.mobilechat.requests;

import android.content.res.Resources;

import com.example.mobilechat.R;
import com.example.mobilechat.response.SendGCMIDResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class SendGCMIDRequest extends SpringAndroidSpiceRequest<SendGCMIDResponse> {
	private String regid;
	private Resources resources;

	public SendGCMIDRequest(Resources resources, String regid) {
		super(SendGCMIDResponse.class);
		this.resources = resources;
		this.regid = regid;
	}

	@Override
	public SendGCMIDResponse loadDataFromNetwork() throws Exception {
		String url = String.format(resources.getString(R.string.rest_endpoint) + "/gcm/register");
		
		SendGCMIDRequestBody body = new SendGCMIDRequestBody();
		body.regid = regid;

		return getRestTemplate().postForEntity(url, body, SendGCMIDResponse.class).getBody();
	}

	/**
	 * This method generates a unique cache key for this request. In this case
	 * our cache key depends just on the keyword..
	 * 
	 * @return
	 */
	public String createCacheKey() {
		return "registerGCM";
	}
}
