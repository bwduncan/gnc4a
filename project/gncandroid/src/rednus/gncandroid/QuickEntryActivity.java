/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * #TODO License
 */
package rednus.gncandroid;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeMap;

import rednus.gncandroid.GNCDataHandler.Account;
import rednus.gncandroid.GNCDataHandler.DataCollection;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;
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
	private DataCollection		dc;
	
	static final int DATE_DIALOG_ID = 0;
	
	private int currentView = 0;
	
    private int mYear;
    private int mMonth;
    private int mDay;
    
	private AutoCompleteTextView mDescription;
	private Spinner mTo;
	private Spinner mFrom;
	private EditText mAmount;
	private Button dateButton;
	private Spinner transtypeSpinner;
	private String[] accounts;
	private String[] descs;
	ArrayList<String> account_array = new ArrayList<String>();


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
        
		dc = app.gncDataHandler.getGncData();
        constructAccountList(dc.book.rootAccountGUID);

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
					mAmount.setText("");
				}
			}
		});
     
		// add log entry
		if (app.localLOGV)
			Log.i(TAG, "Activity Finished");
	}
	
	private void constructAccountList(String rootGUID)
	{
		getListData(rootGUID, "");
		accounts = new String[account_array.size()];
		account_array.toArray(accounts);
	}
	
	private void getListData(String rootGUID, String prefix) {
		String subGUID;
		// get root account
		Account root = dc.accounts.get(rootGUID);
		if (null == root)
			return;
		// clear current list
		// Add root as Top - only if not Root Account
		if (!root.name.contains("Root"))
		{
			account_array.add(prefix + root.name);
			prefix = prefix + root.name + ":";
		}
		// Read data and fill list
		Iterator it = root.subList.iterator();
		while (it.hasNext()) {
			subGUID = (String) it.next();
			getListData(subGUID, prefix);
		}
	}

	private void setupTransferControls() {
		mDescription = (AutoCompleteTextView) findViewById(R.id.EditTextDescriptoin);
		mTo = (Spinner) findViewById(R.id.spinner_to);
		mFrom = (Spinner) findViewById(R.id.spinner_from);
		mAmount = (EditText) findViewById(R.id.amount);
		dateButton = (Button) findViewById(R.id.ButtonDate);

		TreeMap<String,String> toAccounts = app.gncDataHandler.GetAccountList(true, false);
		String[] toAccountNames = new String[toAccounts.size()];
		toAccounts.keySet().toArray(toAccountNames);
		ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, toAccountNames);
		toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTo.setAdapter(toAdapter);

		TreeMap<String,String> fromAccounts = app.gncDataHandler.GetAccountList(false, false);
		String[] fromAccountNames = new String[fromAccounts.size()];
		fromAccounts.keySet().toArray(fromAccountNames);
		ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fromAccountNames);
		fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mFrom.setAdapter(fromAdapter);
		
		descs = app.gncDataHandler.GetTransactionDescriptions();
		ArrayAdapter<String> descAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, descs);
		mDescription.setAdapter(descAdapter);
		
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

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        	
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
    
    public class DescriptionAutoComplete extends AutoCompleteTextView {
		
		public DescriptionAutoComplete(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

    	@Override
		public void onCommitCompletion(CompletionInfo completion) {
			
			Toast.makeText(QuickEntryActivity.this, completion.getText(), Toast.LENGTH_LONG).show();
		}
    	
    }
   
}
