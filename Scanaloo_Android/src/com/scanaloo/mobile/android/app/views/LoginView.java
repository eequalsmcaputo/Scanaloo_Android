package com.scanaloo.mobile.android.app.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.scanaloo.mobile.android.R;
import com.scanaloo.mobile.android.util.MenuActivity;
import com.scanaloo.mobile.android.util.Scanaloo;

public class LoginView extends MenuActivity {
	
	/*
	private enum PageState {
		login, pageview;
	}
	*/
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Scanaloo.logMessage("LOGIN_VIEW", "Scanaloo login screen created.");
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		if(Scanaloo.isLoggedIn(this)){
			showLoops();
			//setPageState(PageState.pageview);
			
		} /*else {
			setPageState(PageState.login);
		}*/
	}
	
	public void doLogin(View v)
	{
		
		EditText user = (EditText)findViewById(R.id.edtUser);
		String user_name = user.getText().toString();
		Scanaloo.logMessage("DO_LOGIN", "Logging in...");
		if (Scanaloo.slLogin(this, user_name))
		{
			Scanaloo.logMessage("DO_LOGIN", "Login successful. User ID: " +
					Scanaloo.getUserId(this));
			
			//Scanaloo.initMenu(this);
			showLoops();
			//setPageState(PageState.pageview);
		} else {
			Scanaloo.showToast(this, "Could not log in.");
		}
		
	}
	
	private void showLoops()
	{
		Scanaloo.slLoops(this);
	}
	
	/*
	private void setPageState(PageState pageState)
	{
		RelativeLayout login = (RelativeLayout) findViewById(R.id.lytrLogin);
		RelativeLayout pageview = (RelativeLayout) findViewById(R.id.lytrPageView);
		
		if(pageState == PageState.login)
		{
			login.setVisibility(View.VISIBLE);
			pageview.setVisibility(View.GONE);
		} else if (pageState == PageState.pageview)
		{
			login.setVisibility(View.GONE);
			pageview.setVisibility(View.VISIBLE);
		}
		
	}
	*/
}
