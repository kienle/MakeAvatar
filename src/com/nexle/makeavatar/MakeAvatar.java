package com.nexle.makeavatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.widget.ImageView;

public class MakeAvatar {
	public static void makeCircal(Context context, ImageView imageView, Bitmap mainImage, Bitmap mask) {
		Log.d("KienLT", "[MakeAvatar] test " + mainImage.getWidth() + " mask: " + mask.getWidth());
        Canvas canvas = new Canvas();
//        Bitmap mainImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.img);
//        Bitmap mask = BitmapFactory.decodeResource(context.getResources(), R.drawable.ava_mask);
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);

        canvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        canvas.drawBitmap(mainImage, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);

        imageView.setImageBitmap(result);
        imageView.invalidate();
	}
}
