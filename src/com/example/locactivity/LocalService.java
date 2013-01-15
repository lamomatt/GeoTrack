package com.example.locactivity;


/*Class: LocalService
 * This Class is to run the Location update service in background
 * 
 * LocalService reads location change based on TIME and DISTANCE parameters. 
 * Everytime a location change is recorded, its written into a database (writeToDB function)
 * 
 * The unbounded (START_STICKY) service is initiated from onStartCommand. 
 * 
 *  setup function calls requestUpdatesFromProvider which starts listening to location change using LocationListener. onLocationChanged is called when a new location
 *  is found is returned as location and given to writeToDB function for writing to database.
 *  
 *  Function:
 *  writeToDB -> writes the new location to database
 *  All other functions are similar to mentioned in MainMapActivity
 * 
 */

import java.io.IOException;
import java.util.List;
import java.util.Locale;





import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class LocalService extends Service {
	
	private LocationManager mLocationManager;
    private static final int TEN_SECONDS = 5000;
    private static final int TEN_METERS = 10;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private boolean mUseFine;
    DatabaseHandler db;
    
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
    public class LocalBinder extends Binder {
        LocalService getService() {
            return LocalService.this;
        }
    }
    
    @Override
    public void onCreate() {

        // Display a notification about us starting.  We put an icon in the status bar.
    	Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
    }
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        
        mUseFine = true;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        db = new DatabaseHandler(this, "GeoTrack", null, 9);
        setup();
        
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	try {
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        mLocationManager.removeUpdates(listener);
        Log.i("LocalService","onDestroy: Service stopped");
        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }
    

    
    private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
        Location location = null;
        if (mLocationManager.isProviderEnabled(provider)) {
            mLocationManager.requestLocationUpdates(provider, TEN_SECONDS, TEN_METERS, listener);
            location = mLocationManager.getLastKnownLocation(provider);
            //Log.i("LocalService", "requestUpdatesFromProvider: Write GPS data to DB, Lat: "+ location.getLatitude()+" Lon: "+location.getLongitude());
        } else {
            Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
        }
        return location;
    }

    
	private final LocationListener listener = new LocationListener() {

        public void onLocationChanged(Location location) {
			// A new location update is received.  Do something useful with it.  In this case,
            // we're sending the location to write to a file
            // location.
            writeToDB(location);
                
            }

		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			
		}
        
    };
    
    /*Function: writeToDB
     * writes the updated location to db.
     * db holds maximum of 20 histories
     * if history goes beyond 20, first entry from db will be deleted.
     */
    private void writeToDB(Location location) {
    	
    	//App holds maximum of 20 location history
    	if(db.rowCount() > 20){
    		db.deleteEntry();
    	}
    	
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
		try {
			addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
		} catch (IOException e) {
			Log.w("LocalService","writeToDB: Error contacting server, Shutting down Service");
			onDestroy();
			return ;
		}
		String countryCode = addresses.get(0).getCountryCode();
		String country = addresses.get(0).getCountryName();
		String city = addresses.get(0).getAddressLine(1);
		Time currentDate = new Time();
		currentDate.setToNow();
		Log.i("LocalService","writeToDB: writing to db: ");
		db.writeDB(location.getLatitude(), location.getLongitude(), currentDate.format("%Y-%m-%d"), country, city);
    }
    
    private void setup() {
        Location gpsLocation = null;
        mLocationManager.removeUpdates(listener);
        // Get fine location updates only.
        if (mUseFine) {
            // Request updates from just the fine (gps) provider.
            gpsLocation = requestUpdatesFromProvider(
                    LocationManager.GPS_PROVIDER, R.string.not_support_gps);
            // Update the UI immediately if a location is obtained.
            if (gpsLocation != null) writeToDB(gpsLocation);
        }
    }
}


