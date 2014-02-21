package com.example.mobilechat;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mobilechat.requests.LoginRequest;
import com.example.mobilechat.requests.SignUpRequest;
import com.example.mobilechat.response.LoginResponse;
import com.example.mobilechat.response.SignUpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class LoginActivity extends Activity implements OnClickListener {

	private static final String TAG = "com.example.mobilechat.LoginActivity.TAG";

	public static final String SESSION_TOKEN = "com.example.mobilechat.LoginActivity.SESSION_TOKEN";

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "1.1.1";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	private EditText username;
	private EditText password;

	private SharedPreferences preferences;

	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	private String SENDER_ID = "669288725304";

	private GoogleCloudMessaging gcm;

	private String regid;

	protected SpiceManager spiceManager = new SpiceManager(
			JacksonSpringAndroidSpiceService.class);
	private String lastRequestCacheKeySignup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		username = (EditText) findViewById(R.id.loginUsername);
		password = (EditText) findViewById(R.id.loginPassword);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		
		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(getApplicationContext());

            if (regid.isEmpty()) {
                registerInBackground();
            }
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);

	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	public void signupPressed(View view) {
		signup(username.getText().toString(), password.getText().toString());
	}

	public void loginPressed(View view) {
		String regid = getRegistrationId(getApplicationContext());
		if(regid.isEmpty())
		{
			Toast.makeText(getApplicationContext(), "Error connecting, please try again later", Toast.LENGTH_SHORT).show();
            registerInBackground();
			return;
		}
		LoginRequest request = new LoginRequest(getResources(), username.getText().toString(),
				password.getText().toString(), regid);
		lastRequestCacheKeySignup = request.createCacheKey();
		spiceManager.execute(request, lastRequestCacheKeySignup,
				DurationInMillis.ALWAYS_EXPIRED, new LoginRequestListener());
	}
	
	// ------------------------------------------------------------------------
	// ---------end of block that can fit in a common base class for all
	// activities
	// ------------------------------------------------------------------------

	private void signup(String username, String password) {
		SignUpRequest request = new SignUpRequest(getResources(), username, password);
		lastRequestCacheKeySignup = request.createCacheKey();
		spiceManager.execute(request, lastRequestCacheKeySignup,
				DurationInMillis.ALWAYS_EXPIRED, new SignUpRequestListener());
	}

	@Override
	public void onClick(View arg0) {
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Check device for Play Services APK.
		checkPlayServices();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGcmPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
					}
					regid = gcm.register(SENDER_ID);
					Log.i(TAG, "Device registered, registration ID=" + regid);
					
					// For this demo: we don't need to send it because the
					// device will send
					// upstream messages to a server that echo back the message
					// using the
					// 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(getApplicationContext(), regid);
				} catch (IOException ex) {
					Log.i(TAG, "Error :" + ex.getMessage());
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return regid;
			}

			@Override
			protected void onPostExecute(String regid) {
				// You should send the registration ID to your server over
				// HTTP, so it
				// can use GCM/HTTP or CCS to send messages to your app.
				Log.i(TAG, "Done executing: regid = " + regid);
			}
		}.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(LoginActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}
	
	// inner class of your spiced Activity
	private class SignUpRequestListener implements
			RequestListener<SignUpResponse> {

		@Override
		public void onRequestFailure(SpiceException e) {
			Toast.makeText(getApplicationContext(),
					"failure: response  = " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(SignUpResponse response) {
			Toast.makeText(getApplicationContext(), response.getMessage(),
					Toast.LENGTH_SHORT).show();
			loginPressed(null);
		}
	}

	// inner class of your spiced Activity
	private class LoginRequestListener implements
			RequestListener<LoginResponse> {

		@Override
		public void onRequestFailure(SpiceException e) {
			if (e.getCause() instanceof HttpClientErrorException) {
				HttpClientErrorException exception = (HttpClientErrorException) e
						.getCause();
				if (exception.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
					Toast.makeText(getApplicationContext(),
							"No user found with those credentials.",
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
			Toast.makeText(getApplicationContext(),
					"failure: response  = " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(LoginResponse response) {
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(SESSION_TOKEN, response.getSessiontoken());
			editor.commit();
			Toast.makeText(
					getApplicationContext(),
					response.getMessage() + ", token = "
							+ response.getSessiontoken(), Toast.LENGTH_SHORT)
					.show();
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
		}
	}

}
