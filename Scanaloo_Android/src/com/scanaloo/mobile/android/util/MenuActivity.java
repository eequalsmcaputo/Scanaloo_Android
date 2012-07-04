package com.scanaloo.mobile.android.util;

import com.scanaloo.mobile.android.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
 
public class MenuActivity extends Activity {
 
    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu, menu);
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
 
        switch (item.getItemId())
        {
        case R.id.mnuLogout:
            Scanaloo.logout(this);
        	return true;
        case R.id.mnuLoop:
        	Scanaloo.slNewLoop(this);
        	return true;
        case R.id.mnuMyLoops:
        	Scanaloo.slMyLoops(this);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }    
 
}