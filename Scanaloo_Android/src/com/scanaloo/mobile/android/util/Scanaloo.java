package com.scanaloo.mobile.android.util;

import com.scanaloo.mobile.android.R;

import com.scanaloo.mobile.android.app.views.LoginView;
import com.scanaloo.mobile.android.app.views.LoopStartView;
import com.scanaloo.mobile.android.app.views.MenuBarView;
import com.scanaloo.mobile.android.app.views.PageView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

public class Scanaloo {
	
	private static final String USER_ID = "user_id";
	public static final String PAGE_LOOP = "loop";
	public static final String PAGE_LOOPS = "loops";
	public static final String PAGE_LOGIN = "login";
	public static final String PARAM_USERID = "user_id";
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_TYPE = "type";
	
	/*
	public static void initMenu(Context context) {
		Intent i = new Intent(context, MenuBarView.class);
		context.startActivity(i);
	}
	*/
	
	public static void slNewLoop(Context context)
	{
		Intent i = new Intent(context, LoopStartView.class);
		context.startActivity(i);	
		
	}
	
	public static void slMyLoops(Context context)
	{
		openWebView(context, PAGE_LOOPS, 2);
	}
	
	public static void slLoops(Context context)
	{
		openWebView(context, Scanaloo.PAGE_LOOPS, 1);
	}
	
	public static boolean slLogin(Context context, String user_name)
	{
		long user_id = -1l;
		
		try {
			HttpResponse response = doGet(getSlapi(context, PAGE_LOGIN) + 
					"?user_name=" + user_name);
			
			String resp = readResponse(response);
			logMessage("LOGIN", resp);
			
			if (resp.indexOf(USER_ID) >= 0)
			{
				user_id = new Long(resp.split(":")[1].trim());
				Log.w("LOGIN", "User ID: " + user_id);
				setUserId(context, user_id);
			}
			
		} catch(Exception e) {
			Scanaloo.logError("SL_LOGIN", e);
		}
		
		return (user_id >= 0l);
		
	}
	
	private static void openWebView(Context context, String page, int type)
	{
		try {
			Scanaloo.logMessage("OPEN_WEB_VIEW", "Type: " + type);
			Intent i = new Intent(context, PageView.class);
			i.putExtra(PARAM_PAGE, page);
			i.putExtra(PARAM_USERID, getUserIdString(context));
			i.putExtra(PARAM_TYPE, type);
			context.startActivity(i);
		} catch(Exception e) {
			logError(page, e);
		}
	}
	
	public static boolean uploadPhoto(Context context,
			File file, String title, String category, String friends) {
		
		try {
			FormBodyPart[] params = new FormBodyPart[4];
			
			params[0] = new FormBodyPart("loop[user_id]", 
					new StringBody(getUserIdString(context)));
			params[1] = new FormBodyPart("loop[title]", new StringBody(title));
			params[2] = new FormBodyPart("loop[category]", new StringBody(category));
			params[3] = new FormBodyPart("loop[friends]", new StringBody(friends));
			
			return executeUpload(context, file, params);
			
		} catch (UnsupportedEncodingException e) {
			logError("SCANALOO_UPLOAD_PHOTO", e);
			return false;
		}
		
	}
	
	private static boolean executeUpload(Context context, 
			File file, FormBodyPart[] params) {
		try {
			
			final String photoParam = "loop[photo]";
			
			HttpClient httpClient = new DefaultHttpClient();
			
			//Get the service to post to
			HttpPost postRequest = new HttpPost(getSlapi(context, PAGE_LOOP));
			
			//Put the image into a multipart object
			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(photoParam, preparePhoto(file));
			
			//Add the other parameters to the request
			for(FormBodyPart p : params){
				reqEntity.addPart(p);
			}
			
			//Add the request to the post object
			postRequest.setEntity(reqEntity);
			
			return (null != readResponse(httpClient.execute(postRequest)));

		} catch (Exception e) {
			logError("SCANALOO_EXECUTE_UPLOAD", e);
			return false;
		}
	}
	
	private static String readResponse(HttpResponse response){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String sResponse;
			StringBuilder s = new StringBuilder();

			while ((sResponse = reader.readLine()) != null) {
				s = s.append(sResponse);
			}
			
			return s.toString();	
		} catch (Exception e) {
			logError("SCANALOO_READ_RESPONSE", e);
			return null;
		}
	}
	
	private static ByteArrayBody preparePhoto(File file)
	{
		Bitmap bm = BitmapFactory.decodeFile(file.getPath());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bm.compress(CompressFormat.JPEG, 75, bos);
		byte[] data = bos.toByteArray();

		return new ByteArrayBody(data, file.getName());
	}

	public static HttpResponse doGet(String url)
	{
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			return httpClient.execute(httpGet);
		} catch (Exception e) {
			Scanaloo.logError("DO_GET", e);
			return null;
		}
	}
	
	public static SharedPreferences getPrefs(Context context)
	{
		return context.getSharedPreferences("scanaloo_prefs", 0);
	}
	
	public static boolean isLoggedIn(Context context)
	{
		return (getUserId(context) >= 0l);
	}
	
	public static long getUserId(Context context)
	{
		return getPrefs(context).getLong(USER_ID, -1l);
	}
	
	public static String getUserIdString(Context context)
	{
		return String.valueOf(getUserId(context));
	}
	
	private static void setUserId(Context context, long user_id)
	{
		SharedPreferences.Editor editor = getPrefs(context).edit();
		editor.putLong(USER_ID, user_id);
		editor.commit();	
	}
	
	public static void logout(Context context)
	{
		SharedPreferences.Editor editor = getPrefs(context).edit();
		editor.remove(USER_ID);
		editor.commit();
		clearActivities(context);
	}
	
	public static void clearActivities(Context context)
	{
		context.startActivity(new Intent(context, LoginView.class)
		 .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

	public static String getSlapi(Context context, String page) {
		String web = context.getResources().getString(R.string.slapi);
		return web + page;
	}

	public static void logError(String src, Exception e)
	{
		Log.e("SCANALOO_" + src, e.getMessage());
	}

	public static void logMessage(String src, String msg)
	{
		Log.w("SCANALOO_" + src, msg);
	}
	
	public static void showToast(Context context, String msg)
	{
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
}
