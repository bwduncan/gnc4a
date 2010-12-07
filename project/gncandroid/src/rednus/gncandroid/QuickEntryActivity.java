/**
 * Copyright (C) 2010 Rednus Limited
 *     http://www.rednus.co.uk
 *  
 * Project     : GNCAndroid
 * Package     : rednus.GNCAndroid
 * File        : QuickEntryActivity.java
 * Description : 
 */
package rednus.gncandroid;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
/**
 * @author shyam.avvari
 * 
 */
public class QuickEntryActivity
		extends Activity {
	// TAG for this activity
	private static final String	TAG	= "QuickEntryActivity";
	// Log information boolean
	private GNCAndroid			app;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get application
		app = (GNCAndroid) getApplication();
		// add log entry
		if (app.localLOGV)
			Log.i(TAG, "Activity created");
		// Temp
		TextView t = new TextView(this);
		t.setText("QuickEntry");
		setContentView(t);
		// add log entry
		if (app.localLOGV)
			Log.i(TAG, "Activity Finished");
	}
}
