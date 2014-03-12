package com.talis.puc2014;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

public class PUCPreferenceManager {

	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	private Context _context;
	
	// Shared pref file name
	private static final String PREF_NAME = "PUCPref";
	
	// keys used to store info
	private static final String KEY_DONEDATE = "doneDate";
	
	// Constructor
	public PUCPreferenceManager(Context context){
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		editor = pref.edit();
	}
	
	public void storeDoneDate() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); 

		editor.putString(KEY_DONEDATE, dateFormat.format(calendar.getTime()));		
		editor.commit();		
	}
		
	public Date getDoneDate() {
		String expiryString = pref.getString(KEY_DONEDATE, null);

		if (expiryString != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); 
			Date expiryDate = new Date();
			
			try {
				expiryDate = dateFormat.parse(expiryString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			return expiryDate;
		} else {
			return null;
		}
	}
}
