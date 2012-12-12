/*Class: ListItem
 * class for getting and setting City, Date, Latitude and Longitude
 * Getters and Setters are used for processing each item in the List
 * 
 * 
 * Function LocationHistoryFragment.getLocHist sets the value of each item
 * 
 * Class ListAdapter gets the values and display as TextViews in List contained in Fragment
 * 
 * 
 * 
 */

package com.example.locactivity;

public class ListItem {
	private String city;
	private String date;
	
	private String latitude;
	private String longitude;
	
	public String getCity(){
		return city;
	}
	
	public void setCity(String city){
		this.city=city;
	}
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String date){
		this.date=date;
	}
	
	public String getLatitude(){
		return latitude;
	}
	
	public void setLatitude(String latitude){
		this.latitude = latitude;
	}
	
	public String getLongitude(){
		return longitude;
	}
	
	public void setLongitude(String longitude){
		this.longitude = longitude;
	}
	
}
