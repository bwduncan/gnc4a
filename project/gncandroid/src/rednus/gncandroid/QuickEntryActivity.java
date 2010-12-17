/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * #TODO License
 */
package rednus.gncandroid;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.AdapterView.OnItemSelectedListener;
/**
 * This class displays Quick entry screen.
 * #TODO add docu when finished. 
 * 
 * @author John Gray
 * 
 */
public class QuickEntryActivity
		extends Activity {
	// TAG for this activity
	private static final String	TAG	= "QuickEntryActivity";
	// Log information boolean
	private GNCAndroid			app;
	
	static final int DATE_DIALOG_ID = 0;
	
	private int currentView = 0;
	
    private int mYear;
    private int mMonth;
    private int mDay;
    
	private AutoCompleteTextView mDescription;
	private AutoCompleteTextView mTo;
	private AutoCompleteTextView mFrom;
	private EditText mAmount;
	private Button dateButton;
	private Spinner transtypeSpinner;

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
		setContentView(R.layout.quickentry);

		Button saveButton = (Button) findViewById(R.id.ButtonSave);
		Button clearButton = (Button) findViewById(R.id.ButtonClear);
		
		transtypeSpinner = (Spinner) findViewById(R.id.transtype_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        		QuickEntryActivity.this, R.array.transtype_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transtypeSpinner.setAdapter(adapter);
         
        transtypeSpinner.setOnItemSelectedListener(new TransTypeOnItemSelectedListener());        

        setupTransferControls();
        
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Save goes here
			}
		});
		
		clearButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if ( currentView == 0 ) {
			        final Calendar c = Calendar.getInstance();
					dateButton.setText(DateFormat.format("MM/dd/yyyy", c));
					
					mDescription.setText("");
					mTo.setText("");
					mFrom.setText("");
					mAmount.setText("0.00");
				}
			}
		});
		
     
		// add log entry
		if (app.localLOGV)
			Log.i(TAG, "Activity Finished");
	}

	private void setupTransferControls() {
		mDescription = (AutoCompleteTextView) findViewById(R.id.EditTextDescriptoin);
		mTo = (AutoCompleteTextView) findViewById(R.id.to);
		mFrom = (AutoCompleteTextView) findViewById(R.id.from);
		mAmount = (EditText) findViewById(R.id.amount);
		dateButton = (Button) findViewById(R.id.ButtonDate);

		/*  TODO: Tied the auto completes to data from the xml file
		ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(this, R.layout.list_item, mDbHelper.getAllAccounts());
		mTo.setAdapter(accountAdapter);
		mFrom.setAdapter(accountAdapter);
		
		mTransactions = mDbHelper.getAllTransctions();
		ArrayAdapter<String> transactionAdapter = new ArrayAdapter<String>(this, R.layout.list_item, mTransactions);
		mDescription.setAdapter(transactionAdapter);
		*/
		
        // get the current date
        final Calendar c = Calendar.getInstance();
		dateButton.setText(DateFormat.format("MM/dd/yyyy", c));
		
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

		dateButton.setOnClickListener(new View.OnClickListener() {
		      public void onClick(View v) {
		    	  showDialog(DATE_DIALOG_ID);
		      }
		    });
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	                    mDateSetListener,
	                    mYear, mMonth, mDay);
	    }
	    return null;
	}
	
    // the call back received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
            		
            		Button dateButton = (Button) findViewById(R.id.ButtonDate);

            		dateButton.setText(new StringBuilder()
	                    // Month is 0 based so add 1
	                    .append(mMonth + 1).append("/")
	                    .append(mDay).append("/")
	                    .append(mYear));
	                }
            };	
            
    public class TransTypeOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        	
        	if ( currentView != pos )
        	{
        		currentView = pos;
        		
	            TableLayout field_table = (TableLayout)findViewById(R.id.field_table);
	            field_table.removeAllViews();
	 
	            // Create new LayoutInflater - this has to be done this way, as you can't directly inflate an XML without creating an inflater object first
	            LayoutInflater inflater = getLayoutInflater();
	 
	 	       	switch ( pos )
		        	{
		        	case 0:
		                field_table.addView(inflater.inflate(R.layout.transfer, null));
		                setupTransferControls();
		                break;
		        	case 1:
		                field_table.addView(inflater.inflate(R.layout.invoice, null));
		                break;
		        	case 2:
		                field_table.addView(inflater.inflate(R.layout.expensevoucher, null));
		                break;
		        	}
	 			
	 			transtypeSpinner = (Spinner) findViewById(R.id.transtype_spinner);
	 	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	 	        		QuickEntryActivity.this, R.array.transtype_array, android.R.layout.simple_spinner_item);
	 	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	 	        transtypeSpinner.setAdapter(adapter);
	 	        transtypeSpinner.setSelection(pos);
	 	        
	 	        transtypeSpinner.setOnItemSelectedListener(new TransTypeOnItemSelectedListener());        
        	}

        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
   
}
