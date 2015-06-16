/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.bases;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lairdtech.bt.BluetoothAdapterWrapper;
import com.lairdtech.bt.BluetoothAdapterWrapperCallback;
import com.lairdtech.bt.ble.BleDeviceBase;
import com.lairdtech.lairdtoolkit.ListFoundDevicesHandler;
import com.lairdtech.lairdtoolkit.R;
import com.lairdtech.misc.DebugWrapper;

public class BaseActivity extends Activity implements BluetoothAdapterWrapperCallback{

	private static final int ENABLE_BT_REQUEST_ID = 1;
	private final static String TAG = "Base Activity";
	protected Activity mActivity;

	protected TextView mValueName;
	protected TextView mValueRSSI;
	protected TextView mValueBattery;
	protected Button mBtnScan;

	protected BluetoothAdapterWrapper mBluetoothAdapterWrapper;
	protected Dialog mDialogFoundDevices;
	private Dialog mDialogAbout;
	private View mViewAbout;

	protected ListFoundDevicesHandler mListFoundDevicesHandler = null;

	private BleDeviceBase mBleDeviceBase;

	protected SharedPreferences mSharedPreferences;
	protected boolean isInNewScreen = false;
	protected boolean isPrefRunInBackground = true;
	protected boolean isPrefPeriodicalScan = true;

	/**
	 * Set the bleDevice base to the specific device manager for each application within the toolkit.
	 * This ensures it is not null
	 * @param bleDeviceBase
	 */
	protected void setBleDeviceBase(BleDeviceBase bleDeviceBase){
		mBleDeviceBase = bleDeviceBase;
	}

	protected BleDeviceBase getBleDeviceBase(){
		return mBleDeviceBase;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = this;
		bindViews();
		setAdapters();
		setListeners();

		mBluetoothAdapterWrapper = new BluetoothAdapterWrapper(this, this);

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	}

	/**
	 * Binds the generic textViews, Buttons, Layouts and ListViews in the Managers.
	 */
	protected void bindViews() {
		mValueName = (TextView) findViewById(R.id.valueDeviceName);	
		mValueRSSI = (TextView) findViewById(R.id.valueDeviceRssi);	
		mValueBattery = (TextView) findViewById(R.id.valueBattery);
		mBtnScan = (Button) findViewById(R.id.btnScan); 

		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mViewAbout = li.inflate(R.layout.activity_about, null, false);
	}

	/**
	 * used for setting handler and adapter for the dialog listView.
	 */
	protected void setAdapters() {
		//setting handler and adapter for the dialog list view
		mListFoundDevicesHandler = new ListFoundDevicesHandler(this);
	}

