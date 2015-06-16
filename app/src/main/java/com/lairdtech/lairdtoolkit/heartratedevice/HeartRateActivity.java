/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.heartratedevice;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.R;
import com.lairdtech.lairdtoolkit.bases.BaseActivity;

public class HeartRateActivity extends BaseActivity implements HeartRateActivityUiCallback	{
	private HeartRateManager mHeartRateManager;
	private TextView mValueHeartRate;
	private TextView mValueSensorPosition;
	private HeartRateGraph mGraph;
	private View mView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_heart_rate);
		mView =  ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
		super.onCreate(savedInstanceState);

		mHeartRateManager = new HeartRateManager(this, mActivity);
		setBleDeviceBase(mHeartRateManager);

		initialiseDialogAbout(getResources().getString(R.string.about_heart_rate));
		initialiseDialogFoundDevices("HRM");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.heart_rate, menu);
		getActionBar().setIcon(R.drawable.icon_heart_rate);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (mBluetoothAdapterWrapper.isBleScanning() == true) {
			menu.findItem(R.id.action_scanning_indicator).setActionView(R.layout.progress_indicator);
		} else {
			menu.findItem(R.id.action_scanning_indicator).setActionView(null);
		}
		return true;
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
	protected void bindViews() {
		super.bindViews();
		mValueHeartRate = (TextView) findViewById(R.id.valueCharBPM);
		mValueSensorPosition = (TextView) findViewById(R.id.valueCharBodySensor);
		mGraph = new HeartRateGraph(mActivity, mView);
	}

	@Override
	public void onUiConnected(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnScan.setText("Disconnect");
				mValueName.setText(mHeartRateManager.getName());
			}
		});
		invalidateUI();			
	}

	@Override
	public void onUiDisconnect(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnScan.setText(getResources().getString(R.string.btn_scan));
				mValueBattery.setText(getResources().getString(R.string.non_applicable));
				mValueName.setText(getResources().getString(R.string.non_applicable));
				mValueHeartRate.setText(getResources().getString(R.string.no_single_data_found));
				mValueRSSI.setText(getResources().getString(R.string.non_applicable));
				mValueHeartRate.setText(getResources().getString(R.string.HeartRateValue));
				mValueSensorPosition.setText(getResources().getString(R.string.non_applicable));
				if(mGraph != null){
					mGraph.clearGraph();
				}

			}
		});
		mGraph.setStartTime(0);
	}

	@Override
	public void onUiConnectionFailure(BluetoothGatt gatt){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(mGraph != null){
					mBtnScan.setText(getResources().getString(R.string.btn_scan));
					mValueBattery.setText(getResources().getString(R.string.non_applicable));
					mValueName.setText(getResources().getString(R.string.non_applicable));
					mValueHeartRate.setText(getResources().getString(R.string.no_single_data_found));
					mValueRSSI.setText(getResources().getString(R.string.non_applicable));
					mValueHeartRate.setText(getResources().getString(R.string.HeartRateValue));
					mValueSensorPosition.setText(getResources().getString(R.string.non_applicable));
					mGraph.clearGraph();
				}
			}
		});
	}

	@Override
	public void onUiBatteryReadSuccess(final String result) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueBattery.setText(result);
			}
		});
	}

	@Override
	public void onUiReadRemoteRssiSuccess(final int rssi) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueRSSI.setText(rssi + " db");
			}
		});
	}

	@Override
	public void onUiBonded() {
		invalidateOptionsMenu();
	}

	@Override
	public void onUiHeartRateChange(final String  mCharHrMeasurement){
		mGraph.startTimer();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueHeartRate.setText(mCharHrMeasurement + " bpm");
				mGraph.addNewData(Double.parseDouble(mCharHrMeasurement));
			}
		});
	}

	@Override
	public void onUiSensorPosition(final String mBodySensorLocation){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueSensorPosition.setText(mBodySensorLocation);
			}
		});
	}
}
