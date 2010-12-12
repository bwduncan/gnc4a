/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * Project : GNCAndroid Package : rednus.GNCAndroid File : GNCAndroid.java
 * Description :
 */
package rednus.gncandroid;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author shyam.avvari
 * 
 */
public class GNCAndroid extends Application implements
		OnSharedPreferenceChangeListener {
	// TAG for this activity
	private static final String	TAG					= "GNCAndroid";
	public static final String	SPN					= "gnc4aprefs";
	public static final boolean	localLOGV		= true;
	// Log information boolean
	public Resources						res;
	public GNCDataHandler				gncData;
	private String							dataFile		= null;
	private boolean							gzipFile		= false;
	private boolean							reloadFile	= false;
	//
	public String getDataFile() {
		return dataFile;
	}
	public boolean isReloadFile() {
		return reloadFile;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		// keep a copy of resources for later use in application
		res = getResources();
		// read preferences
		this.readPreferences();
	}
	private void readPreferences() {
		SharedPreferences sp = getSharedPreferences(SPN, MODE_PRIVATE);
		// set listener to this
		sp.registerOnSharedPreferenceChangeListener(this);
		// read shared preferences to get data file
		dataFile = sp.getString(res.getString(R.string.pref_data_file_key), null);
		if (localLOGV)
			Log.i(TAG, "Data file is " + dataFile);
		if (dataFile == null)
			return;
		// read if data file is compressed
		gzipFile = sp
				.getBoolean(res.getString(R.string.pref_data_file_gzip), false);
	}
	private void setDataFileChanged(String newPath) {
		// set file to be reloaded if changed
		if ((null == dataFile && !newPath.equals(""))
				|| (null != dataFile && !dataFile.equals(newPath)))
			this.reloadFile = true;
		// add log
		if (localLOGV)
			Log.i(TAG, "Reload file set to " + String.valueOf(reloadFile));
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		if (localLOGV)
			Log.i(TAG, "Pref " + key + "changed... reading new value...");
		if (key.equals(res.getString(R.string.pref_data_file_key))) {
			String newPath = sp.getString(res.getString(R.string.pref_data_file_key),
					null);
			setDataFileChanged(newPath);
			dataFile = newPath;
		} else if (key.equals(res.getString(R.string.pref_data_file_gzip)))
			gzipFile = sp.getBoolean(res.getString(R.string.pref_data_file_gzip),
					false);
	}
	public boolean readData() {
		if (null == dataFile)
			return false;
		if (localLOGV)
			Log.i(TAG, "Reading Data...");
		gncData = new GNCDataHandler(this, dataFile, gzipFile);
		return true;
	}
}
