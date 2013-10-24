
package com.egloos.realmove.android.locationfinder;

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
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.location.Location;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProviderListActivity extends MapActivity {

	private ListView mListView;
	private MapView mMapView;

	private Context mContext;
	private List<ProviderInfo> mProviders;
	private LayoutInflater mInflater;
	private ListAdapter mAdapter;

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
	}

	@Override
	protected void onDestroy() {
		mContext = null;
		mMapView = null;
		mListView = null;
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		drawOverlays();
	}

	private void listProvider() {
		LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getAllProviders();

		if (providers != null) {
			mProviders = new ArrayList<ProviderInfo>();
			for (int i = 0; i < providers.size(); i++) {
				ProviderInfo info = new ProviderInfo(providers.get(i));

				Location location = locationManager.getLastKnownLocation(info.getName());
				info.setLastKnownLocation(location);
				mProviders.add(info);
			}
		}
	}

	private void drawOverlays() {
		MyLocOverlay myLocOverlay = new MyLocOverlay();
		myLocOverlay.enableMyLocation();
		mMapView.getOverlays().add(myLocOverlay);

		if (mProviders != null) {
			boolean flag = false;
			for (int i = 0; i < mProviders.size(); i++) {
				ProviderInfo info = mProviders.get(i);
				Location location = info.getLastKnownLocation();
				if (location != null) {
					GeoPoint geo = new GeoPoint((int) (location.getLatitude() * 1000000), (int) (location.getLongitude() * 1000000));
					MyOverlay overlay = new MyOverlay(geo, info.getName());
					mMapView.getOverlays().add(overlay);

					if (!flag) {
						MapController controller = mMapView.getController();
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
			if (provider.getLastKnownLocation() != null) {
				SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
				holder.time.setText(df.format(new Date(provider.getLastKnownLocation().getTime())));
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

		private GeoPoint geoPoint;
		private String name;

		public MyOverlay(GeoPoint geoPoint, String name) {
			this.geoPoint = geoPoint;
			this.name = name;
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
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
			paint.setColor(Color.WHITE);
			canvas.drawRect(left - 3, top - 3, right + 3, bottom + 3, paint);

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

			if ( geoPoint == null ) return;
			
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
			paint.setColor(Color.WHITE);
			canvas.drawRect(left - 3, top - 3, right + 3, bottom + 3, paint);

			paint.setAntiAlias(true);
			paint.setColor(Color.RED);
			canvas.drawText(text, left, bottom - fm.descent, paint);
		}
		
	}

	
}
