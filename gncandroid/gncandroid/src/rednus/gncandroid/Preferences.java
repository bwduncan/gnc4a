/**
 * Copyright (C) 2010 Rednus Limited
 *     http://www.rednus.co.uk
 *  
 * Project     : GNCAndroid
 * Package     : rednus.GNCAndroid
 * File        : Preferences.java
 * Description : 
 */
package rednus.gncandroid;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
/**
 * @author shyam
 * 
 */
public class Preferences
		extends PreferenceActivity {
	// TAG for this activity
	private static final String	TAG				= "Preferences";
	private static final int	RESULT_FOR_FILE	= 7;
	private GNCAndroid			app;
	private String				pref_data_file_key;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (GNCAndroid) getApplication();
		if (app.localLOGV)
			Log.i(TAG, "Showing Preferences screen..");
		//set activity title
		setTitle(getString(R.string.app_name) + " > " + app.res.getString(R.string.menu_prefs));
		// set preferences file name
		this.getPreferenceManager().setSharedPreferencesName(app.SPN);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		// get custom field to capture on click
		pref_data_file_key = app.res.getString(R.string.pref_data_file_key);
		Preference dataFilePref = this.findPreference(pref_data_file_key);
		dataFilePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				if (pref.getKey().equalsIgnoreCase(pref_data_file_key)) {
					if (app.localLOGV)
						Log.i(TAG, "Clicked custom field.. show file chooser");
					Intent intent = new Intent("rednus.GNCAndroid.action.FILECHOOSER");
					startActivityForResult(intent, RESULT_FOR_FILE);
					return true;
				}
				return false;
			}
		});
		//set version dialog
		String pref_version_key = app.res.getString(R.string.pref_version_key);
		Preference versionPref = this.findPreference(pref_version_key);
		versionPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference pref){
				new AlertDialog.Builder(Preferences.this)
					//.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.pref_app_version)
					.setMessage(R.string.app_version_history)
					.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					})
					.create()
					.show();
				return true;
			}
		});
		//set version dialog
		String pref_about_key = app.res.getString(R.string.pref_about_key);
		Preference aboutPref = this.findPreference(pref_about_key);
		aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference pref){
				new AlertDialog.Builder(Preferences.this)
					//.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.pref_app_about)
					.setMessage(R.string.app_about_text)
					.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					})
					.create()
					.show();
				return true;
			}
		});
		// check if not already set then pass value
		if (app.getDataFile() != null)
			dataFilePref.setSummary(app.getDataFile());
	}
	/*
	 * Gets return from FileCHooser activity for selected file name
	 * 
	 * @see android.preference.PreferenceActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_FOR_FILE && resultCode == RESULT_OK) {
			String path = (String) data.getExtras().get(pref_data_file_key);
			if (app.localLOGV)
				Log.i(TAG, "Got path " + path + " from file chooser..");
			// set file name to summary
			this.findPreference(pref_data_file_key).setSummary(path);
			//set preference value
			this.findPreference(pref_data_file_key).getEditor().putString(pref_data_file_key, path).commit();
			// now copy path to application
			app.setDataFile(path);
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//here finish the activity to free memory
		if (app.localLOGV)
			Log.i(TAG, "Activity Exited");
		this.finish();
	}
}
