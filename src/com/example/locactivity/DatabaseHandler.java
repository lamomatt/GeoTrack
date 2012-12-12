package com.example.locactivity;

/*Class: DatabaseHandler
 * This class handles all the database operation. The class is designed to keep all the database transactions. 
 * 
 * Functions:
 * DatabaseHandler -> opens a writable database
 * writeDB -> write contents of location(city, country, latitude, longitude, date) into db
 * deleteEntry -> this function deletes the first entry from db. This is to keep only 20 histories in db
 * readDB -> Read the contents from db
 * descTable -> get a description of table
 * rowCount -> returns number of rows in the table.
 */
import java.sql.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	private String latitude="Latitude";
	private String longitude = "Longitude";
	private String date = "Date";
	private String country = "Country";
	private String city = "City";
	private String tableName = "Locdata";
	private String tableId = "LocdataId";
	SQLiteDatabase db;

	public DatabaseHandler(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		db = this.getWritableDatabase();
		Log.d("DatabaseHandler", "DatabaseHandler");
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String createQuery = "create table if not exists "+tableName+" (id integer primary key autoincrement, "+latitude+" double, "+longitude+" double, "+country+" Text, "+city+" Text, "+date+" Text)";
		try{
		db.execSQL(createQuery);
		Log.i("DatabaseHandler","onCreate: Table created");
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	//inserting values into locdata
	public void writeDB(double lat, double lon, String entryDate, String countryName, String cityName){
		Log.i("DatabaseHandler","writeDB: "+entryDate+" "+countryName+" "+cityName);
		ContentValues cv = new ContentValues();
		cv.put(latitude, lat);
		cv.put(longitude, lon);
		cv.put(country, countryName);
		cv.put(city, cityName);
		cv.put(date, entryDate);
		db.insert(tableName, tableId, cv);
	}
	
	//delete first entry of table
	public void deleteEntry(){
		Log.i("DatabaseHandler","deleteEntry: deleting 1st entry");
		db.delete(tableName, "id = (select min(id) from "+tableName+")", null);
	}
	
	//read the whole table
	public Cursor readDB(){
		String readQuery = "select * from "+ tableName;
		Cursor cursor = db.rawQuery(readQuery, null);
        return cursor;
	}
	
	public void descTable(){
		String descTable = "PRAGMA table_info("+tableName+")";
		Log.d("DatabaseHandler", "descTable");
		Cursor cursor = db.rawQuery(descTable, null);
        if (cursor.moveToFirst()) {
            do {
            	Log.d("Databasehandler","descTable: "+cursor.getString(0) );
            	Log.d("Databasehandler","descTable: "+cursor.getString(1) );
            	Log.d("Databasehandler","descTable: "+cursor.getString(2) );
            	Log.d("Databasehandler","descTable: "+cursor.getString(3) );
            	Log.d("Databasehandler","descTable: "+cursor.getString(4) );
            	Log.d("Databasehandler","descTable: "+cursor.getString(5) );
            } while (cursor.moveToNext());
        }
		
	}
	
	public int rowCount(){
		int count=0;
		String query = "select count(*) from "+tableName;
		Cursor cursor = db.rawQuery(query, null);
		if(cursor.moveToFirst()){
			do{
				Log.i("DatabaseHandler","rowCount: "+cursor.getInt(0));
				count = cursor.getInt(0);
			}while(cursor.moveToNext());
				
		}
		
		return(count);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("DatabaseHandler","onUpgrade: dropping tables");
		db.execSQL("drop table if exists "+tableName);
		onCreate(db);
	}
	
}
