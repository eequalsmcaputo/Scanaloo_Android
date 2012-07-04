package com.scanaloo.mobile.android.app.views;

import com.scanaloo.mobile.android.R;
import com.scanaloo.mobile.android.util.MenuActivity;
import com.scanaloo.mobile.android.util.Scanaloo;

import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PageView extends MenuActivity {

	String mCurrentPage;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pageview);
		WebView webView = (WebView) findViewById(R.id.webView);
		webView.setWebViewClient(new MyWebViewClient());
		
		Bundle extras = getIntent().getExtras();
		
		if(extras != null)
		{
			Scanaloo.logMessage("PAGE_VIEW", "Showing page...");
			String page = extras.getString(Scanaloo.PARAM_PAGE);
			String user_id = extras.getString(Scanaloo.PARAM_USERID);
			int type = extras.getInt(Scanaloo.PARAM_TYPE);
			
			loadScanalooPage(page, user_id, type);
		}
	}
	
	private void loadScanalooPage(String page, String user_id, int type)
	{
		WebView webView = (WebView) findViewById(R.id.webView);
		
		String web = Scanaloo.getSlapi(this, page);
		String url = web + "?" + Scanaloo.PARAM_USERID + "=" + user_id +
				"&" + Scanaloo.PARAM_TYPE + "=" + type;
		webView.loadUrl(url);
		
		Scanaloo.logMessage("LOAD_SCANALOO_PAGE", "URL " + url + " loaded.");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	WebView webView = (WebView) findViewById(R.id.webView);
	    	if (webView.getUrl().indexOf("myloopshow?") >= 0)
	    	{
	    		Scanaloo.slMyLoops(this);
	    		return true;
	    	} else if (webView.getUrl().indexOf("type=2") >= 0)
	    	{
	    		Scanaloo.slLoops(this);
	    	}
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	view.loadUrl(url);
	    	return true;
	    }
	}	
}
