
package com.egloos.realmove.android.locationfinder;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class ProviderListActivity extends MapActivity {

	private ListView mListView;
	private MapView mMapView;

	private Context mContext;
	private List<String> mProviders;
	private LayoutInflater mInflater;
	private ListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.provider_list);
		
		listProvider();

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mListView = (ListView) findViewById(android.R.id.list);
		mMapView = (MapView) findViewById(R.id.mapview);

		mAdapter = new ProviderAdapter();
		mListView.setAdapter(mAdapter);
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
	}

	private void listProvider() {
		LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		mProviders = locationManager.getAllProviders();

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
		public String getItem(int position) {
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
				holder.req = (ImageButton) convertView.findViewById(R.id.req_btn);
				holder.progress = (ProgressBar) convertView.findViewById(android.R.id.progress);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String providerName = getItem(position);
			SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
			
			holder.name.setText(providerName);

			return convertView;
		}
	}

	class ViewHolder {
		TextView name;
		TextView time;
		ImageButton req;
		ProgressBar progress;
	}

}
