/**
 * Copyright (C) 2010 Rednus Limited
 *     http://www.rednus.co.uk
 *  
 * Project     : GNCAndroid
 * Package     : rednus.GNCAndroid
 * File        : MainView.java
 * Description : 
 */
package rednus.gncandroid;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;
/**
 * @author shyam.avvari
 * 
 */
public class MainView 
		extends TabActivity {
	// TAG for this activity
	private static final String	TAG	= "MainView";
	// Aplication Reference
	private GNCAndroid			app;
	// progress bar
	private ProgressDialog		pd;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get application
		app = (GNCAndroid) getApplication();
		// first check if data file is set otherwise show preferences
		if (null == app.getDataFile()) {
			// add log entry
			if (app.localLOGV)
				Log.i(TAG, "No Data file set.. Forcing preferences...");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(app.res.getString(R.string.message_set_data_file))
					.setCancelable(false)
					.setPositiveButton(app.res.getString(R.string.button_text_ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// show prefs
									Intent i = new Intent(getBaseContext(), Preferences.class);
									startActivity(i);
									return;
								}
							});
			builder.create().show();
			return;
		}
		// add log entry
		if (app.localLOGV)
			Log.i(TAG, "Activity Started");
		// Read data
		pd = ProgressDialog.show(this, "Please Wait...", "Loading...", true);
		// app.dataHandler.readGNCData();
		// The activity TabHost
		final TabHost tabHost = getTabHost();
		// remove all tabs
		tabHost.clearAllTabs();
		// Reusable TabSpec for each tab
		TabHost.TabSpec spec;
		// Reusable Intent for each tab
		Intent intent;
		// add accounts tab
		intent = new Intent().setClass(this, AccountsActivity.class);
		spec = tabHost
				.newTabSpec("accounts")
				.setIndicator(getString(R.string.ic_tab_accounts),
						app.res.getDrawable(R.drawable.ic_tab_accounts)).setContent(intent);
		tabHost.addTab(spec);
		// add quick tab
		intent = new Intent().setClass(this, QuickEntryActivity.class);
		spec = tabHost
				.newTabSpec("quick")
				.setIndicator(getString(R.string.ic_tab_quick),
						app.res.getDrawable(R.drawable.ic_tab_actions)).setContent(intent);
		tabHost.addTab(spec);
		// // // add actions tab
		// // intent = new Intent().setClass(this, ActionsActivity.class);
		// // spec = tabHost
		// // .newTabSpec("actions")
		// // .setIndicator(getString(R.string.ic_tab_actions),
		// // app.res.getDrawable(R.drawable.ic_tab_actions))
		// // .setContent(intent);
		// // tabHost.addTab(spec);
		// set default tab
		tabHost.setCurrentTab(0);
		// add log entry
		if (app.localLOGV)
			Log.i(TAG, "Activity Finished");
		pd.dismiss();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		// add log entry
		if (app.localLOGV)
			Log.i(TAG, "Activity Restarted");
		// replay created event so checks for file and shows screen
		this.onCreate(new Bundle());
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//we dont want the app to be running so close it
		this.finish();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_prefs:
			// show prefs
			Intent i = new Intent(getBaseContext(), Preferences.class);
			startActivity(i);
			return true;
		case R.id.menu_save:
			// Save data
			return true;
		case R.id.menu_discard:
			// cancel changes and reload - but ask before doing
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
