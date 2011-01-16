/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * Project : GNCAndroid Package : rednus.GNCAndroid File : MainView.java
 * Description :
 */
package rednus.gncandroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import android.content.SharedPreferences;
import android.content.res.Resources;
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
	private static final String TAG = "GNCDataHandler"; // TAG for this activity
	private GNCAndroid app; // Application
	private DataCollection gncData; // Data Collection
	private SQLiteDatabase sqliteHandle;
	private boolean longAccountNames;
	private String accountFilter;
	private TreeMap<String, String> accountPrefMapping;
	private Resources res;
	public boolean dataValid = false;

	private final String transInsert = "insert into transactions(guid,currency_guid,num,post_date,enter_date,description) values(?,?,?,?,?,?)";
	private final String splitsInsert = "insert into splits(guid,tx_guid,account_guid,memo,action,reconcile_state,value_num,value_denom,quantity_num,quantity_denom)"
			+ " values(?,?,?,?,?,?,?,?,?,?)";

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
	public GNCDataHandler(GNCAndroid app, String dataFile,
			boolean longAccountNames) {
		this.app = app;

		res = app.getResources();

		this.longAccountNames = longAccountNames;
		try {
			BuildAccountMapping();

			sqliteHandle = SQLiteDatabase.openDatabase(dataFile, null,
					SQLiteDatabase.OPEN_READWRITE
							| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
			gncData = new DataCollection();
			Cursor cursor = sqliteHandle.rawQuery("select * from books", null);
			if (cursor.getCount() > 0) {
				if (cursor.moveToNext()) {
					// CREATE TABLE books (guid text(32) PRIMARY KEY NOT NULL,
					// root_account_guid text(32) NOT NULL, root_template_guid
					// text(32) NOT NULL);
					gncData.book.GUID = cursor.getString(cursor
							.getColumnIndex("guid"));
					gncData.book.rootAccountGUID = cursor.getString(cursor
							.getColumnIndex("root_account_guid"));
				}
			}
			cursor.close();

			// cursor =
			// sqliteHandle.rawQuery("select accounts.*,sum(CAST(value_num AS REAL)/value_denom) as bal from accounts,transactions,splits where splits.tx_guid=transactions.guid and splits.account_guid=accounts.guid and hidden=0 group by accounts.name",null);
			// cursor =
			// sqliteHandle.rawQuery("select *,sum(CAST(value_num AS REAL)/value_denom) as bal from accounts left outer join splits on splits.account_guid=accounts.guid group by accounts.name",null);
			cursor = sqliteHandle.rawQuery("select * from accounts", null);
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					Account account = new Account();
					// CREATE TABLE accounts (guid text(32) PRIMARY KEY NOT
					// NULL, name text(2048) NOT NULL, account_type text(2048)
					// NOT NULL, commodity_guid text(32), commodity_scu integer
					// NOT NULL, non_std_scu integer NOT NULL, parent_guid
					// text(32), code text(2048), description text(2048), hidden
					// integer, placeholder integer);
					account.GUID = cursor.getString(cursor
							.getColumnIndex("guid"));
					account.name = cursor.getString(cursor
							.getColumnIndex("name"));
					account.type = cursor.getString(cursor
							.getColumnIndex("account_type"));
					account.parentGUID = cursor.getString(cursor
							.getColumnIndex("parent_guid"));
					account.code = cursor.getString(cursor
							.getColumnIndex("code"));
					account.description = cursor.getString(cursor
							.getColumnIndex("description"));
					account.placeholder = cursor.getInt(cursor
							.getColumnIndex("placeholder")) != 0;

					gncData.accounts.put(account.GUID, account);
				}
			}
			cursor.close();

			gncData.completeCollection();
			dataValid = true;
		} catch (Exception e) {
			Log.e(TAG, e.getStackTrace().toString());
		}
	}

	public void BuildAccountMapping() {
		// TODO This isn't the full list
		accountPrefMapping = new TreeMap<String, String>();
		accountPrefMapping.put(res.getString(R.string.pref_account_type_asset),
				"ASSET");
		accountPrefMapping.put(res.getString(R.string.pref_account_type_bank),
				"BANK");
		accountPrefMapping.put(res.getString(R.string.pref_account_type_cc),
				"CREDIT");
		accountPrefMapping.put(res
				.getString(R.string.pref_account_type_expense), "EXPENSE");
		accountPrefMapping.put(
				res.getString(R.string.pref_account_type_equity), "EQUITY");
		accountPrefMapping.put(
				res.getString(R.string.pref_account_type_income), "INCOME");
		accountPrefMapping.put(res
				.getString(R.string.pref_account_type_liability), "LIABILITY");
		accountPrefMapping.put(res
				.getString(R.string.pref_account_type_mutual_fund), "MUTUAL");
		accountPrefMapping.put(res.getString(R.string.pref_account_type_stock),
				"STOCK");
	}

	public void GenAccountFilter(SharedPreferences sp) {
		StringBuffer filter = new StringBuffer();
		if (!sp.getBoolean(res.getString(R.string.pref_show_hidden_account),
				false))
			filter.append(" and hidden=0 ");

		for (String key : accountPrefMapping.keySet())
			if (!sp.getBoolean(key, true))
				filter.append(" and account_type!='"
						+ accountPrefMapping.get(key) + "' ");

		accountFilter = filter.toString();
	}

	public Account GetAccount(String GUID, boolean getBalance) {
		Account account = gncData.accounts.get(GUID);
		if (account == null)
			return null;

		if (account.balance == null && getBalance)
			account.balance = this.AccountBalance(account.GUID);

		return account;
	}

	public Account AccountFromCursor(Cursor cursor, boolean getBalance) {
		return GetAccount(cursor.getString(cursor.getColumnIndex("guid")),
				getBalance);
	}

	public Double AccountBalance(String GUID) {
		Cursor cursor = sqliteHandle
				.rawQuery(
						"select accounts.*,sum(CAST(value_num AS REAL)/value_denom) as bal from accounts,transactions,splits where splits.tx_guid=transactions.guid and splits.account_guid=accounts.guid and accounts.guid='"
								+ GUID + "' group by accounts.name", null);
		Double retVal = 0.0;
		if (cursor.getCount() > 0) {
			if (cursor.moveToNext()) {
				int balIndex = cursor.getColumnIndex("bal");
				if (!cursor.isNull(balIndex))
					retVal = cursor.getDouble(balIndex);
			}
		}
		cursor.close();
		return retVal;
	}

	public TreeMap<String, String> GetAccountList(String[] accountTypes) {
		String query;
		String types;
		
		/*
		if (expense)
			types = "'EXPENSE'";
		else
			types = "'CREDIT', 'BANK'";
		*/
		StringBuffer tb = new StringBuffer();
		Boolean first = true;
		for (String at: accountTypes) {
			if ( !first )
				tb.append(", ");
			tb.append("'"+at+"'");
			first = false;
		}
		types = tb.toString();
		
		
		query = "select guid, name from accounts where account_type in ("
				+ types + ") and hidden=0 and non_std_scu=0 order by name";

		Cursor cursor = sqliteHandle.rawQuery(query, null);
		if (cursor.getCount() > 0) {
			TreeMap<String, String> listData = new TreeMap<String, String>();
			while (cursor.moveToNext()) {
				Account account = this.AccountFromCursor(cursor, false);

				if (longAccountNames)
					listData.put(account.fullName, account.GUID);
				else {
					String guid = listData.get(account.name);
					if (guid == null)
						listData.put(account.name, account.GUID);
					else { // We have a name collision
						listData.put(account.fullName, account.GUID);
					}
				}

			}
			cursor.close();

			return listData;
		} else
			return null;

	}

	public TreeMap<String, Account> GetSubAccounts(String rootGUID) {
		Cursor cursor = sqliteHandle.rawQuery(
				"select * from accounts where parent_guid='" + rootGUID + "' "
						+ accountFilter + " order by name", null);
		if (cursor.getCount() > 0) {
			TreeMap<String, Account> listData = new TreeMap<String, Account>();
			Account rootAccount = this.GetAccount(rootGUID, true);
			if (!rootAccount.name.contains("Root"))
				listData.put(rootGUID, rootAccount);
			while (cursor.moveToNext()) {
				Account account = this.AccountFromCursor(cursor, true);

				if (account.hasChildren
						|| ((int) (account.balance * 100.0)) != 0)
					listData.put(account.GUID, account);
			}
			cursor.close();

			return listData;
		} else
			return null;
	}

	public String[] GetTransactionDescriptions() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		String lastyear = Integer.toString(year - 1);

		Cursor cursor = sqliteHandle.rawQuery(
				"select distinct description from transactions where post_date > "
						+ lastyear + "0101000000", null);
		int count = cursor.getCount();
		if (count == 0)
			return null;

		String[] values = new String[count];
		int index = 0;
		while (cursor.moveToNext()) {
			values[index++] = cursor.getString(cursor
					.getColumnIndex("description"));
		}
		cursor.close();

		return values;
	}

	private String GenGUID() {
		UUID uuid = UUID.randomUUID();
		String GUID = Long.toHexString(uuid.getMostSignificantBits())
				+ Long.toHexString(uuid.getLeastSignificantBits());
		return GUID;
	}

	public boolean insertTransaction(String toGUID, String fromGUID,
			String description, String amount, String date) {

		try {
			sqliteHandle.beginTransaction();

			String tx_guid = GenGUID();

			Date now = new Date();
			DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String postDate = formatter.format(now);

			DateFormat simpleFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date enter = simpleFormat.parse(date);

			String enterDate = formatter.format(enter);

			// We need to insert 3 records (a transaction and two splits)
			// CREATE TABLE splits (guid text(32) PRIMARY KEY NOT NULL, tx_guid
			// text(32) NOT NULL, account_guid text(32) NOT NULL, memo
			// text(2048) NOT NULL, action text(2048) NOT NULL, reconcile_state
			// text(1) NOT NULL, reconcile_date text(14), value_num bigint NOT
			// NULL, value_denom bigint NOT NULL, quantity_num bigint NOT NULL,
			// quantity_denom bigint NOT NULL, lot_guid text(32));
			// CREATE TABLE transactions (guid text(32) PRIMARY KEY NOT NULL,
			// currency_guid text(32) NOT NULL, num text(2048) NOT NULL,
			// post_date text(14), enter_date text(14), description text(2048));

			// First the transaction
			Object[] transArgs = { tx_guid, "d42c51800f472526f265de2711a36020",
					postDate, "", enterDate, description };
			sqliteHandle.execSQL(transInsert, transArgs);

			double d = Double.parseDouble(amount);
			int demom = 100;
			int value = (int) (d * demom);

			Object[] toArgs = { GenGUID(), tx_guid, toGUID, "", "", "n", value,
					100, value, 100 };
			sqliteHandle.execSQL(splitsInsert, toArgs);

			Object[] fromArgs = { GenGUID(), tx_guid, fromGUID, "", "", "n",
					-value, 100, -value, 100 };
			sqliteHandle.execSQL(splitsInsert, fromArgs);

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
		String transSQL = "select guid from transactions where description='"
				+ description + "' order by post_date desc limit 1;";
		Cursor cursor = sqliteHandle.rawQuery(transSQL, null);
		if (cursor.getCount() > 0 && cursor.moveToNext()) {
			String transGUID = cursor.getString(cursor.getColumnIndex("guid"));
			String accountsSQL = "select accounts.guid from accounts,splits where tx_guid='"
					+ transGUID + "' and account_guid=accounts.guid";
			Cursor accountsCursor = sqliteHandle.rawQuery(accountsSQL, null);
			int count = accountsCursor.getCount();
			if (count > 0) {
				accountGUIDs = new String[count];
				while (accountsCursor.moveToNext()) {
					accountGUIDs[index++] = accountsCursor.getString(cursor
							.getColumnIndex("guid"));
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

	//
	/**
	 * This class is a collection of all gnc data objects.
	 * 
	 * @author shyam.avvari
	 * 
	 */
	public class DataCollection {
		// book information
		public Book book = new Book();
		// all data types
		public Map<String, Account> accounts = new HashMap<String, Account>();
		public Map<String, Commodity> commodities = new HashMap<String, Commodity>();

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
			for (Account account : accounts.values())
				account.fullName = getFullName(account);

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
				if (parent == null || parent.name.contains("Root")) {
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
			for (String accountName : accounts.keySet()) {
				Account child = accounts.get(accountName);
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
		public String GUID;
		public String version;
		public String compName;
		public String compId;
		public String compAddr;
		public String compEmail;
		public String compUrl;
		public String compFax;
		public String compContact;
		public String compPhone;
		public String defCustTaxTable;
		public String defVendTaxTable;
		public int cntAccount;
		public int cntTransaction;
		public int cntSchedxaction;
		public int cntJob;
		public int cntInvoice;
		public int cntCustomer;
		public int cntBillTerm;
		public int cntTaxTable;
		public int cntEmployee;
		public int cntEntry;
		public int cntVendor;
		public String cmdtySpace;
		public String cmdtyId;
		public String rootAccountGUID;
	}

	public class Commodity {
		public String space;
		public String id;
		public String quoteSource;
	}

	public class Account {
		// fields for an account
		public String type;
		public String GUID;
		public String parentGUID;
		public String name;
		public String fullName;
		public String notes;
		public String description;
		public String code;
		public boolean placeholder;
		public Commodity currency;
		// calculated balance amount
		public Double balance;
		// transactions that belong to account
		public List<String> trans = new ArrayList<String>();
		public boolean hasChildren = false;
		// id's of child-accounts
		public List<String> subList = new ArrayList<String>();
	}

	public class AccountComparator implements Comparator<Account> {
		public int compare(Account o1, Account o2) {
			return o1.fullName.compareTo(o2.fullName);
		}
	}
}
