/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid
 */

package com.scanaloo.mobile.android.app.views;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.scanaloo.mobile.android.R;
import com.scanaloo.mobile.android.util.MenuActivity;
import com.scanaloo.mobile.android.util.Scanaloo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

public class LoopStartView extends MenuActivity {
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.loopstart);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		preview = (SurfaceView) findViewById(R.id.preview);
		
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		Button loop = (Button) findViewById(R.id.loop);
		loop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startLoop();
			}
		});
	}

	private void sendPicture(File photo) {
		Scanaloo.logMessage("SEND_PICTURE", "Sending picture...");
		try {
			Toast.makeText(this, "Starting loop...", Toast.LENGTH_LONG);
			// Scanaloo.showToast(this, "Starting loop...");
			
			EditText title = (EditText)findViewById(R.id.title);
			EditText category = (EditText)findViewById(R.id.category);
			
			if (Scanaloo.uploadPhoto(this, photo,
					title.getText().toString(),
					category.getText().toString(),
					"Test Friends")) {
				Scanaloo.showToast(this, "Loop started!");
				Scanaloo.logMessage("SEND_PICTURE", "Picture sent.");
				finish();
			}
		} catch (Exception e) {
			Scanaloo.logError("SEND_PICTURE", e);
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		camera = Camera.open();
		startPreview();
	}

	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}

		camera.release();
		camera = null;
		inPreview = false;

		super.onPause();
	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;
		Camera.Size last = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
			last = size;
		}

		if (result == null) {
			result = last;
			//result.height = preview.getHeight();
			//result.width = preview.getWidth();
		}

		return (result);
	}

	private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea < resultArea) {
					result = size;
				}
			}
		}

		return (result);
	}

	private void initPreview(int width, int height) {
		if (camera != null && previewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(previewHolder);
			} catch (Exception e) {
				Scanaloo.logError("INIT_PREVIEW", e);
				Scanaloo.showToast(this, e.getMessage());
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size size = getBestPreviewSize(width, height, parameters);

				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);

					Size picSize = getSmallestPictureSize(parameters);
					parameters.setPictureSize(picSize.width, picSize.height);
					camera.setParameters(parameters);
					cameraConfigured = true;
				}
			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera != null) {
			this.setCameraDisplayOrientation(this, camera);

			camera.startPreview();
			inPreview = true;
		}
	}

	private void setCameraDisplayOrientation(Activity activity,
			android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = 
				new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(
				Camera.CameraInfo.CAMERA_FACING_BACK, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
		Camera.Parameters parameters = camera.getParameters();
		parameters.setRotation(180);
		camera.setParameters(parameters);
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			initPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SavePhotoTask().execute(data);
			camera.startPreview();
			inPreview = true;
		}
	};

	protected void startLoop() {
		try {
			this.takePicture();
		} catch (Exception e) {
			Scanaloo.logError("TAKE_PICTURE", e);
		}
	}

	private void takePicture() {
		if (inPreview) {
			camera.takePicture(null, null, photoCallback);
			inPreview = false;
		}
	}

	class SavePhotoTask extends AsyncTask<byte[], String, File> {

		@Override
		protected File doInBackground(byte[]... jpeg) {

			File photo = new File(Environment.getExternalStorageDirectory(),
					"photo" + java.lang.System.currentTimeMillis() + ".jpg");

			if (photo.exists()) {
				photo.delete();
			}

			try {
				Bitmap bmp = fixOrientation(jpeg[0]);
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, 
						new FileOutputStream(photo.getPath()));
			} catch (java.io.IOException e) {
				Scanaloo.logError("SAVE_PHOTO", e);
			}

			return photo;
		}
		
		private Bitmap fixOrientation(byte[] photo) {
			
			Bitmap bmp = BitmapFactory.decodeByteArray(photo, 0, photo.length);
			
		    if (bmp.getWidth() > bmp.getHeight()) {
		        Matrix matrix = new Matrix();
		        matrix.postRotate(90);
		        return Bitmap.createBitmap(bmp , 0, 0, 
		        		bmp.getWidth(), bmp.getHeight(), matrix, true);
		        
		    } else {
		    	return bmp;
		    }
		}

		protected void onPostExecute(File photo) {
			sendPicture(photo);
		}
	}
}