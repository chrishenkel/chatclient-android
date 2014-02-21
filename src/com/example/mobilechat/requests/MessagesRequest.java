package com.example.mobilechat.requests;

import android.content.res.Resources;

import com.example.mobilechat.R;
import com.example.mobilechat.response.MessageList;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

//Create a request in its own Java file, it should not an inner class of a Context
public class MessagesRequest extends SpringAndroidSpiceRequest<MessageList> {

	private Resources resources;

	public MessagesRequest(Resources resources) {
		super(MessageList.class);
		this.resources = resources;
	}

	@Override
	public MessageList loadDataFromNetwork() throws Exception {
		String url = String.format(resources.getString(R.string.rest_endpoint) + "/message");
		return getRestTemplate().getForObject(url, MessageList.class);
	}

	/**
	 * This method generates a unique cache key for this request. In this case
	 * our cache key depends just on the keyword.
	 * 
	 * @return
	 */
	public String createCacheKey() {
		return "newmessages";
	}
}