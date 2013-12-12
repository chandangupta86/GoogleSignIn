package com.example.googlesignin;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity implements GoogleSignInDelegate {
	
	GoogleSignInClass mGoogleSignInClass;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		mGoogleSignInClass = new GoogleSignInClass(getApplicationContext(),MainActivity.this);
		mGoogleSignInClass.execute();
	}

	
	// This is the function calaled from google sign in class
	protected void onActivityResult(final int requestCode, final int resultCode,final Intent data) 
	{
		if (requestCode == GoogleSignInClass.GET_EMAIL_REQUEST_CODE) 
		{
			if(resultCode == RESULT_OK)
				mGoogleSignInClass.onAccountSelected(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
			else
				onGoogleProfileDataReceived(null);
		}
		if (requestCode == GoogleSignInClass.GET_GOOGLE_SIGN_IN_REQUEST_CODE) 
		{
			if(resultCode == RESULT_OK)
				mGoogleSignInClass.onGoogleSignInSuccess();
			else
				onGoogleProfileDataReceived(null);
		}
	}



	@Override
	public void onGoogleProfileDataReceived(
			GoogleSignInResponse googleSignInResponse) {
		// TODO Auto-generated method stub
		if(googleSignInResponse!=null)
		{
			Log.e("test","data "+googleSignInResponse.given_name+ " "+googleSignInResponse.family_name);
		}
		else
		{
			Log.e("test","failure");
		}
		
		
	}
	
	
}
