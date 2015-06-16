/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.serialdevice;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.R;
import com.lairdtech.lairdtoolkit.bases.BaseActivity;

public class SerialActivity extends BaseActivity implements SerialManagerUiCallback{

	private Button mBtnSend;
	private EditText mValueVspInputEt;
	private ScrollView mScrollViewVspOut;
	private TextView mValueVspOutTv;
	private TextView mValueRxCounterTv;
	private TextView mValueTxCounterTv;

	private SerialManager mSerialManager;

	private boolean isPrefClearTextAfterSending = false;


	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_serial);
		super.onCreate(savedInstanceState);

		mSerialManager = new SerialManager(this, this);
		setBleDeviceBase(mSerialManager.getVSPDevice());

		initialiseDialogAbout(getResources().getString(R.string.about_serial));
		initialiseDialogFoundDevices("VSP");

	}

	/*
	 * *************************************
	 * UI methods
	 * *************************************
	 */
	@Override
	protected void bindViews(){
		super.bindViews();
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mScrollViewVspOut = (ScrollView) findViewById(R.id.scrollViewVspOut);
		mValueVspInputEt = (EditText) findViewById(R.id.valueVspInputEt);
		mValueVspOutTv = (TextView) findViewById(R.id.valueVspOutTv);
		mValueRxCounterTv = (TextView) findViewById(R.id.valueRxCounterTv);
		mValueTxCounterTv = (TextView) findViewById(R.id.valueTxCounterTv);
	}

	@Override
	protected void setListeners(){
		super.setListeners();

		mBtnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				/*
				 * *********************
				 * to send data to module
				 * *********************
				 */
				String data = mValueVspInputEt.getText().toString();
				if(data != null){
					mBtnSend.setEnabled(false);
					if(mValueVspOutTv.getText().length() <= 0){
						mValueVspOutTv.append(">");
					} else{
						mValueVspOutTv.append("\n\n>");
					}

					mSerialManager.startDataTransfer(data + "\r");

					InputMethodManager inputManager = (InputMethodManager)
							getSystemService(Context.INPUT_METHOD_SERVICE); 

					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

					if(isPrefClearTextAfterSending == true){
						mValueVspInputEt.setText("");
					} else{
						// do not clear the text from the editText
					}

				}
			}
		});
	}

	@Override
	protected void onPause(){
		super.onPause();

		if(isInNewScreen == true
				|| isPrefRunInBackground == true){
			// let the app run normally in the background
		} else{
			// stop scanning or disconnect if we are connected
			if(mBluetoothAdapterWrapper.isBleScanning()){
				mBluetoothAdapterWrapper.stopBleScan();

			} else if(getBleDeviceBase().isConnecting()
					|| getBleDeviceBase().isConnected()){
				getBleDeviceBase().disconnect();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.serial, menu);
		getActionBar().setIcon(R.drawable.icon_serial);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (mBluetoothAdapterWrapper.isBleScanning() == true) {
			menu.findItem(R.id.action_scanning_indicator).setActionView(R.layout.progress_indicator);
		} else {
			menu.findItem(R.id.action_scanning_indicator).setActionView(null);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		
		case R.id.action_clear:
			mValueVspOutTv.setText("");
			mSerialManager.getVSPDevice().clearRxAndTxCounter();

			mValueRxCounterTv.setText("0");
			mValueTxCounterTv.setText("0");

			break;
		}
		return super.onOptionsItemSelected(item);
	}


	/*
	 * *************************************
	 * SerialManagerUiCallback
	 * *************************************
	 */
	@Override
	public void onUiConnected(BluetoothGatt gatt) {
		uiInvalidateBtnState();
	}

	@Override
	public void onUiDisconnect(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnSend.setEnabled(false);
			}
		});
		uiInvalidateBtnState();
	}

	@Override
	public void onUiConnectionFailure(
			final BluetoothGatt gatt){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnSend.setEnabled(false);
			}
		});
		uiInvalidateBtnState();
	}

	@Override
	public void onUiBatteryReadSuccess(String result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUiReadRemoteRssiSuccess(int rssi) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUiBonded() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUiVspServiceNotFound(BluetoothGatt gatt) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				mBtnSend.setEnabled(false);
			}
		});
	}

	@Override
	public void onUiVspRxTxCharsNotFound(BluetoothGatt gatt) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				mBtnSend.setEnabled(false);
			}
		});
	}

	@Override
	public void onUiVspRxTxCharsFound(BluetoothGatt gatt) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				mBtnSend.setEnabled(true);
			}
		});
	}

	@Override
	public void onUiSendDataSuccess(
			final String dataSend) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueVspOutTv.append(dataSend);
				mValueTxCounterTv.setText("" + mSerialManager.getVSPDevice().getTxCounter());
				mScrollViewVspOut.smoothScrollTo(0, mValueVspOutTv.getBottom());
			}
		});
	}

	@Override
	public void onUiReceiveData(final String dataReceived) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueVspOutTv.append(dataReceived);
				mValueRxCounterTv.setText("" + mSerialManager.getVSPDevice().getRxCounter());
				mScrollViewVspOut.smoothScrollTo(0, mValueVspOutTv.getBottom());	
			}
		});
	}

	@Override
	public void onUiUploaded() {
		mBtnSend.setEnabled(true);
	}


	/*
	 * *************************************
	 * other
	 * *************************************
	 */
	@Override
	protected void loadPref(){
		super.loadPref();
		isPrefClearTextAfterSending = mSharedPreferences.getBoolean("pref_clear_text_after_sending", false);
	}


}