/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * #TODO License
 */
package rednus.gncandroid;

import java.text.NumberFormat;
import java.util.Map;
import java.util.TreeMap;
import rednus.gncandroid.GNCDataHandler.Account;
import rednus.gncandroid.GNCDataHandler.DataCollection;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author shyam.avvari
 * 
 */
public class AccountsActivity extends Activity implements OnItemClickListener {
	// TAG for this activity
	private static final String TAG = "AccountsActivity";
	// Application data
	private GNCAndroid app;
	private String currRootGUID;
	private Map<String, Account> listData = new TreeMap<String, Account>();
	private DataCollection dc;

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
		// First get first object of data
		dc = app.gncDataHandler.getGncData();
		getListData(dc.book.rootAccountGUID);
		// set view
		setContentView(R.layout.accounts);
		// get list
		ListView lv = (ListView) findViewById(R.id.accounts);
		lv.setAdapter(new AccountsListAdapter(this));
		lv.setOnItemClickListener(this);
		// done
		if (app.localLOGV)
			Log.i(TAG, "Activity Finished");
	}

	/**
	 * This method will get all the sub accounts of root and adds it to the list
	 * for display.
	 * 
	 * @param root
	 */
	private void getListData(String rootGUID) {
		// get root account
		listData = app.gncDataHandler.GetSubAccounts(rootGUID);

		currRootGUID = rootGUID;
	}

	/*
	 * Event Handler for List item selection
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	public void onItemClick(AdapterView<?> parent, View child, int position,
			long id) {
		if (app.localLOGV)
			Log.i(TAG, "Account Selected");
		// get adapter
		BaseAdapter b = (BaseAdapter) parent.getAdapter();
		// get current item
		Account account = (Account) b.getItem(position);
		// depending on root or item get list
		if (account.GUID.equalsIgnoreCase(currRootGUID))
			getListData(account.parentGUID);
		else
			getListData(account.GUID);
		// set refresh
		b.notifyDataSetChanged();
		// reset list position to top
		parent.scrollTo(0, 0);
		if (app.localLOGV)
			Log.i(TAG, "Accounts Refreshed");
	}

	/**
	 * This class implements Adapter methods for displaying accounts in a list
	 * view
	 * 
	 * @author avvari.shyam
	 * 
	 */
	private class AccountsListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		/**
		 * Constructor - creates inflator's instance from context
		 * 
		 * @param context
		 */
		public AccountsListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		/*
		 * Returns the total number of items to be displayed
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		public int getCount() {
			return listData.size();
		}

		/*
		 * Returns the Item in specific position
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		public Object getItem(int i) {
			return listData.get(listData.keySet().toArray()[i]);
		}

		/*
		 * Returns the id of the item in specific position. In this case its the
		 * position itself.
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		public long getItemId(int i) {
			return i;
		}

		/*
		 * This method creates the list item for specific position passed. It
		 * populates data from the list item at position.
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			AccountItem item;
			Account account;
			// always create new view
			convertView = mInflater.inflate(R.layout.account_item, null);
			item = new AccountItem();
			item.btnExpand = (ImageView) convertView
					.findViewById(R.id.acc_more);
			item.txvAccName = (TextView) convertView
					.findViewById(R.id.acc_name);
			item.txvBalance = (TextView) convertView
					.findViewById(R.id.acc_balance);
			convertView.setTag(item);
			// set values for account line item
			account = (Account) getItem(position);
			item.txvAccName.setText(account.name);
			item.txvBalance.setText(String.valueOf(NumberFormat
					.getCurrencyInstance().format(account.balance)));
			// set amount colour
			if (account.balance < 0)
				item.txvBalance.setTextColor(app.res
						.getColor(R.color.color_negetive));
			else
				item.txvBalance.setTextColor(app.res
						.getColor(R.color.color_positive));
			// set image properties
			if (account.GUID.equalsIgnoreCase(currRootGUID))
				item.btnExpand.setImageResource(R.drawable.list_expanded);
			else if (account.hasChildren)
				item.btnExpand.setImageResource(R.drawable.list_collapsed);
			// return
			return convertView;
		}

		/*
		 * When the data in list listData changed, it is necessary to redraw the
		 * list view and fill it up with new data.
		 * 
		 * @see android.widget.BaseAdapter#notifyDataSetChanged()
		 */
		@Override
		public void notifyDataSetChanged() {
			// first invalidate data set
			super.notifyDataSetInvalidated();
			super.notifyDataSetChanged();
		}

		/*
		 * This methods tells the view layout that all the items in the list are
		 * not enabled by default.
		 * 
		 * @see android.widget.BaseAdapter#areAllItemsEnabled()
		 */
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		/*
		 * This method checks if the list item has children and then return true
		 * to enable the list item as clickable.
		 * 
		 * @see android.widget.BaseAdapter#isEnabled(int)
		 */
		@Override
		public boolean isEnabled(int position) {
			Account account = (Account) getItem(position);
			return account.hasChildren;
		}

		/**
		 * This is holder class for Account Summary Row
		 * 
		 * @author shyam.avvari
		 * 
		 */
		private class AccountItem {
			ImageView btnExpand;
			TextView txvAccName;
			TextView txvBalance;
		}
	}
}
