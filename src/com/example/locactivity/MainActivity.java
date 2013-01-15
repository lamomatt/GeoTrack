package com.example.locactivity;

/*Project GoeTrack
 * Class: MainActivity
 * 
 * App keeps list of locations where user has been moving while app is on. 
 *	Software shows trail in map. List holds points and time of locations. When 
 *	list item is clicked it shows location from map.
 *
 *Project classes: 
 *
 *MainActivity: Host the home page of GeoTracK project. The class opens up activity_main layout which contains 3 buttons. 
 *New service/activity will be started depending upon which button was clicked(Logic is written in onClick function).
 *Details are given on top of each functions
 *  
 *LocationHistoryFragment: Display the list which contains the history of locations where user had been. This fragment is shown
 *along with the main page incase of portrait mode in tablets. 
 *Last 20 locations visited will be shown in history. Class LocalService(function writeToDB) handles the number of history items
 * to be stored in db. For more details about the class, please see the comments in class itself
 *
 *MainMapActivity: Displays google map along with the current user location. This activity is started when "Get current Location" 
 *button is clicked(implemented in onClick function). This map implements a listener which gets updated whenever 
 *the user location changes. Map also stays intact even when the orientation is changed. For more details, please see the comments in
 *class itself.
 *
 *ShowHistoryMap: This class does the same purpose as MainMapActivity does, just with one exception. It does not update the location
 *regularly ie listener is not implemented. For more details, please see the ShowHistoryMap class.
 *
 *ItemsOverlay: This class handles the display of map and its overlays, points, markers and handles maintaining focus on the markers.
 *For details, please see the comments in class
 *
 *ListItem: Class for setting/getting City,Date, Latitude, Longitude
 *
 *LocalService: This class runs a service which updates the location details in background. Start/Stop buttons are provided in home
 *page.
 *DatabaseHandler: Handles the database operations. The location details are stored in db which will be used for showing the history. 
 *
 * 
 */




