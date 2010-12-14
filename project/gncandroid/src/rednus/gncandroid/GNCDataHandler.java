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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import android.util.Log;

/**
 * This class implements methods to read data file and create a data collection
 * and also methods to update the data file.
 * 
 * @author shyam.avvari
 * 
 */
public class GNCDataHandler {
	private static final String	TAG	= "GNCDataHandler"; // TAG for this activity
	private GNCAndroid					app;										// Application
	private DataCollection			gncData;								// Data Collection
	/**
	 * On create the handler create new DataCollection, create input stream for
	 * file and depending on the parser used the data will be parsed.
	 * 
	 * @param app
	 *          GNCAndroid Application reference
	 * @param dataFile
	 *          String containing path to data file
	 * @param compressed
	 *          Boolean to specify if the data file is compressed or not
	 * @param parserType
	 *          Type of parser to be used
	 */
	public GNCDataHandler(GNCAndroid app, String dataFile, boolean compressed,
			String parserType) {
		this.app = app;
		// create new collection
		gncData = new DataCollection();
		// get file reader
		InputStream inStream = getInputStream(dataFile, compressed);
		// depending on parser type
		if (parserType.equals("DOM"))
			readFileUsingDOM(inStream);
		if (parserType.equals("SAX"))
			readFileUsingSAX(inStream);
	}
	/**
	 * Returns the data collection object.
	 */
	public DataCollection getGncData() {
		return gncData;
	}
	/**
	 * This method returns the InputStream. If the compressed flag is set then the
	 * return is of GZipInputStream type, otherwise FileInputStream.
	 * 
	 * @param filePath
	 * @param compressed
	 * @return
	 */
	private InputStream getInputStream(String filePath, boolean compressed) {
		InputStream inStream;
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
				inStream = new GZIPInputStream(inStream);
			} catch (IOException e) {
				Log.i(TAG, "Gzip File " + filePath + " cannot be read...");
				throw new RuntimeException(e);
			}
		return inStream;
	}
	/**
	 * This method creates DomDataParser and parses the data (Slow processing but
	 * full functionality)
	 * 
	 * @param inStream
	 */
	private void readFileUsingDOM(InputStream inStream) {
		// create parser
		DomDataParser parser = new DomDataParser(inStream, gncData);
		// parse data
		parser.parse();
	}
	/**
	 * This method creates SAXDataParser and parses the data (Fast processing but
	 * less functionality)
	 * 
	 * @param inStream
	 */
	private void readFileUsingSAX(InputStream inStream) {
		// create parser class
		// parse data
	}
	//
	/**
	 * This class is a collection of all gnc data objects.
	 * 
	 * @author shyam.avvari
	 * 
	 */
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
