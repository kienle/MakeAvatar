package com.nexle.makeavatar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class MakeAvatarActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_make_avatar);
	}
}
