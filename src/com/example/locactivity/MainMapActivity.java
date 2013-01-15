package com.example.locactivity;

import java.io.IOException;

import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

/*Class: MainMapActivity
 * This class is called from MainActivity when the user clicks "Get current Location" button.
 * The class displays google map and the current location of user(marked with the image similar to a pegman)
 * Class implements a location listener  which gets updated in every TIME_GAP and DISTANCE. If there is a change in the location
 * updateUILocation function is called which sets the new location on the google map.
 * 
 * Functions:
 * onCreate -> Initialize activity and call setup.
 * LocationListener -> Handles the location change
 * updateUILocation -> Update location on map when there is a location change
 * requestUpdatesFromProvider -> request update from provider based on TIME_GAP and DISTANCE
 * useFineProvider -> use fine provider
 * setup -> start receiving updates from provider
 * 
 */
public class MainMapActivity extends MapActivity {

	// UI handler codes.
	private LocationManager mLocationManager;

	private static final int TIME_GAP = 5000;
	private static final int DISTANCE = 10;
	private boolean mUseFine;

	// Keys for maintaining UI states after rotation.
	private static final String KEY_FINE = "use_fine";

	/*Function: onCreate
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 * 
	 * Initialize the activity set content view with activity_loc layout
	 * setup function is called to get the location details and start listening to location changes
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loc);
		Log.i("MainMapActivity", "onCreate");

		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);



		mUseFine = true;
		setup();
	}

	/*Function: LocationListener
	 * Used for receiving notifications from the LocationManager when the location has changed.
	 * Upon location update function onLocationChanged is executed. updateUILocation function is called inside onLocationChanged 
	 * and the new location is displayed.
	 */
	private final LocationListener listener = new LocationListener() {

		public void onLocationChanged(Location location) {
			updateUILocation(location);

		}

		public void onProviderDisabled(String arg0) {
			Toast.makeText(MainMapActivity.this, "GPS disabled", Toast.LENGTH_LONG).show();
		}

		public void onProviderEnabled(String arg0) {
			Toast.makeText(MainMapActivity.this, "GPS enabled", Toast.LENGTH_LONG).show();
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

	};
	/*Function: updateUILocation
	 * displays the google map and the location of user.
	 * This function receives message from two functions
	 * a) from setup -> this happens at the initial phase. This is called only once. 
	 * b) from onLocationChanged -> updateUILocation is called everytime when there is change in user's location.
	 * 
	 *   Once the location is received, Geocoder gets the addresses using the latitude/longitude values.
	 *   here we assume the first address as the correct address that should be shown on map.
	 *   The maps is then drawn using mapview layout and markers are also set.
	 *   Finally location on map is obtained using GeoPoint and map is drawn using overlays.
	 * 
	 */
	public void updateUILocation(Location location) {
		// We're sending the update to a handler which then updates the UI with the new
		// location.

		Geocoder gcd = new Geocoder(this, Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String address = addresses.get(0).getAddressLine(0);
		String city = addresses.get(0).getAddressLine(1);
		String country = addresses.get(0).getAddressLine(2);
		String countryCode = addresses.get(0).getCountryCode();
		Log.i("LocActivity", "updateUILocation: Latitude: "+String.valueOf(location.getLatitude()));
		Log.i("LocActivity", "updateUILocation: Longitude: "+String.valueOf(location.getLongitude()));
		Log.i("LocActivity", "updateUILocation: City: "+city);
		Log.i("LocActivity", "updateUILocation: Country: "+country);
		Log.i("LocActivity", "updateUILocation: Address: "+address);
		Log.i("LocActivity", "updateUILocation: Country Code: "+countryCode);

		MapView mapView = (MapView) findViewById(R.id.mapview);
		mapView.setVisibility(View.VISIBLE);
		mapView.setBuiltInZoomControls(true);


		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
		Drawable drawable = this.getResources().getDrawable(R.drawable.pegman_launcher);

		ItemsOverlay itemsoverlay = new ItemsOverlay(drawable, this);

		GeoPoint point = new GeoPoint((int)(location.getLatitude()*1e6),(int)(location.getLongitude()*1e6));
		OverlayItem overlayitem = new OverlayItem(point, address, city+", "+countryCode);
		MapController mapController = mapView.getController();
		mapController.setCenter(point);

		itemsoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemsoverlay);
	}


	/*Function: requestUpdatesFromProvider
	 * The function gets the location details  using requestLocationUpdates call and returns location. 
	 * This location will be consumed by updateUILocation.
	 * the listener is also registered so that whenever there is a change in location, listener gets triggered.
	 * the time and distance interval is mentioned using TIME_GAP and DISTANCE variable.
	 */
	private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
		Location location = null;
		if (mLocationManager.isProviderEnabled(provider)) {
			mLocationManager.requestLocationUpdates(provider, TIME_GAP, DISTANCE, listener);
			location = mLocationManager.getLastKnownLocation(provider);
		} else {
			Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
		}
		return location;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_loc, menu);
		return true;
	}

	// Callback method for the "fine provider" button.
	public void useFineProvider(View v) {
		mUseFine = true;
		setup();
	}

	@Override
	protected void onPause(){
		super.onPause();
		Log.i("MainMapActivity", "onPause");
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i("MainMapActivity", "onDestroy");
		finish();
	}
	@Override
	protected void onResume() {
		super.onResume();
		setup();
	}
	// Restores UI states after rotation.
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_FINE, mUseFine);
	}

	// Stop receiving location updates whenever the Activity becomes invisible.
	@Override
	protected void onStop() {
		super.onStop();
		mLocationManager.removeUpdates(listener);
	}

	/*Function: setup
	 * Set up fine location providers
	 * All the gps updates are cleared and a fresh call is made to requestUpdatesFromProvider function
	 * If location details are received an updateUILocation function is called to display location
	 */
	private void setup() {
		Location gpsLocation = null;
		mLocationManager.removeUpdates(listener);
		// Get fine location updates only.
		if (mUseFine) {
			// Request updates from just the fine (gps) provider.
			gpsLocation = requestUpdatesFromProvider(
					LocationManager.GPS_PROVIDER, R.string.not_support_gps);
			// Update the UI immediately if a location is obtained.
			if (gpsLocation != null) updateUILocation(gpsLocation);
		}
	}



	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
