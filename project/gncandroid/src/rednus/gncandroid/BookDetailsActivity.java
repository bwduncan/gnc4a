/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * #TODO License
 */
package rednus.gncandroid;

import rednus.gncandroid.GNCDataHandler.DataCollection;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * This class displays opened Book details
 * 
 * @author avvari.shyam
 * 
 */
public class BookDetailsActivity extends Activity {
	private static final String TAG = "BookDetailsActivity";
	private GNCAndroid app;

	/*
	 * When activity is started, and if Data file is already read, then display
	 * account information tree.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (GNCAndroid) getApplication();
		if (app.localLOGV)
			Log.i(TAG, "Showing Book Details screen..");
		// set activity title
		setTitle(getString(R.string.app_descr) + " > "
				+ app.res.getString(R.string.menu_book));
		// set view
		setContentView(R.layout.bookdetails);
		// set column 1 to shrinkable
		((TableLayout) findViewById(R.id.book_details_table))
				.setColumnShrinkable(1, true);
		// now set data
		setFieldValues();
	}

	private void setFieldValues() {
		// get data file
		((TextView) this.findViewById(R.id.data_file_name)).setText(app
				.getDataFilePath());
		// get data collection
		DataCollection gncData = app.gncDataHandler.getGncData();
		// set book version
		((TextView) this.findViewById(R.id.book_version))
				.setText(gncData.book.version);
		// set company details
		((TextView) this.findViewById(R.id.comp_name))
				.setText(gncData.book.compName);
		((TextView) this.findViewById(R.id.comp_id))
				.setText(gncData.book.compId);
		((TextView) this.findViewById(R.id.comp_addr))
				.setText(gncData.book.compAddr);
		((TextView) this.findViewById(R.id.comp_email))
				.setText(gncData.book.compEmail);
		((TextView) this.findViewById(R.id.comp_url))
				.setText(gncData.book.compUrl);
		((TextView) this.findViewById(R.id.comp_phone))
				.setText(gncData.book.compPhone);
		((TextView) this.findViewById(R.id.comp_fax))
				.setText(gncData.book.compFax);
		((TextView) this.findViewById(R.id.comp_contact))
				.setText(gncData.book.compContact);
	}
}
