/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * Project : GNCAndroid Package : rednus.GNCAndroid File : MainView.java
 * Description :
 */
package rednus.gncandroid;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import android.util.Log;

/**
 * This class defines what method should a data handler implement. It makes life
 * easy in future if SQLITE data handler is planned.
 * 
 * @author shyam.avvari
 * 
 */
public class GNCDataHandler {
	// TAG for this activity
	private static final String	TAG	= "GNCDataHandler";
	// Application
	private GNCAndroid					app;
	// Data Collection
	private DataCollection			gncData;
	/**
	 * Initialize Data handler
	 */
	public GNCDataHandler(GNCAndroid app, String dataFile, boolean compressed) {
		this.app = app;
		gncData = new DataCollection();
		readDataFile(dataFile, compressed);
	}
	public DataCollection getGncData() {
		return gncData;
	}
	private void readDataFile(String filePath, boolean compressed) {
		FileInputStream inStream;
		GZIPInputStream gzStream;
		DomDataParser parser;
		// Get the input stream
		try {
			inStream = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			Log.i(TAG, "File " + filePath + " not found...");
			throw new RuntimeException(e);
		}
		// check if it is compressed then get gzip stream
		if (compressed)
			try {
				gzStream = new GZIPInputStream(inStream);
				parser = new DomDataParser(gzStream, gncData);
			} catch (IOException e) {
				Log.i(TAG, "File " + filePath + " cannot be read...");
				throw new RuntimeException(e);
			}
		else
			parser = new DomDataParser(inStream, gncData);
		//parse data
		parser.parse();
	}
	//
	public class DataCollection {
		// book information
		public Book									book			= new Book();
		// all data types
		public Map<String, Account>	accounts	= new HashMap<String, Account>();
		/**
		 * This method should be called once all the data has been filled. It does
		 * the following: 1. Add children data to accounts 2. Calculate account
		 * balances 3. #
		 */
		public void completeCollection() {
			if (app.localLOGV)
				Log.i(TAG, "Calculating Data...");
			// find children
			// for (String childGUID : accounts.keySet())
			// ((Account) accounts.get(accounts.get(childGUID).parentGUID)).subList
			// .add(childGUID);
			if (app.localLOGV)
				Log.i(TAG, "Calculating Data...Done");
		}
	}
	public class Book {
		// configuration data
		public String	bookGUID;
		public String	compName;
		public String	compId;
		public String	compAddr;
		public String	compEmail;
		public String	compUrl;
		public String	compFax;
		public String	compContact;
		public String	compPhone;
		public String	defCustTaxTable;
		public String	defVendTaxTable;
		public int		cntAccount;
		public int		cntTransaction;
		public int		cntSchedxaction;
		public int		cntJob;
		public int		cntInvoice;
		public int		cntCustomer;
		public int		cntBillTerm;
		public int		cntTaxTable;
		public int		cntEmployee;
		public int		cntEntry;
		public int		cntVendor;
		public String	cmdtySpace;
		public String	cmdtyId;
		public String	rootAccount;
	}
	public class Commodity {
		public String	space;
		public String	id;
	}
	public class Account {
		// fields for an account
		public String				type;
		public String				GUID;
		public String				parentGUID;
		public String				name;
		public String				notes;
		public String				description;
		public String				code;
		public boolean			placeholder;
		public Commodity		currency;
		// calculated balance amount
		public double				balance;
		// transactions that belong to account
		public List<String>	trans		= new ArrayList<String>();
		// id's of child-accounts
		public List<String>	subList	= new ArrayList<String>();
	}
}
