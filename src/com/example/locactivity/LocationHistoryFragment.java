package com.example.locactivity;

/*Class: LocationHistoryFragment
 * 
 * This fragment class displays the history of locations user has visited. 
 * Functions: 
 * onCreate-> Initialize the fragment and set content view with list_item layout
 * Runnable-> check for dataset change
 * onListItemClick-> This function is called when a item from list is clicked
 * onStop-> executes when fragment is stopped
 * getLocList-> fetches location details from db
 * 
 * Class: ListAdapter
 * This private class extends ArrayAdapter which is used to display list items.
 * Functions:
 * ListAdapter-> Initialize the class 
 * getView-> populates the value in each row
 * 
 * Details are mentioned on top of each functions
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class LocationHistoryFragment extends ListFragment{
	
	View view;
	private ArrayList<ListItem> list = null;
	private ListAdapter listAdapter;
	private Runnable viewHistory;
	private ProgressDialog progressDialog = null; 
	
	

	
	/*
	 * Function: onCreate
	 * initialize LocationHistoryFragment, set content view with list_item
	 * 
	 * A row if list_item represents the thumbnail of location on the left and right side is divided into two rows 
	 * First row contains City and country name and second row has the date of visit.
	 * 
	 * The location details are obtained through getters defined in ListItem class.
	 * 
	 *  Starts progressdialog and a new thread to retrieve the location history.
	 * 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
    	list = new ArrayList<ListItem>();
    	this.listAdapter = new ListAdapter(getActivity(), R.layout.list_item, list );
    	setListAdapter(this.listAdapter);
    	progressDialog = ProgressDialog.show(getActivity(),"Please wait...", "Retrieving data ...", true);
        viewHistory = new Runnable(){
        	public void run() {
        		getLocList();
            }
        };
        Thread thread =  new Thread(null, viewHistory, "MagentoBackground");
        thread.start();
        
        
         
    }
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.lochistoryview, container,false);
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    }
    
   /*Function: Runnable
    * This thread is called from getLocHist function to keep track of change in list at runtime. 
    * when there is a change in list, the thread is called and the list is updated
    */
    
    private Runnable returnRes = new Runnable() {

        public void run() {
            if(list != null && list.size() > 0){
            	listAdapter.notifyDataSetChanged();
                for(int i=0;i<list.size();i++)
                	listAdapter.add(list.get(i));
            }
            progressDialog.dismiss();
            listAdapter.notifyDataSetChanged();
        }
    };
    
    /*Function: onListItemClick
     * This built-in function is called when an item from  the list is clicked.
     * A new activity, ShowHistoryMap is started which display the location of the user on the map corresonding to the clicked item
     */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ListItem item = listAdapter.getItem(position);
		Toast.makeText(getActivity(), "OK...", Toast.LENGTH_SHORT).show();
		Log.i("LocalHistoryFragment","onListItemClick: Show map for latitude: "+ Double.valueOf(item.getLatitude().trim()).doubleValue()+", longitude: "+Double.valueOf(item.getLongitude().trim()).doubleValue());
		
		//progressDialog = ProgressDialog.show(getActivity(),"Please wait...", "Showing map...", true);
		
		Intent i = new Intent(getActivity(), ShowHistoryMap.class);
		i.putExtra("latitude", Double.valueOf(item.getLatitude().trim()).doubleValue());
		i.putExtra("longitude", Double.valueOf(item.getLongitude().trim()).doubleValue());
		//i.putExtra("progressDialog", progressDialog.toString());
		startActivity(i);
	}
    
    
    @Override
    public void onPause(){
    	super.onStop();

        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.activityMainButtons);
        Log.d("LocationHistoryFragment","onStop:orientation "+getResources().getConfiguration().orientation);
        if (layout != null)
        	layout.setVisibility(View.VISIBLE);
        
    }
    


	
    /*Function: getLocHist 
     * Reads the location details from db "GeoTrack"  and sets value in the ListItem list.
     * Inorder to preserve the cronological order while viewing the list, a list reversal is done. 
     * Finally, a thread is started to check for any change in the list. 
     */
	private void getLocList(){
		
		list = new ArrayList<ListItem>();
		ListItem l;
		
        try {
    		//read GeoTracker database
        	DatabaseHandler db = new DatabaseHandler(getActivity(), "GeoTrack", null, 9);
        	Cursor cursor = db.readDB();
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                	l= new ListItem();
                	l.setCity(cursor.getString(3)+","+cursor.getString(4));
                	l.setDate(cursor.getString(5));
                	l.setLatitude(String.valueOf(cursor.getDouble(1)));
                	l.setLongitude(String.valueOf(cursor.getDouble(2)));
                	list.add(l);
                	Log.i("LocationHistoryFragment","getLocHist:"+cursor.getInt(0));
                	Log.i("LocationHistoryFragment","getLocList: Retreiving History: "+l.getCity()+", "+l.getLatitude()+", "+l.getLongitude()+", "+l.getDate());
                } while (cursor.moveToNext());
            }
        	db.close();
        }catch (Exception e) {
        	e.printStackTrace();
        } 
        Collections.reverse(list);
        
        getActivity().runOnUiThread(returnRes);
	}
	
	
	/*Class: ListAdapter 
	 * 
	 * The class is for showing listItems
	 * Adapter classes are used to display customized Lists
	 * ListAdapter inflates list_item layout 
	 * Each list_item displays single row of class ListItem
	 * list_item has four TextViews (TextViews lat and lon are hidden)
	 * topText and bottomText shows (City, country) and Date respectively.
	 */
	private class ListAdapter extends ArrayAdapter<ListItem>{
	    private ArrayList<ListItem> items;
	    
	    public ListAdapter(Context context, int textViewResourceId,  ArrayList<ListItem> list ) {
	        super(context, textViewResourceId,  list);
	        this.items = list;
	    }

	    /*Function: getView
	     * (non-Javadoc)
	     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	     * 
	     * This function gets the view list_item and sets the content of each row. 
	     * 
	     * Getters are used to get items from db
	     * Thumbnail at the left side of each item is a bitmap image obtained  using google map api.
	     * 
	     */
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	
	    	if(convertView == null){
	    		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    		convertView = inflater.inflate(R.layout.list_item, parent,  false);
	    	}
	    	
	    	ListItem item = items.get(position);
	    	
	        if (item != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.topText);
	            TextView bt = (TextView) convertView.findViewById(R.id.bottomText);
	            TextView lat = (TextView) convertView.findViewById(R.id.latitude);
	            TextView lon = (TextView) convertView.findViewById(R.id.longitude);
	            if (tt != null) {
	                  tt.setText(item.getCity());          
	            }
	            if(bt != null){
	                  bt.setText(item.getDate());
	            }
	            if(lat != null){
	            	lat.setText(item.getLatitude());
	            }
	            if(lon != null){
	            	lon.setText(item.getLongitude());
	            }
	            
	            
	            //insert map thumbnail into list using bitmap image
	            ImageView thumb = (ImageView) convertView.findViewById(R.id.thumbnail);
	            Bitmap bitmap = null;
	            URL url = null;
	            try {
					url = new URL("http://maps.googleapis.com/maps/api/staticmap?center="+item.getLatitude()+","+item.getLongitude()+"&zoom=13&size=65x38&key=AIzaSyDiILtofrS6HGrttwWsDD7jUzFOsWZHVso&sensor=true");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
	            
	            try {
					bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
	            thumb.setImageBitmap(bitmap);
	            
	    }
	    return convertView;
	    	
	 
	    }

	    
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.i("LocationHistoryFragment","onDestroy");
	}
	

}
