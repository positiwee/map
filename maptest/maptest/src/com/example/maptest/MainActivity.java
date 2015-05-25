package com.example.maptest;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements
		OnMyLocationChangeListener {

	GoogleMap mGoogleMap;
	LatLng loc;
	CameraPosition cp;
	MarkerOptions markerOption; // ���۸ʿ� �⺻��Ŀ ǥ��
	Marker marker1;
	Marker marker2;
	Marker dangerMarker;
	LocationManager locationManger;
	String provider;
	boolean firstLocationChange;
	LatLng dangerLocation;
//
	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 1;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;

	LocationRequest mLocationRequest;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startActivity(new Intent(this, FlashActivity.class));

		mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		// Enabling MyLocation Layer of Google Map
		mGoogleMap.setMyLocationEnabled(true);
		// Setting event handler for location change

		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

		firstLocationChange = true;

		mGoogleMap.setOnMyLocationChangeListener(this);

		locationManger = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		provider = locationManger.getBestProvider(criteria, true);
		if (provider == null || !locationManger.isProviderEnabled(provider)) {
			List<String> list = locationManger.getAllProviders();
			for (int i = 0; i < list.size(); i++) {
				String temp = list.get(i);
				if (locationManger.isProviderEnabled(temp)) {
					provider = temp;
					break;
				}
			}
		}

		Location location = locationManger.getLastKnownLocation(provider);
		if (location == null)
			Toast.makeText(this, "Not provider", Toast.LENGTH_SHORT).show();
		else {
			CameraPosition cp = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location
							.getLongitude())).zoom(10).build();
			// addMarker(location.getLatitude(), location.getLongitude(), cp,
			// true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void requestLocation() {
		String url = "http://nukeguys.cafe24.com/client.php";
		Uri.Builder builder = Uri.parse(url).buildUpon();

		GsonRequest<DangerLocation> gsonRequest = new GsonRequest<DangerLocation>(
				Request.Method.GET, builder.toString(), DangerLocation.class, null,
				listener, errorListener);
		RestClient.client().getRequestQueue().add(gsonRequest);
	}

	private Listener<DangerLocation> listener = new Listener<DangerLocation>() {
		@Override
		public void onResponse(DangerLocation location) {
			addMarker2(location);
		}
	};

	private ErrorListener errorListener = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			Toast.makeText(MainActivity.this, "Network Error",
					Toast.LENGTH_SHORT).show();
		}
	};

	OnMarkerClickListener onMarkerClickListener = new OnMarkerClickListener() {

		@Override
		public boolean onMarkerClick(Marker marker) {
			//Toast.makeText(MainActivity.this, "��Ŀ�� Ŭ����", Toast.LENGTH_LONG)
			//		.show();

			dangerLocation = marker.getPosition();
			addDangerMarker(dangerLocation.latitude, dangerLocation.longitude);

			return false;
		}
	};

	private void addMarker(double latitude, double longitude, CameraPosition cp) {
		// latLng������ ���� ����, �浵�� ����
		loc = new LatLng(latitude, longitude);

		// ��Ŀ,Ÿ��Ʋ, ������ ǥ��
		if (marker1 != null)
			marker1.remove(); // ������Ŀ�����

		// CameraPosition.Builder().target((loc)).zoom(zoomLevel).build();
		mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
		// animateCamera(CameraUpdateFactory.newCameraPosition(cp));
		markerOption = new MarkerOptions().position(loc).title("������ġ")
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
				.snippet("Ŭ���ϼ���");
		marker1 = mGoogleMap.addMarker(markerOption);

		// ��Ŀ�� Ÿ��Ʋ,�������� Ŭ������ �� ȣ���
		mGoogleMap
				.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick(Marker arg0) {

						// TODO Auto-generated method stub

						AlertDialog.Builder alert = new AlertDialog.Builder(
								MainActivity.this);
						alert.setTitle("������ġ�� ������ �Է��Ͻðڽ��ϱ�?");
						alert.setIcon(R.drawable.ic_launcher);
						// positive��ưŬ���� ó���� �̺�Ʈ ��ü ����
						DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// �����ͺ��̽� �Է��۾� ��� ����
								Toast.makeText(MainActivity.this, "�Է��۾�����",
										Toast.LENGTH_LONG).show();

							}
						};
						alert.setPositiveButton("Ȯ��", positiveClick);
						alert.setNegativeButton("���", null);
						alert.show();
					}

				});

		// ��Ŀ�� Ŭ������ �� ȣ���
		mGoogleMap.setOnMarkerClickListener(onMarkerClickListener);
	}

	private void moveMarker(LatLng latlng) {
		// marker1.setPosition(latlng);
		animateMarker(marker1, latlng, false);
	}

	private void addMarker2(DangerLocation location) {
		// latLng������ ���� ����, �浵�� ����
		loc = new LatLng(location.latitude, location.longitude);

		// ��Ŀ,Ÿ��Ʋ, ������ ǥ��
		if (marker2 != null)
			marker2.remove(); // ������Ŀ�����

		cp = new CameraPosition.Builder().target((loc)).zoom(16).build();
		markerOption = new MarkerOptions().position(loc).title(location.title)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.scouter))
				.snippet(location.snippet);
		marker2 = mGoogleMap.addMarker(markerOption);
	}

	private void addDangerMarker(double latitude, double longitude) {
		// ��Ŀ,Ÿ��Ʋ, ������ ǥ��
		//if (dangerMarker != null)
		//	dangerMarker.remove(); // ������Ŀ�����
		Toast.makeText(MainActivity.this, "���� �������� �����մϴ�",
				Toast.LENGTH_LONG).show();
		markerOption = new MarkerOptions()
		
				.position(new LatLng(latitude, longitude)).title("������ġ").icon(BitmapDescriptorFactory.fromResource(R.drawable.emergency))
				.snippet("�� ������ ���������Դϴ�.");
		dangerMarker = mGoogleMap.addMarker(markerOption);
	}

	@Override
	public void onMyLocationChange(Location location) {
		requestLocation();

		// ���� ����
		double latitude = location.getLatitude();
		// ���� �浵
		double longitude = location.getLongitude();

		CameraPosition cp;

		if (firstLocationChange) {
			firstLocationChange = false;
			cp = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location
							.getLongitude())).zoom(14).build();
			addMarker(latitude, longitude, cp);
		} else {
			moveMarker(new LatLng(mGoogleMap.getMyLocation().getLatitude(),
					mGoogleMap.getMyLocation().getLongitude()));
		}

	}

	public void animateMarker(final Marker marker, final LatLng toPosition,
			final boolean hideMarker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = mGoogleMap.getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 1000;

		final Interpolator interpolator = new LinearInterpolator();

		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * toPosition.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * toPosition.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				} else {
					if (hideMarker) {
						marker.setVisible(false);
					} else {
						marker.setVisible(true);
					}
				}
			}
		});
	}
}