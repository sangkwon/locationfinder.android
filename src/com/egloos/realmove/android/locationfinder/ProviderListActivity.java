
package com.egloos.realmove.android.locationfinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProviderListActivity extends MapActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private ListView mListView;
	private MapView mMapView;
	private LayoutInflater mInflater;

	private Context mContext;
	private List<ProviderInfo> mProviders = new ArrayList<ProviderInfo>();;
	private ArrayList<MyLocListener> mListeners = new ArrayList<MyLocListener>();
	private BaseAdapter mAdapter;

	private MyLocOverlay mMyLocOverlay;
	private LocationClient mLocationClient;
	private MyOverlay mLocationClientOverlay;
	
	private ProviderInfo mRequestedProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.provider_list);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mListView = (ListView) findViewById(android.R.id.list);
		mMapView = (MapView) findViewById(R.id.mapview);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(false);

		mAdapter = new ProviderAdapter();
		mListView.setAdapter(mAdapter);

		listProvider();

		mMyLocOverlay = new MyLocOverlay();
		mMapView.getOverlays().add(mMyLocOverlay);

		mLocationClient = new LocationClient(mContext, this, this);

		drawOverlays();
	}

	@Override
	protected void onDestroy() {
		LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		if ( locationManager != null ) {
			for (MyLocListener listener : mListeners) {
				locationManager.removeUpdates(listener);
			}
		}

		mListeners.clear();

		mContext = null;
		mMapView = null;
		mListView = null;

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMyLocOverlay.disableMyLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mMyLocOverlay.enableMyLocation();
	}

	long mRequested = 0;

	@Override
	protected void onStart() {
		mRequested = System.currentTimeMillis();
		mLocationClient.connect();
		super.onStart();
	}

	@Override
	protected void onStop() {
		mLocationClient.disconnect();
		super.onStop();
	}

	private void addLocationClientLocation(String flag) {
		if (mLocationClient != null) {
			ProviderInfo info = null;

			for (ProviderInfo bean : mProviders) {
				if (bean.getName().startsWith("LocClient")) {
					info = bean;
					break;
				}
			}

			Location location = mLocationClient.getLastLocation();
			if (location != null) {
				if (info == null) {
					info = new ProviderInfo("LocClient " + flag);
					mProviders.add(info);

					mLocationClientOverlay = new MyOverlay(info);
					mMapView.getOverlays().add(mLocationClientOverlay);
				}
				info.setLocation(location);
			}
		}
	}

	private void listProvider() {
		addLocationClientLocation("L");

		LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getAllProviders();

		if (providers != null) {
			for (int i = 0; i < providers.size(); i++) {
				ProviderInfo info = new ProviderInfo(providers.get(i));

				MyLocListener listener = new MyLocListener(info);
				mListeners.add(listener);
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0L, listener);

				Location location = locationManager.getLastKnownLocation(info.getName());
				info.setLocation(location);
				mProviders.add(info);
			}

			mRequestedProvider = new ProviderInfo();
			mRequestedProvider.setName("Req");
			mProviders.add(mRequestedProvider);
		}
	}

	private void drawOverlays() {
		if (mProviders != null) {
			boolean flag = false;
			for (int i = 0; i < mProviders.size(); i++) {
				ProviderInfo info = mProviders.get(i);
				Location location = info.getLocation();
				if (location != null) {
					MyOverlay overlay = new MyOverlay(info);
					mMapView.getOverlays().add(overlay);

					if (!flag) {
						MapController controller = mMapView.getController();
						GeoPoint geo = new GeoPoint((int) (location.getLatitude() * 1000000), (int) (location.getLongitude() * 1000000));
						controller.animateTo(geo);
						controller.setZoom(19);
						flag = true;
					}
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	class ProviderAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mProviders == null ? 0 : mProviders.size();
		}

		@Override
		public ProviderInfo getItem(int position) {
			return mProviders.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.provider_row, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.req = (Button) convertView.findViewById(R.id.req_btn);
				holder.progress = (ProgressBar) convertView.findViewById(android.R.id.progress);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			ProviderInfo provider = getItem(position);

			holder.name.setText(provider.getName());
			if (provider.getLocation() != null) {
				SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
				holder.time.setText(df.format(new Date(provider.getLocation().getTime())));
			} else {
				holder.time.setText("None");
			}
			return convertView;
		}
	}

	class ViewHolder {
		TextView name;
		TextView time;
		Button req;
		ProgressBar progress;
	}

	class Overlays extends ItemizedOverlay<OverlayItem> {

		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public Overlays(Drawable icon) {
			super(icon);
		}

		public void add(OverlayItem item) {
			mOverlays.add(item);
			populate();
		}

		@Override
		protected OverlayItem createItem(int index) {
			System.out.println("createItem() " + index);
			return mOverlays.get(index);
		}

		@Override
		public int size() {
			System.out.println("size() " + mOverlays.size());
			return mOverlays.size();
		}

	}

	class MyOverlay extends Overlay {

		private ProviderInfo info;

		public MyOverlay(ProviderInfo info) {

			this.info = info;
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			String name = info.getName();
			Location location = info.getLocation();

			if (location == null) {
				return;
			}

			GeoPoint geoPoint = new GeoPoint((int) (location.getLatitude() * 1000000), (int) (location.getLongitude() * 1000000));

			super.draw(canvas, mapView, shadow);

			Point pixPoint = new Point();

			mapView.getProjection().toPixels(geoPoint, pixPoint); // 지리좌표를 화면상의 픽셀좌표로 변환

			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.map_ic_push);
			canvas.drawBitmap(bmp, pixPoint.x, pixPoint.y - (bmp.getHeight()), null);

			String text = name;
			Paint paint = new Paint();
			int textSize = 24;
			paint.setTextSize(textSize);
			float textWidth = paint.measureText(text);
			FontMetrics fm = paint.getFontMetrics();

			int left = pixPoint.x + bmp.getWidth();
			int top = pixPoint.y - (bmp.getHeight() / 2);
			int right = (int) (left + textWidth);
			int bottom = top + textSize;
//			paint.setColor(Color.WHITE);
//			canvas.drawRect(left - 3, top - 3, right + 3, bottom + 3, paint);

			paint.setAntiAlias(true);
			paint.setColor(Color.RED);
			canvas.drawText(text, left, bottom - fm.descent, paint);
		}
	}

	class MyLocOverlay extends MyLocationOverlay {

		private GeoPoint geoPoint;

		public MyLocOverlay() {
			super(mContext, mMapView);
			geoPoint = getMyLocation();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			if (geoPoint == null)
				return;

			Point pixPoint = new Point();

			mapView.getProjection().toPixels(geoPoint, pixPoint); // 지리좌표를 화면상의 픽셀좌표로 변환

			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.map_ic_push);
			canvas.drawBitmap(bmp, pixPoint.x, pixPoint.y - (bmp.getHeight()), null);

			String text = "My";
			Paint paint = new Paint();
			int textSize = 24;
			paint.setTextSize(textSize);
			float textWidth = paint.measureText(text);
			FontMetrics fm = paint.getFontMetrics();

			int left = pixPoint.x + bmp.getWidth();
			int top = pixPoint.y - (bmp.getHeight() / 2);
			int right = (int) (left + textWidth);
			int bottom = top + textSize;
			// paint.setColor(Color.WHITE);
			// canvas.drawRect(left - 3, top - 3, right + 3, bottom + 3, paint);

			paint.setAntiAlias(true);
			paint.setColor(Color.RED);
			canvas.drawText(text, left, bottom - fm.descent, paint);
		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Toast.makeText(this, "onConnectionFailed() " + connectionResult.hasResolution(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnected(Bundle bundle) {
		Toast.makeText(this, "onConnected() " + new DecimalFormat().format(System.currentTimeMillis() - mRequested) + " ms", Toast.LENGTH_SHORT)
				.show();
		System.err.println("onConnected()");
		addLocationClientLocation(" onConn");
		mAdapter.notifyDataSetChanged();
		mMapView.invalidate();
	}

	@Override
	public void onDisconnected() {
		System.err.println("onDisconnected()");
		Toast.makeText(this, "onDisconnected()", Toast.LENGTH_SHORT).show();
	}

	class MyLocListener implements LocationListener {

		ProviderInfo mInfo;

		public MyLocListener(ProviderInfo info) {
			mInfo = info;
		}

		@Override
		public void onLocationChanged(Location location) {
			System.err.println("onLocationChanged() " + mInfo.getName());
			Toast.makeText(mContext, "onLocationChanged() ", Toast.LENGTH_SHORT).show();

			if (mRequestedProvider != null) {
				mRequestedProvider.setLocation(location);
				mAdapter.notifyDataSetChanged();
				
				mMapView.invalidate();
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			System.err.println("onProviderEnabled() " + mInfo.getName());

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	}

}
