package com.example.maptest;

import android.app.ActivityManager;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class RestClient
{
	private static final class SingletonHolder
	{
		static final RestClient client = new RestClient();		 
	}
	
	private RestClient(){}
	
	public static RestClient client()
	{
		return SingletonHolder.client;
	}

	private static RequestQueue requestQueue;
	private static ImageLoader imageLoader;
	
	
	public static void init(Context context)
	{
		requestQueue = Volley.newRequestQueue(context);
		
		int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		 // Use 1/8th of the available memory for this memory cache.
		int cacheSize = 1024 * 1024 * memClass / 8;
		imageLoader = new ImageLoader(requestQueue, new BitmapLruCache(cacheSize));
		
	}
	
	public RequestQueue getRequestQueue()
	{
		if (requestQueue != null)
			return requestQueue;
		else
			throw new IllegalStateException("RequestQueue not initialized");
	}
	
	public ImageLoader getImageLoader()
	{
		if (imageLoader != null)
			return imageLoader;
		else
			throw new IllegalStateException("ImageLoader not initailized");
	}
}