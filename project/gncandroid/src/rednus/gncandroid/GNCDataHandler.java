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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	private GNCAndroid			app;					// Application
	private DataCollection		gncData;				// Data Collection
	private SQLiteDatabase	    sqliteHandle;
	/**
	 * On create the handler create new DataCollection, create input stream for
	 * file and depending on the parser used the data will be parsed.
	 * 
	 * @param app
	 *            GNCAndroid Application reference
	 * @param dataFile
	 *            String containing path to data file
	 * @param compressed
	 *            Boolean to specify if the data file is compressed or not
	 */
	public GNCDataHandler(GNCAndroid app, String dataFile, boolean compressed) {
		this.app = app;
		// get file reader
		/*
		InputStream inStream = getInputStream(dataFile, compressed);
		this.readFile(inStream);
		*/
		try
		{
			sqliteHandle = SQLiteDatabase.openDatabase(dataFile,null,SQLiteDatabase.OPEN_READWRITE);
			gncData = new DataCollection();
			Cursor cursor = sqliteHandle.rawQuery("select * from books",null);
	        if(cursor.getCount() >0)
	        {
	            if (cursor.moveToNext())
	            {
	            	// CREATE TABLE books (guid text(32) PRIMARY KEY NOT NULL, root_account_guid text(32) NOT NULL, root_template_guid text(32) NOT NULL);
	            	gncData.book.GUID = cursor.getString(cursor.getColumnIndex("guid"));
	            	gncData.book.rootAccountGUID = cursor.getString(cursor.getColumnIndex("root_account_guid"));
	             }
	        }
	        cursor.close();
		
			//cursor = sqliteHandle.rawQuery("select accounts.*,sum(CAST(value_num AS REAL)/value_denom) as bal from accounts,transactions,splits where splits.tx_guid=transactions.guid and splits.account_guid=accounts.guid and hidden=0 group by accounts.name",null);
			//cursor = sqliteHandle.rawQuery("select *,sum(CAST(value_num AS REAL)/value_denom) as bal from accounts left outer join splits on splits.account_guid=accounts.guid group by accounts.name",null);
			cursor = sqliteHandle.rawQuery("select * from accounts",null);
	        if(cursor.getCount() >0)
	        {
	            while (cursor.moveToNext())
	            {
	            	Account account = new Account();
	            	// CREATE TABLE accounts (guid text(32) PRIMARY KEY NOT NULL, name text(2048) NOT NULL, account_type text(2048) NOT NULL, commodity_guid text(32), commodity_scu integer NOT NULL, non_std_scu integer NOT NULL, parent_guid text(32), code text(2048), description text(2048), hidden integer, placeholder integer);
	            	account.GUID = cursor.getString(cursor.getColumnIndex("guid"));
	            	account.name = cursor.getString(cursor.getColumnIndex("name"));
	            	account.type = cursor.getString(cursor.getColumnIndex("account_type"));
	            	account.parentGUID = cursor.getString(cursor.getColumnIndex("parent_guid"));
	            	account.code = cursor.getString(cursor.getColumnIndex("code"));
	            	account.description = cursor.getString(cursor.getColumnIndex("description"));
	            	account.placeholder = cursor.getInt(cursor.getColumnIndex("placeholder"))!=0;
	            	
	            	gncData.accounts.put(account.GUID, account);
	             }
	        }
	        cursor.close();

	        
	        gncData.completeCollection();
		}
		catch (Exception e )
		{
			Log.e(TAG, e.getStackTrace().toString());
		}

	}
	
	public Account GetAccount(String GUID) {
		Account account = gncData.accounts.get(GUID);
		if ( account == null )
			return null;

		if ( account.balance == null )
			account.balance = this.AccountBalance(account.GUID);
		
		return account;
	}
	
	public Account AccountFromCursor(Cursor cursor) {
		return GetAccount(cursor.getString(cursor.getColumnIndex("guid")));
	}
	
	public Double AccountBalance(String GUID) {
		Cursor cursor = sqliteHandle.rawQuery("select accounts.*,sum(CAST(value_num AS REAL)/value_denom) as bal from accounts,transactions,splits where splits.tx_guid=transactions.guid and splits.account_guid=accounts.guid and accounts.guid='"+GUID+"' group by accounts.name",null);
        Double retVal = 0.0;
		if(cursor.getCount() >0)
        {
            if (cursor.moveToNext())
            {
            	int balIndex = cursor.getColumnIndex("bal");
            	if ( !cursor.isNull(balIndex) )
            		retVal = cursor.getDouble(balIndex);
             }
        }
        cursor.close();
		return retVal;
	}
	
	public TreeMap<String,String> GetAccountList(boolean expense, boolean longNames) {
		String query;
		String types;
		if ( expense )
			types = "'EXPENSE'";
		else
			types = "'CREDIT', 'BANK'";
		
		query = "select guid, name from accounts where account_type in ("+ types +") and hidden=0 and non_std_scu=0 order by name";

		Cursor cursor = sqliteHandle.rawQuery(query,null);
        if(cursor.getCount() >0)
        {
        	TreeMap<String, String> listData = new TreeMap<String, String>();
            while (cursor.moveToNext())
            {
            	Account account = this.AccountFromCursor(cursor);
            	
            	if ( longNames )
            		listData.put(account.fullName, account.GUID);
            	else
            		listData.put(account.name, account.GUID);

            }
            cursor.close();	
            
            return listData;
        }
        else
        	return null;
	
	}
	
	public TreeMap<String, Account> GetSubAccounts(String rootGUID) {
		Cursor cursor = sqliteHandle.rawQuery("select * from accounts where parent_guid='"+rootGUID+"' and hidden=0 and account_type!='EQUITY' and account_type!='EXPENSE' and account_type!='INCOME' order by name",null);
        if(cursor.getCount() >0)
        {
        	TreeMap<String, Account> listData = new TreeMap<String, Account>();
        	Account rootAccount = this.GetAccount(rootGUID);
    		if (!rootAccount.name.contains("Root"))
    			listData.put(rootGUID,rootAccount);
            while (cursor.moveToNext())
            {
            	Account account = this.AccountFromCursor(cursor);
            	
            	if ( account.hasChildren || ((int)(account.balance*100.0)) != 0 )
            		listData.put(account.GUID, account);
             }
            cursor.close();	
            
            return listData;
        }
        else
        	return null;
	}
	
	public String[] GetTransactionDescriptions() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		String lastyear = Integer.toString(year-1);
		
		Cursor cursor = sqliteHandle.rawQuery("select distinct description from transactions where post_date > "+lastyear+"0101000000",null);
        int count = cursor.getCount();
        if ( count == 0 )
        	return null;
        
        String[] values = new String[count];
		int index = 0;
        while (cursor.moveToNext())
        {
        	values[index++] = cursor.getString(cursor.getColumnIndex("description"));
        }
        cursor.close();	
        
        return values;
	}
	
	private String GenGUID() {
		UUID uuid = UUID.randomUUID();
		String GUID = Long.toHexString(uuid.getMostSignificantBits())+Long.toHexString(uuid.getLeastSignificantBits());
		return GUID;
	}
	
	public boolean insertTransaction(String toGUID, String fromGUID, String description, String amount, String date) {

		try {
	        sqliteHandle.beginTransaction();
	        
			String tx_guid = GenGUID();
	
			Log.i(TAG, "insertTransaction " + description+ "\n" +
					tx_guid + "\n" +
					toGUID + "\n" +
					fromGUID + "\n" +
					date + "\n" +
					amount + "\n");
			
	        Date now = new Date();
	        DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
	        String postDate = formatter.format(now);
	        
	        DateFormat simpleFormat = new SimpleDateFormat("MM/dd/yyyy");
	        Date enter = simpleFormat.parse(date);
	        
	        String enterDate = formatter.format(enter);
	
			
			// We need to insert 3 records (a transaction and two splits)
			// CREATE TABLE splits (guid text(32) PRIMARY KEY NOT NULL, tx_guid text(32) NOT NULL, account_guid text(32) NOT NULL, memo text(2048) NOT NULL, action text(2048) NOT NULL, reconcile_state text(1) NOT NULL, reconcile_date text(14), value_num bigint NOT NULL, value_denom bigint NOT NULL, quantity_num bigint NOT NULL, quantity_denom bigint NOT NULL, lot_guid text(32));
			// CREATE TABLE transactions (guid text(32) PRIMARY KEY NOT NULL, currency_guid text(32) NOT NULL, num text(2048) NOT NULL, post_date text(14), enter_date text(14), description text(2048));
	
			// First the transaction
	        String transQuery = "insert into transactions(guid,currency_guid,num,post_date,enter_date,description)" +
	        " values('"+tx_guid+"','d42c51800f472526f265de2711a36020','"+postDate+"','','"+enterDate+"','"+description+"')";
			Log.v(TAG, transQuery);
			sqliteHandle.execSQL(transQuery);
			
			double d = Double.parseDouble(amount);
			int demom = 100;
			int value = (int)(d*demom);
			
			String toQuery = "insert into splits(guid,tx_guid,account_guid,memo,action,reconcile_state,value_num,value_denom,quantity_num,quantity_denom)"+
			" values('"+GenGUID()+"','"+tx_guid+"','"+toGUID+"','','','n',"+value+",100,"+value+",100)";
			Log.v(TAG, toQuery);
			sqliteHandle.execSQL(toQuery);

			String fromQuery = "insert into splits(guid,tx_guid,account_guid,memo,action,reconcile_state,value_num,value_denom,quantity_num,quantity_denom)"+
			" values('"+GenGUID()+"','"+tx_guid+"','"+fromGUID+"','','','n',"+ -value +",100,"+ -value +",100)";
			Log.v(TAG, fromQuery);
			sqliteHandle.execSQL(fromQuery);

			sqliteHandle.setTransactionSuccessful();
			
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			sqliteHandle.endTransaction();
		}
		

	}
	
	public String[] GetAccountsFromTransactionDescription(String description) {
		int index = 0;
		String[] accountGUIDs = null;
		String transSQL = "select guid from transactions where description='"+description+"' order by post_date desc limit 1;";
		Cursor cursor = sqliteHandle.rawQuery(transSQL,null);
        if ( cursor.getCount() > 0 && cursor.moveToNext() )
        {
        	String transGUID = cursor.getString(cursor.getColumnIndex("guid"));
        	String accountsSQL = "select accounts.guid from accounts,splits where tx_guid='"+transGUID+"' and account_guid=accounts.guid";
        	Cursor accountsCursor = sqliteHandle.rawQuery(accountsSQL,null);
        	int count = accountsCursor.getCount();
        	if ( count > 0 ) {
        		accountGUIDs = new String[count];
        		while ( accountsCursor.moveToNext() ) {
        			accountGUIDs[index++] = accountsCursor.getString(cursor.getColumnIndex("guid"));
        		}
        	}
        	accountsCursor.close();
        }
        cursor.close();	
        return accountGUIDs;
	}
	
	/**
	 * Returns the data collection object.
	 */
	public DataCollection getGncData() {
		return gncData;
	}
	/**
	 * This method returns the InputStream. If the compressed flag is set then
	 * the return is of GZipInputStream type, otherwise FileInputStream.
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
	 * This method creates SAXDataParser and parses the data (Fast processing
	 * but less functionality)
	 * 
	 * @param inStream
	 */
	private void readFile(InputStream inStream) {
		XMLReader parser;
		// get parser
		if (app.localLOGV)
			Log.i(TAG, "Getting Parser");
		try {
			parser = SAXParserFactory.newInstance().newSAXParser()
					.getXMLReader();
		} catch (ParserConfigurationException e) {
			if (app.localLOGV)
				Log.i(TAG, "Cant Get Parser - " + e.getMessage());
			throw new RuntimeException(e);
		} catch (SAXException e) {
			if (app.localLOGV)
				Log.i(TAG, "Cant Get Parser - " + e.getMessage());
			throw new RuntimeException(e);
		}
		if (app.localLOGV)
			Log.i(TAG, "Getting Parser..Done");
		// set handlers
		XMLHandler xmlh = new XMLHandler();
		parser.setContentHandler(xmlh);
		parser.setErrorHandler(xmlh);
		// parse the data
		if (app.localLOGV)
			Log.i(TAG, "Parsing Data");
		try {
			parser.parse(new InputSource(inStream));
		} catch (IOException e) {
			if (app.localLOGV)
				Log.i(TAG, "File cannot be read");
			throw new RuntimeException(e);
		} catch (SAXException e) {
			if (app.localLOGV)
				Log.i(TAG, "Cannot Parse - " + e.getMessage());
			throw new RuntimeException(e);
		}
		if (app.localLOGV)
			Log.i(TAG, "Parsing Data..Done");
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
		public Book						book		= new Book();
		// all data types
		public Map<String, Account>		accounts	= new HashMap<String, Account>();
		public Map<String, Commodity>	commodities	= new HashMap<String, Commodity>();
		/**
		 * This method should be called once all the data has been filled. It
		 * does the following: 1. Add children data to accounts 2. Calculate
		 * account balances 3. #
		 */
		public void completeCollection() {
			if (app.localLOGV)
				Log.i(TAG, "Calculating Data...");
			// get full names
			updateFullNames();
			// create account tree
			createAccountTree();
			if (app.localLOGV)
				Log.i(TAG, "Calculating Data...Done");
		}
		/**
		 * We need full account names like granparent:parent:accountname to sort
		 * the accounts, this method will update accounts with fullName
		 * attribute
		 */
		private void updateFullNames() {
			// get iterator
			Iterator accountIterator = accounts.values().iterator();
			Account account;
			while (accountIterator.hasNext()) {
				account = (Account) accountIterator.next();
				account.fullName = getFullName(account);
			}
		}
		/**
		 * This method takes an account, finds its parents and grand parents
		 * until root, and returns fullName of account
		 * 
		 * @param account
		 * @return fullName
		 */
		private String getFullName(Account account) {
			String fullName;
			// If we know the full name, then return it; otherwise, construct it
			if (account.fullName != null)
				return account.fullName;
			// Follow chain of parents, pre-pending their names,
			// so we get "Grandparent:Parent:Name"
			String p = account.parentGUID;
			fullName = account.name;
			while (null != p) {
				Account parent = (Account) gncData.accounts.get(p);
				if (parent == null || parent.name.equalsIgnoreCase("ROOT")) {
					break;
				}
				fullName = parent.name + ":" + fullName;
				p = parent.parentGUID;
			}
			return fullName;
		}
		/**
		 * We need to populate each account with list of its immediate children
		 * in subList. This method goes through each account and updates its
		 * parent account's subList with its GUID.
		 */
		private void createAccountTree() {
			// get key set
			Set<String> keySet = accounts.keySet();
			// get iterator
			Iterator it = keySet.iterator();
			while (it.hasNext()) {
				Account child = accounts.get((String) it.next());
				if (child.parentGUID != null) {
					Account parent = accounts.get(child.parentGUID);
					parent.subList.add(child.GUID);
					parent.hasChildren = true;
				}
			}
		}
	}
	public class Book {
		// configuration data
		public String	GUID;
		public String	version;
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
		public String	rootAccountGUID;
	}
	public class Commodity {
		public String	space;
		public String	id;
		public String	quoteSource;
	}
	public class Account {
		// fields for an account
		public String		type;
		public String		GUID;
		public String		parentGUID;
		public String		name;
		public String		fullName;
		public String		notes;
		public String		description;
		public String		code;
		public boolean		placeholder;
		public Commodity	currency;
		// calculated balance amount
		public Double		balance;
		// transactions that belong to account
		public List<String>	trans		= new ArrayList<String>();
		public boolean		hasChildren	= false;
		// id's of child-accounts
		public List<String>	subList		= new ArrayList<String>();
	}
	public class AccountComparator implements Comparator<Account> {
		public int compare(Account o1, Account o2) {
			return o1.fullName.compareTo(o2.fullName);
		}
	}
	/**
	 * This class is the XML data handler which implements the start and end
	 * element methods to capture xml data and create data objects in data
	 * collection object.
	 * 
	 * @author avvari.shyam
	 * 
	 */
	private class XMLHandler extends DefaultHandler {
		// Local data while collecting data
		private Book			cBook;
		private Account			cAccount;
		private Commodity		cCommodity;
		// data utilities
		DateFormat				dfm			= new SimpleDateFormat(
													"yyyy-MM-dd HH:mm:ss");
		// String to collect data
		protected StringBuffer	buffer		= new StringBuffer();
		// current object values
		private String			cCountType;
		private boolean			isTemplate	= false;
		/*
		 * At the beginning of the document create a new data collection object.
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
		 */
		@Override
		public void startDocument() throws SAXException {
			// create new collection
			gncData = new DataCollection();
			// call super
			super.startDocument();
		}
		/*
		 * Start of element - find out the element and capture data
		 * 
		 * @see
		 * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
		 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// Temp field
			String value;
			// Always clear buffer when a new element is started
			buffer.setLength(0);
			// Dispatch the type of main element
			// gnc:book
			if (qName.equalsIgnoreCase("gnc:book")) {
				cBook = new Book();
				cBook.version = attributes.getValue("version");
			} else if (qName.equalsIgnoreCase("gnc:count-data")) {
				cCountType = attributes.getValue("cd:type");
			} else if (qName.equalsIgnoreCase("gnc:commodity")
					|| qName.equalsIgnoreCase("act:commodity")) {
				cCommodity = new Commodity();
			} else if (qName.equalsIgnoreCase("gnc:account")) {
				cAccount = new Account();
			} else if (qName.equalsIgnoreCase("gnc:template-transactions")) {
				isTemplate = true;
			}
		}
		/*
		 * End of element - add gathered data to data pool
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
		 * java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// Trim the sides of the value
			String value = buffer.toString().trim();
			if (qName.equalsIgnoreCase("gnc:book") && cBook != null) {
				gncData.book = cBook;
			} else if (qName.equalsIgnoreCase("book:id")) {
				cBook.GUID = value;
			} else if (qName.equalsIgnoreCase("gnc:count-data")) {
				if (cCountType.equalsIgnoreCase("account"))
					cBook.cntAccount = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("transaction"))
					cBook.cntTransaction = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("schedxaction"))
					cBook.cntSchedxaction = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("gnc:GncJob"))
					cBook.cntJob = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("gnc:GncInvoice"))
					cBook.cntInvoice = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("gnc:GncCustomer"))
					cBook.cntCustomer = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("gnc:GncBillTerm"))
					cBook.cntBillTerm = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("gnc:GncTaxTable"))
					cBook.cntTaxTable = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("gnc:GncEmployee"))
					cBook.cntEmployee = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("gnc:GncEntry"))
					cBook.cntEntry = Integer.valueOf(value);
				else if (cCountType.equalsIgnoreCase("gnc:GncVendor"))
					cBook.cntVendor = Integer.valueOf(value);
			} else if (qName.equalsIgnoreCase("cmdty:space")) {
				cCommodity.space = value;
			} else if (qName.equalsIgnoreCase("cmdty:id")) {
				cCommodity.id = value;
			} else if (qName.equalsIgnoreCase("cmdty:quote_source")) {
				cCommodity.quoteSource = value;
			} else if (qName.equalsIgnoreCase("gnc:commodity")) {
				gncData.commodities.put(cCommodity.quoteSource, cCommodity);
			} else if (qName.equalsIgnoreCase("act:name")) {
				cAccount.name = value;
			} else if (qName.equalsIgnoreCase("act:id")) {
				cAccount.GUID = value;
			} else if (qName.equalsIgnoreCase("act:type")) {
				cAccount.type = value;
			} else if (qName.equalsIgnoreCase("act:parent")) {
				cAccount.parentGUID = value;
			} else if (qName.equalsIgnoreCase("act:description")) {
				cAccount.description = value;
			} else if (qName.equalsIgnoreCase("act:code")) {
				cAccount.code = value;
			} else if (qName.equalsIgnoreCase("gnc:account")
					&& cAccount != null && !isTemplate) {
				gncData.accounts.put(cAccount.GUID, cAccount);
				if (cAccount.type.equalsIgnoreCase("root")) {
					cBook.rootAccountGUID = cAccount.GUID;
					Log.i(TAG, "Root guid is " + cAccount.GUID);
				}
			} else if (qName.equalsIgnoreCase("gnc:template-transactions")) {
				isTemplate = false;
			}
		}
		/*
		 * At the end of document complete data collection
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
		 */
		@Override
		public void endDocument() throws SAXException {
			// Complete data collection
			gncData.completeCollection();
			// call super
			super.endDocument();
		}
		/*
		 * Accumulate characters read
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			buffer.append(ch, start, length);
		}
	}
}
