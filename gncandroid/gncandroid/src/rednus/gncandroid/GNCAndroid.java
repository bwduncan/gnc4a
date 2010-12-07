/**
 * Copyright (C) 2010 Rednus Limited
 *     http://www.rednus.co.uk
 *  
 * Project     : GNCAndroid
 * Package     : rednus.GNCAndroid
 * File        : GNCAndroid.java
 * Description : 
 */
package rednus.gncandroid;
import android.app.Application;
import android.content.res.Resources;
import android.util.Log;
/**
 * @author shyam.avvari
 * 
 */
public class GNCAndroid
		extends Application {
	// TAG for this activity
	private static final String	TAG			= "GNCAndroid";
	public static final String	SPN			= "gnc4aprefs";
	// Log information boolean
	public boolean				localLOGV	= true;
	public Resources			res;
	private String				dataFile	= null;
	private boolean				gzipFile	= true;
	// public GNCDataHandler dataHandler;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		// keep a copy of resources for later use in application
		res = getResources();
		// read shared preferences to get data file
		dataFile = this.getSharedPreferences(SPN, MODE_PRIVATE).getString(
				res.getString(R.string.pref_data_file_key), null);
		if (localLOGV)
			Log.i(TAG, "Data file is " + dataFile);
		// dataHandler = new GNCDataHandler(this);
	}
	/**
	 * @return the dataFile
	 */
	public String getDataFile() {
		return dataFile;
	}
	/**
	 * @param dataFile
	 *            the dataFile to set
	 */
	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}
}