	/**
	 * used to set onClickListener for the generic scan button. 
	 */
	protected void setListeners() {
		// set onClickListener for the scan button 
		mBtnScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(v.getId())
				{
				case R.id.btnScan:
				{
					if(mBluetoothAdapterWrapper.isEnabled() == false){
						DebugWrapper.errorMsg("Bluetooth must be on to start scanning.", TAG, DebugWrapper.getDebugMessageVisibility() );
						DebugWrapper.toastMsg(mActivity, "Bluetooth must be on to start scanning.");
						return;
					}else if(mBleDeviceBase.isConnected() == false
							&& mBleDeviceBase.isConnecting() == false){
						
						// do a scan operation
						if(isPrefPeriodicalScan == true){
							mBluetoothAdapterWrapper.startBleScanPeriodically();
						} else{
							mBluetoothAdapterWrapper.startBleScan();
						}

						mDialogFoundDevices.show();

					} else if(mBleDeviceBase.isConnected() == true){
						mBleDeviceBase.disconnect();

					} else if(mBleDeviceBase.isConnecting() == true){
						DebugWrapper.toastMsg(mActivity, "Wait for connection!");
					}
					uiInvalidateBtnState();

					break;
				}
				}			
			}
		});
	}

	/**
	 * Initialize the dialog for the devices found from a BLE scan.
	 * @param title
	 */
	protected void initialiseDialogFoundDevices(String title) {
		/*
		 * create/set dialog ListView
		 */
		ListView mLvFoundDevices = new ListView(mActivity);
		mLvFoundDevices.setAdapter(mListFoundDevicesHandler);
		mLvFoundDevices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				final BluetoothDevice device = mListFoundDevicesHandler.getDevice(position);
				if(device == null) return;

				mBluetoothAdapterWrapper.stopBleScan();
				mDialogFoundDevices.dismiss();
				mBleDeviceBase.connect(device, false);
				uiInvalidateBtnState();

			}
		});

		/*
		 * create and initialise Dialog
		 */
		mDialogFoundDevices = new Dialog(this);
		mDialogFoundDevices.setContentView(mLvFoundDevices);
		mDialogFoundDevices.setTitle("Select a "+ title +" device");
		mDialogFoundDevices.setCanceledOnTouchOutside(false);
		mDialogFoundDevices.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				mBluetoothAdapterWrapper.stopBleScan();
				invalidateOptionsMenu();

			}
		});

		mDialogFoundDevices.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				mListFoundDevicesHandler.clearList();				
			}
		});
	}

	protected void initialiseDialogAbout(String text){
		mDialogAbout = new Dialog(this);
		mDialogAbout.setContentView(mViewAbout);
		mDialogAbout.setTitle("About");
		mDialogAbout.setCanceledOnTouchOutside(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) mDialogAbout.findViewById(R.id.logo).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		/*
		 * set content to display in the about message
		 */
		TextView valueAbout = (TextView) mDialogAbout.findViewById(R.id.valueAbout);
		valueAbout.setMovementMethod(LinkMovementMethod.getInstance());
		valueAbout.setText(Html.fromHtml(text));
	}

	/**
	 * Add the found devices to a listView in the dialog.
	 * @param device 
	 * @param rssi 
	 * @param scanRecord 
	 */
	protected void handleFoundDevice(final BluetoothDevice device,
			final int rssi,
			final byte[] scanRecord){
		//adds found devices to list view
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				mListFoundDevicesHandler.addDevice(device, rssi, scanRecord);
				mListFoundDevicesHandler.notifyDataSetChanged();
			}
		});
	}

	protected void onResume() {
		super.onResume();
		loadPref();
		isInNewScreen = false;
		//check that BT is enabled as use could have turned it off during the onPause.
		if (mBluetoothAdapterWrapper.isEnabled() == false){
			Intent enableBTIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBTIntent, ENABLE_BT_REQUEST_ID);
		}
	}

	public void onBackPressed(){
		if(mBleDeviceBase.isConnected() == true){
			mBleDeviceBase.disconnect();
			invalidateOptionsMenu();
		}else{
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		
		case android.R.id.home:
			if(mBleDeviceBase.isConnected() == true){
				mBleDeviceBase.disconnect();
				invalidateOptionsMenu();
			}else{
				finish();
			}
			break;
		
		case R.id.action_about:

			mDialogAbout.show();
			break;

		case R.id.action_settings:
			isInNewScreen = true;

			intent = new Intent(this, BaseActivitySettings.class);
			startActivity(intent);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	protected void invalidateUI() {

		invalidateOptionsMenu();
	}


	/**
	 * invalidate the scan button state
	 */
	protected void uiInvalidateBtnState(){
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if(mBleDeviceBase.isConnected() == false
						&& mBleDeviceBase.isConnecting() == false){
					mBtnScan.setText(R.string.btn_scan);

				} else if(mBleDeviceBase.isConnected() == true){
					mBtnScan.setText(R.string.btn_disconnect);

				} else if(mBleDeviceBase.isConnecting() == true){
					mBtnScan.setText(R.string.btn_connecting);
				}

				invalidateOptionsMenu();
			}
		});
	}


	/*
	 ************************************* 
	 * Bluetooth adapter callbacks
	 * ***********************************
	 */  

	@Override
	public void onBleStopScan() {
		// dismiss' dialog if no devices are found.
		if(mListFoundDevicesHandler.getCount() <= 0){ 
			mDialogFoundDevices.dismiss();
		}
		uiInvalidateBtnState();
	}

	@Override
	public void onBleDeviceFound(BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		handleFoundDevice(device, rssi, scanRecord);
	}

	@Override
	public void onDiscoveryStop() {
		//NOT NEEDED
	}

	@Override
	public void onDiscoveryDeviceFound(BluetoothDevice device, int rssi) {
		//NOT NEEDED
	}

	protected void loadPref() {
		isPrefRunInBackground = mSharedPreferences.getBoolean("pref_run_in_background", true);
		isPrefPeriodicalScan = mSharedPreferences.getBoolean("pref_periodical_scan", true);
	}

}
