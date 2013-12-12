package com.example.googlesignin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;

public class GoogleSignInClass {
	Context mContext;
	Activity mActivity;
	AccountManager mAccountManager;
	String mAccountName ;
	String mToken;
	ProgressDialog mProgressDialog;
	private static final String scope = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	private static final String profileDataUrl = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=";
	public  static final int GET_EMAIL_REQUEST_CODE = 1001;
	public  static final int GET_GOOGLE_SIGN_IN_REQUEST_CODE = 1002;
	
	public GoogleSignInClass(Context context,Activity activity)
	{
		this.mContext = context;
		this.mActivity = activity;
		
		mProgressDialog = new ProgressDialog(mActivity);
		mProgressDialog.setMessage("Please wait.......");
		mProgressDialog.setCancelable(false);
	}
	
	public void execute()
	{
		Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},false, null, null, null, null);
		mActivity.startActivityForResult(intent, GET_EMAIL_REQUEST_CODE);
	}
	
	public void onAccountSelected(String selectedEmail)
	{
		mAccountName = selectedEmail;
		new GetLoginTokenTask().execute();
	}
	
	public void onGoogleSignInSuccess()
	{
		new GetLoginTokenTask().execute();
	}
	
	private class GetLoginTokenTask extends AsyncTask<String, Void, String> 
	{
		String token="";
		@Override
        protected void onPreExecute() 
		{
			mProgressDialog.setTitle("Getting outh token");
			mProgressDialog.show();
		}
		@Override
        protected String doInBackground(String... params) 
		{
			try
			{
				token = GoogleAuthUtil.getToken(mActivity, mAccountName, scope);
	            return token;
			}
			catch(GooglePlayServicesAvailabilityException playEx)
			{
				Dialog alert = GooglePlayServicesUtil.getErrorDialog(playEx.getConnectionStatusCode(),mActivity,GET_GOOGLE_SIGN_IN_REQUEST_CODE);
		    }
			catch (UserRecoverableAuthException userAuthEx)
			{
				mActivity.startActivityForResult(userAuthEx.getIntent(), GET_GOOGLE_SIGN_IN_REQUEST_CODE);
				userAuthEx.printStackTrace();
		        return token;
		    } 
			catch (IOException transientEx)
			{
				// network or server error, the call is expected to succeed if you try again later.
		        // Don't attempt to call again immediately - the request is likely to
		        // fail, you'll hit quotas or back-off.
		    	transientEx.printStackTrace();
		        return "";
		    } 
			catch (GoogleAuthException authEx) 
			{
				// Failure. The call is not expected to ever succeed so it should not be
		        // retried.
		    	authEx.printStackTrace();
		        return token;
		    }
			return token;
        }
        @Override
        protected void onPostExecute(String result) 
        {
        	mProgressDialog.dismiss();
        	Log.e("test","token is "+result);
        	mToken = result;
        	if(result!=null)
        		new GetUserProfileData().execute();
        }
    }
	
	private class GetUserProfileData extends AsyncTask<Void, Void, Void> 
	{
		GoogleSignInResponse mGoogleSignInResponse;
		
		@Override
        protected void onPreExecute() 
		{
			mProgressDialog.setTitle("Fetching profile data");
			mProgressDialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) 
		{
			try 
			{
				mGoogleSignInResponse = fetchNameFromProfileServer();
			} 
			catch (IOException ex) 
			{
				Log.e("test","Following Error occured, please try again. "+ ex.getMessage());
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
				Log.e("test","Bad response: " + e.getMessage());
			}
			return null;
		}
		
		 @Override
	        protected void onPostExecute(Void temp) 
	        {
			GoogleAuthUtil.invalidateToken(mActivity, mToken);
			    mProgressDialog.dismiss();
			    ((GoogleSignInDelegate) mActivity).onGoogleProfileDataReceived(mGoogleSignInResponse);
	        }

		private GoogleSignInResponse fetchNameFromProfileServer() throws IOException, JSONException 
		{
			URL url = new URL(profileDataUrl+ mToken);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			int sc = con.getResponseCode();
			if (sc == 200) 
			{
				InputStream is = con.getInputStream();
				String JsonRes = readResponse(is);
				GoogleSignInResponse mm = new Gson().fromJson( JsonRes, GoogleSignInResponse.class);
				Log.e("test","name="+mm.given_name);
				Log.e("test","id="+mm.id);
				
				is.close();
				return mm;
			} 
			else if (sc == 401) 
			{
				GoogleAuthUtil.invalidateToken(mActivity, mToken);
				Log.e("test","Server auth error, please try again.");
				return null;
			} else {
				Log.e("test","Server returned the following error code: " + sc);
				return null;
			}
		}
		
		private  String readResponse(InputStream is) throws IOException 
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] data = new byte[2048];
			int len = 0;
			while ((len = is.read(data, 0, data.length)) >= 0) 
			{
				bos.write(data, 0, len);
			}
			return new String(bos.toByteArray(), "UTF-8");
		}
	}
	
	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
	

}
