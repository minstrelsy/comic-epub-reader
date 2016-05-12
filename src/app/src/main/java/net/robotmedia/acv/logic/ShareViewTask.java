/*******************************************************************************
 * Copyright 2009 Robot Media SL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.robotmedia.acv.logic;

import java.io.File;
import java.io.FileOutputStream;

import net.robotmedia.acv.Constants;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

public class ShareViewTask extends AsyncTask<View, Object, String> {

	private String mName;
	private Context mContext;
	private static final String IMAGE_NAME = "share.jpg";

	@Deprecated
	private String relativeTempPath = "";

	private String chooserTitle = "";

	private String extraSubject = "";

	private String extraText = "";

	private File tempDir;

	public ShareViewTask(Context context) {
		mContext = context;
		tempDir = context.getCacheDir();
	}

	@Override
	protected String doInBackground(View... params) {
		final View v = params[0];
		Bitmap shareBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
		Canvas c = new Canvas(shareBitmap);
		v.draw(c);
		File imageFile = getImageFile();
		try {
			FileOutputStream out = new FileOutputStream(imageFile);
			boolean success = shareBitmap.compress(CompressFormat.JPEG, Constants.COMPRESSION_QUALITY, out);
			out.close();
			if (success) {
				String path = imageFile.getAbsolutePath();
				return MediaStore.Images.Media.insertImage(mContext.getContentResolver(), path, mName, mName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getChooserTitle() {
		return chooserTitle;
	}

	public String getExtraSubject() {
		return extraSubject;
	}
	public String getExtraText() {
		return extraText;
	}
	private File getImageFile() {
		File dir;
		if (relativeTempPath != null) {
			dir = new File(Environment.getExternalStorageDirectory(), relativeTempPath);
		} else {
			dir = tempDir;
		}
		dir.mkdirs();
		File file = new File(dir, IMAGE_NAME);
		return file;
	}
	@Deprecated
	public String getRelativeTempPath() {
		return relativeTempPath;
	}
	@Override
	protected void onPostExecute (String result) {
		if (result != null) {
			final Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("image/jpeg");
			intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(result));
			intent.putExtra(Intent.EXTRA_SUBJECT, extraSubject.replaceAll("@comic", mName));
			intent.putExtra(Intent.EXTRA_TEXT, extraText.replaceAll("@comic", mName));
			final Intent chooser = Intent.createChooser(intent, chooserTitle);
			mContext.startActivity(chooser);
		}
	}
	
	public void setChooserTitle(String chooserTitle) {
		this.chooserTitle = chooserTitle;
	}
	
	public void setExtraSubject(String extraSubject) {
		this.extraSubject = extraSubject;
	}
	
	public void setExtraText(String extraText) {
		this.extraText = extraText;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	@Deprecated
	public void setRelativeTempPath(String relativeTempPath) {
		this.relativeTempPath = relativeTempPath;
	}
	
	public void setTempDir(File tempDir) {
		this.tempDir = tempDir;
	}

}
