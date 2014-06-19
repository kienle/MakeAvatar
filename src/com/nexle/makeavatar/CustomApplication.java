package com.nexle.makeavatar;

import android.app.Application;
import android.content.Context;

public class CustomApplication extends Application {

	private static Context context;

	public void onCreate() {
		context = getApplicationContext();
	}

	public static Context getAppContext() {
		return context;
	}

}
