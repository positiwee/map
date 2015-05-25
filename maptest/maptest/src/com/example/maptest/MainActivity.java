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
	MarkerOptions markerOption; // 구글맵에 기본마커 표시
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
			//Toast.makeText(MainActivity.this, "마커가 클릭됨", Toast.LENGTH_LONG)
			//		.show();

			dangerLocation = marker.getPosition();
			addDangerMarker(dangerLocation.latitude, dangerLocation.longitude);

			return false;
		}
	};

	private void addMarker(double latitude, double longitude, CameraPosition cp) {
		// latLng변수에 현재 위도, 경도를 저장
		loc = new LatLng(latitude, longitude);

		// 마커,타이틀, 스니핏 표시
		if (marker1 != null)
			marker1.remove(); // 기존마커지우기

		// CameraPosition.Builder().target((loc)).zoom(zoomLevel).build();
		mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
		// animateCamera(CameraUpdateFactory.newCameraPosition(cp));
		markerOption = new MarkerOptions().position(loc).title("현재위치")
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
				.snippet("클릭하세요");
		marker1 = mGoogleMap.addMarker(markerOption);

		// 마커의 타이틀,스니핏을 클릭했을 때 호출됨
		mGoogleMap
				.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick(Marker arg0) {

						// TODO Auto-generated method stub

						AlertDialog.Builder alert = new AlertDialog.Builder(
								MainActivity.this);
						alert.setTitle("현재위치의 정보를 입력하시겠습니까?");
						alert.setIcon(R.drawable.ic_launcher);
						// positive버튼클릭시 처리할 이벤트 객체 생성
						DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 데이터베이스 입력작업 등등 실행
								Toast.makeText(MainActivity.this, "입력작업실행",
										Toast.LENGTH_LONG).show();

							}
						};
						alert.setPositiveButton("확인", positiveClick);
						alert.setNegativeButton("취소", null);
						alert.show();
					}

				});

		// 마커를 클릭했을 때 호출됨
		mGoogleMap.setOnMarkerClickListener(onMarkerClickListener);
	}

	private void moveMarker(LatLng latlng) {
		// marker1.setPosition(latlng);
		animateMarker(marker1, latlng, false);
	}

	private void addMarker2(DangerLocation location) {
		// latLng변수에 현재 위도, 경도를 저장
		loc = new LatLng(location.latitude, location.longitude);

		// 마커,타이틀, 스니핏 표시
		if (marker2 != null)
			marker2.remove(); // 기존마커지우기

		cp = new CameraPosition.Builder().target((loc)).zoom(16).build();
		markerOption = new MarkerOptions().position(loc).title(location.title)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.scouter))
				.snippet(location.snippet);
		marker2 = mGoogleMap.addMarker(markerOption);
	}

	private void addDangerMarker(double latitude, double longitude) {
		// 마커,타이틀, 스니핏 표시
		//if (dangerMarker != null)
		//	dangerMarker.remove(); // 기존마커지우기
		Toast.makeText(MainActivity.this, "위험 지역으로 설정합니다",
				Toast.LENGTH_LONG).show();
		markerOption = new MarkerOptions()
		
				.position(new LatLng(latitude, longitude)).title("위험위치").icon(BitmapDescriptorFactory.fromResource(R.drawable.emergency))
				.snippet("이 지역은 위험지역입니다.");
		dangerMarker = mGoogleMap.addMarker(markerOption);
	}

	@Override
	public void onMyLocationChange(Location location) {
		requestLocation();

		// 현재 위도
		double latitude = location.getLatitude();
		// 현재 경도
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