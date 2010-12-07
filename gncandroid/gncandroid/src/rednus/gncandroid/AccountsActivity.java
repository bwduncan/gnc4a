/**
 * Copyright (C) 2010 Rednus Limited
 *     http://www.rednus.co.uk
 *  
 * Project     : GNCAndroid
 * Package     : rednus.GNCAndroid
 * File        : AccountsActivity.java
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
public class AccountsActivity
		extends Activity {
	// implements OnItemClickListener {
	// TAG for this activity
	private static final String	TAG	= "AccountsActivity";
	// Aplication Reference
	private GNCAndroid			app;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
