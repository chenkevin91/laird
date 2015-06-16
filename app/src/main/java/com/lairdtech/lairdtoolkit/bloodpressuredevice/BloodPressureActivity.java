/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.bloodpressuredevice;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.R;
import com.lairdtech.lairdtoolkit.bases.BaseActivity;

public class BloodPressureActivity extends BaseActivity implements BloodPressureActivityUiCallback {
	private BloodPressureManager mBloodPressureManager;
	private TextView mSystolicResult;
	private TextView mDiastolicResult;
	private TextView mArterialPressureResult;
	private BloodPressureGraph mGraph;
	private View mView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_blood_pressure);
		mView =  ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
		super.onCreate(savedInstanceState);

		mBloodPressureManager = new BloodPressureManager(this, mActivity);

		setBleDeviceBase(mBloodPressureManager);
		initialiseDialogAbout(getResources().getString(R.string.about_blood_pressure));
		initialiseDialogFoundDevices("Blood Pressure");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.blood_pressure, menu);
		getActionBar().setIcon(R.drawable.icon_blood_pressure);
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
		mSystolicResult = (TextView) findViewById(R.id.valueSystolic);
		mDiastolicResult = (TextView) findViewById(R.id.valueDiastolic);
		mArterialPressureResult = (TextView) findViewById(R.id.valueArterialPressure);
		mGraph = new BloodPressureGraph(mActivity, mView);
	};


	/*
	 * *************************************
	 * remote device operation UI callbacks
	 * *************************************
	 * Always update the UI on the runOnUiThread
	 */
	@Override
	public void onUiConnected(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnScan.setText(getResources().getString(R.string.btn_disconnect));
				mValueName.setText(mBloodPressureManager.getName());
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
				mSystolicResult.setText(getResources().getString(R.string.non_applicable));
				mDiastolicResult.setText(getResources().getString(R.string.non_applicable));
				mArterialPressureResult.setText(getResources().getString(R.string.non_applicable));
				mValueRSSI.setText(getResources().getString(R.string.non_applicable));
				if(mGraph != null){
					mGraph.clearGraph();
				}

			}
		});
		mGraph.setStartTime(0);		
	}

	@Override
	public void onUiConnectionFailure(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnScan.setText(getResources().getString(R.string.btn_scan));
				mValueBattery.setText(getResources().getString(R.string.non_applicable));
				mValueName.setText(getResources().getString(R.string.non_applicable));
				mSystolicResult.setText(getResources().getString(R.string.non_applicable));
				mDiastolicResult.setText(getResources().getString(R.string.non_applicable));
				mArterialPressureResult.setText(getResources().getString(R.string.non_applicable));
				mValueRSSI.setText(getResources().getString(R.string.non_applicable));
				if(mGraph != null){
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
	public void onUIBloodPressureRead(final String mValueBloodPressureSystolicResult,
			final String mValueBloodPressureDiastolicResult,
			final String mValueBloodPressureArterialPressureResult) {
		mGraph.startTimer();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSystolicResult.setText(mValueBloodPressureSystolicResult);
				mDiastolicResult.setText(mValueBloodPressureDiastolicResult);
				mArterialPressureResult.setText(mValueBloodPressureArterialPressureResult);
			
				final String [] values = {
						mValueBloodPressureSystolicResult,
						mValueBloodPressureDiastolicResult,
						mValueBloodPressureArterialPressureResult
				};
				
				mGraph.addNewData(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
				
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
}
