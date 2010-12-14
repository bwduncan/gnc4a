/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * #TODO License
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
public class AccountsActivity extends Activity {
	// implements OnItemClickListener {
	private static final String	TAG	= "AccountsActivity"; // TAG for this activity
	private GNCAndroid					app;											// Application Reference
	/*
	 * When activity is started, and if Data file is already read, then display
	 * account information tree.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get application
		app = (GNCAndroid) getApplication();
		if (app.localLOGV)
			Log.i(TAG, "Activity created");
		// #TODO display account tree
		// temp
		TextView t = new TextView(this);
		t.setText("Accounts");
		setContentView(t);
		// done
		if (app.localLOGV)
			Log.i(TAG, "Activity Finished");
	}
}
