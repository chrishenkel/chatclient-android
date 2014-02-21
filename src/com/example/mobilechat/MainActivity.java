package com.example.mobilechat;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilechat.requests.MessagesRequest;
import com.example.mobilechat.requests.SendMessageRequest;
import com.example.mobilechat.response.ChatMessage;
import com.example.mobilechat.response.MessageList;
import com.example.mobilechat.response.SendMessageResponse;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class MainActivity extends Activity {

	private static final String TAG = "com.example.mobilechat.MainActivity";
	private EditText messageBox;
	private SharedPreferences preferences;
	private ScrollView scrollView;
	private LinearLayout ll;
	private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		messageBox = (EditText) findViewById(R.id.mainEditText);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		
		scrollView = (ScrollView) findViewById(R.id.scrollView1);
		scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
            	scrollView.post(new Runnable() {
                    public void run() {
                    	scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
		
		ll = new LinearLayout(MainActivity.this);
	    ll.setOrientation(LinearLayout.VERTICAL);
	    scrollView.addView(ll);	   
	    
	    callAsynchronousTask();
	    
	    requestNewMessages();
	}
	
	public void callAsynchronousTask() {
	    final Handler handler = new Handler();
	    timer = new Timer();
	    TimerTask doAsynchronousTask = new TimerTask() {       
	        @Override
	        public void run() {
	            handler.post(new Runnable() {
	                public void run() {       
	                	if(MessageSingleton.getInstance().getMessage() == MessageSingleton.MESSAGE_NEEDS_UPDATE)
	                	{
	                		requestNewMessages()	;
	                		MessageSingleton.getInstance().setMessage(MessageSingleton.MESSAGE_UPDATED);
	                	}
	                }
	            });
	        }
	    };
	    timer.schedule(doAsynchronousTask, 0, 100); 
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected SpiceManager spiceManager = new SpiceManager(
			JacksonSpringAndroidSpiceService.class);
	private String lastRequestCacheKeySendMessage;
	private String lastRequestCacheKeyRequestMessages;

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
		timer.cancel();
	}

	public void sendPressed(View view) {
		String text = messageBox.getText().toString();
		sendMessage(text);
		messageBox.setText("");
	}
	
	private void requestNewMessages()
	{
		MessagesRequest request = new MessagesRequest(getResources());
		lastRequestCacheKeyRequestMessages = request.createCacheKey();
		spiceManager.execute(request, lastRequestCacheKeyRequestMessages,
				DurationInMillis.ONE_SECOND, new NewMessagesRequestListener());
	}
	
	private void sendMessage(String message)
	{
		SendMessageRequest request = new SendMessageRequest(getResources(), preferences.getString(LoginActivity.SESSION_TOKEN, null), message);
		lastRequestCacheKeySendMessage = request.createCacheKey();
		spiceManager.execute(request, lastRequestCacheKeySendMessage,
				DurationInMillis.ONE_SECOND, new SendMessageRequestListener());
	}

	// inner class of your spiced Activity
	private class SendMessageRequestListener implements
			RequestListener<SendMessageResponse> {

		@Override
		public void onRequestFailure(SpiceException e) {
			Toast.makeText(getApplicationContext(), "error sending message", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(SendMessageResponse response) {
			Toast.makeText(getApplicationContext(), "message sent!", Toast.LENGTH_SHORT).show();
		}
	}
	

	// inner class of your spiced Activity
	private class NewMessagesRequestListener implements
			RequestListener<MessageList> {

		@Override
		public void onRequestFailure(SpiceException e) {
		}

		@Override
		public void onRequestSuccess(MessageList response) {
			ll.removeAllViews();
		    for(ChatMessage m : response)
		    {
		    	TextView tv = new TextView(MainActivity.this.getApplicationContext());
		    	tv.setTextColor(getResources().getColor(R.color.Black));
		    	tv.setText(m.getSender() + " : " + m.getMessage());
		    	ll.addView(tv);
		    }
		}
	}
}
