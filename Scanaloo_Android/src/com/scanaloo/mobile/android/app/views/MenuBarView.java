package com.scanaloo.mobile.android.app.views;

import com.scanaloo.mobile.android.R;
import com.scanaloo.mobile.android.util.Scanaloo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class MenuBarView extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menubar);
		Scanaloo.logMessage("MENU", "Scanaloo menu bar screen enabled.");
		
		Button btnHome = (Button) findViewById(R.id.btnHome);
		btnHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btnHome_Click(v);
			}});
		
		LinearLayout lytHome = (LinearLayout) findViewById(R.id.lytHome);
		lytHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btnHome_Click(v);
			}});
	}
	
	public void btnHome_Click(View v)
	{
		try {
			Scanaloo.clearActivities(this);
			Scanaloo.slMyLoops(this);
		} catch(Exception e) {
			Scanaloo.logError("SCANALOO_HOME", e);
		}		
	}
	
}
