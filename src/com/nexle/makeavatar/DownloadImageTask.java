package com.nexle.makeavatar;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

	private Context mContext;
	private ImageView mImage;
	private int mDrawableMask;

	public DownloadImageTask(Context context, ImageView image, int drawableMask) {
		this.mContext = context;
		this.mImage = image;
		this.mDrawableMask = drawableMask;
	}
	
    @Override
    protected Bitmap doInBackground(String... params) {
    	String urldisplay = params[0];
        Bitmap mIcon11 = null;
        try {
          InputStream in = new java.net.URL(urldisplay).openStream();
          mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        
        if (mDrawableMask != -1) {
	        Bitmap mask = BitmapFactory.decodeResource(mContext.getResources(), mDrawableMask);
	        MakeAvatar.makeCircal(mContext, mImage, result, mask);
        } else {
        	mImage.setImageBitmap(result);
        }
    }
}
