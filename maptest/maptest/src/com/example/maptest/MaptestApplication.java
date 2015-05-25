package com.example.maptest;

import android.app.Application;

public class MaptestApplication extends Application
{
	@Override
	public void onCreate() {
		super.onCreate();
		RestClient.init(this);
	}

}