import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private Bundle savedInstanceState;
	FragmentManager fm = getSupportFragmentManager();
	
	
	/*
	 * Function: onCreate
	 * initialize MainActivity, set content view with activity_main
	 * 
	 *  All the uncaught exceptions are handled. 
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
/*	     Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
	    	 public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
	             Log.e("MainActivity","onCreate: Uncaught Exception");
	             Log.i("MainActivity","onCreate: Shutting down app");
	             finish();
	         }
	     });*/
		try {
			setContentView(R.layout.activity_main);
		} catch (Resources.NotFoundException e) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			return;
		}

		// Check if background GPS service is running
		if (isMyServiceRunning()) {
			Button but1 = (Button) findViewById(R.id.butMainStartBackground);
			but1.setVisibility(View.GONE);

			Button but2 = (Button) findViewById(R.id.butMainStopBackground);
			but2.setVisibility(View.VISIBLE);
		}
		
		
	}



	/*Function: onClick
	 * function handles all the button clicks on activity_main. 
	 * "Get Current Location" -> start new MainMapActivity and display the current location on google map
	 * "View History" -> start LocationHistoryFragment fragment and list the history of locations(read from db) where user had been
	 * "Start/Stop service in background" -> start/stop the service LocalServie which populates db with location details at regular 
	 * intervals (This button is actually a toggle button. Start or stop button is shown depending upon whether service is running
	 *  or not)
	 *  
	 * Additional handlers are given for connectivity checks(wifi and 3G connectivity). Network settings page  will be opened in 
	 * case there is not internet connectivity 
	 * 
	 * 
	 * calling Service LocalService This Service will start and stop
	 * LocalService according to user preference. Service will write latitudinal
	 * and longitudinal values to a file(locdata) in every TIME_GAP minutes.
	 * locdata will be read by LocationHistory and list the history of locations
	 * where user have been.
	 */

	
	public void onClick(View v){
		
		Log.i("MainActivity","onClick");

		/*
		 * Check for wi-fi, 3G connectivity
		 * If not connected, open Network Settings
		 */
		ConnectivityManager manager = (ConnectivityManager)getSystemService(MainActivity.CONNECTIVITY_SERVICE);
		Boolean is3G = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
		Boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
		LocationManager lm =  (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if(((!is3G && !isWifi) || !isGPS)){
			Toast.makeText(this, "Network not available, opening network settings...", Toast.LENGTH_LONG).show();
			startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		}else{
			switch(v.getId()){
			case R.id.butMainMapView:
				Log.i("MainActivity","onClick: R.id.butMainMapView 3G: "+ is3G+" wifi: "+isWifi+" gps: "+isGPS);
				Intent i = new Intent(this, MainMapActivity.class);
				startActivity(i);
				break;
			case R.id.butMainHistView:
				Log.i("MainActivity","onClick: R.id.butMainHistView");
				// However, if we're being restored from a previous state,
				// then we don't need to do anything and should return or else
				// we could end up with overlapping fragments.
				if (savedInstanceState != null) {
					return;
				}
				//hiding all other views in activity_main.xml
				LinearLayout layout = (LinearLayout) findViewById(R.id.activityMainButtons);
				layout.setVisibility(View.GONE);

				//Adding Location history fragment to the main view  
				LocationHistoryFragment locHistFrag = new LocationHistoryFragment();
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(R.id.fragment_container,locHistFrag);
				ft.addToBackStack(null);
				ft.commit();
				break;
			case R.id.butMainStartBackground:
				Button but1 = (Button) findViewById(R.id.butMainStartBackground);
				Button but2 = (Button) findViewById(R.id.butMainStopBackground);
				Intent startBG = new Intent(this, LocalService.class);
				Log.d("MainAstartBGctivity",
						"onClick: Starting background service");

				but1.setVisibility(View.GONE);
				but2.setVisibility(View.VISIBLE);

				startService(startBG);
				break;
			case R.id.butMainStopBackground:
				Button but3 = (Button) findViewById(R.id.butMainStartBackground);
				Button but4 = (Button) findViewById(R.id.butMainStopBackground);
				Intent stopBG = new Intent(this, LocalService.class);
				Log.d("MaistopBGnActivity",
						"onClick: Stopping background service");

				but3.setVisibility(View.VISIBLE);
				but4.setVisibility(View.GONE);

				stopService(stopBG);

				break;

			}
		}

	}
	

	/*Function: isMyServiceRunning
	 * checks if LocalService is running or not
	 * 
	 * returns true is LocalService is running
	 * 
	 * Since the LocalService is unbounded, MainActivity checks whether the service is running everytime activity is created
	 * 
	 */
	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (LocalService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Log.i("MainActivity","onPause");
/*        ArrayList<String> runningactivities = new ArrayList<String>();

        ActivityManager activityManager = (ActivityManager)getSystemService (Context.ACTIVITY_SERVICE); 

        List<RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE); 

        for (int i1 = 0; i1 < services.size(); i1++) { 
            runningactivities.add(0,services.get(i1).topActivity.toString());  
            Log.i("LocationHistoryFragment",services.get(i1).topActivity.toString());
        } 

        if(runningactivities.contains("ComponentInfo{com.example.locactivity/com.example.locactivty.mainmapactivity}")==false){
            LinearLayout layout = (LinearLayout) findViewById(R.id.activityMainButtons);
            Log.d("LocationHistoryFragment","onStop:orientation "+getResources().getConfiguration().orientation);
            layout.setVisibility(View.VISIBLE);
            
        }*/
	}
	
/*Function:onResume
 * Start/Stop service is a toggle button, if service is running, stop button is shown, else start button is shown
 * 
 */
	@Override
	public void onResume(){
		super.onResume();
		Log.i("MainActivity","onResume");
		    fm.popBackStack();
			// Check if background GPS service is running
			if (isMyServiceRunning()) {
				Button but1 = (Button) findViewById(R.id.butMainStartBackground);
				but1.setVisibility(View.GONE);

				Button but2 = (Button) findViewById(R.id.butMainStopBackground);
				but2.setVisibility(View.VISIBLE);
			}

			
	}
	
	/*Function: onDestroy
	 * called this function when GeoTrack exits
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.i("MainActivity","onDestroy");
		finish();
	}
}
