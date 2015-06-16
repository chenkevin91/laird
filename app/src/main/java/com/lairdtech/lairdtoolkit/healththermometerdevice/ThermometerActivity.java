/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.healththermometerdevice;


import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.R;
import com.lairdtech.lairdtoolkit.bases.BaseActivity;

public class ThermometerActivity extends BaseActivity implements ThermometerActivityUICallback{

	private ThermometerManager mThermometerManager;
	private TextView mValueTemperature;
	private TempGraph mGraph;
	private View mView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//Set content view before calling the super, this is so that the BaseActivity has reference to the layout
		setContentView(R.layout.activity_thermometer);
		mView =  ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
		super.onCreate(savedInstanceState);

		mThermometerManager = new ThermometerManager(this, mActivity);
		setBleDeviceBase(mThermometerManager);
		initialiseDialogAbout(getResources().getString(R.string.about_thermometer));

		initialiseDialogFoundDevices("Thermometer");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.thermometer, menu);
		getActionBar().setIcon(R.drawable.icon_temp);
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
		// Bind all generic views in the super. Bind specific views here.
		mValueTemperature = (TextView) findViewById(R.id.valueTemperature);
		mGraph = new TempGraph(mActivity, mView);
	}


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
				mBtnScan.setText("Disconnect");
				mValueName.setText(mThermometerManager.getName());
			}
		});
		//		invalidateUI();	
	}

	@Override
	public void onUiDisconnect(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnScan.setText(getResources().getString(R.string.btn_scan));
				mValueBattery.setText(getResources().getString(R.string.non_applicable));
				mValueName.setText(getResources().getString(R.string.non_applicable));
				mValueTemperature.setText(getResources().getString(R.string.no_single_data_found));
				mValueRSSI.setText(getResources().getString(R.string.non_applicable));
				if(mGraph != null){
					mGraph.clearGraph();
				}
			}
		});
		//		invalidateUI();	
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
				mValueTemperature.setText(getResources().getString(R.string.no_single_data_found));
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
		//		invalidateUI();		
	} 

	@Override
	public void onUiTemperatureChange(final String result) {
		mGraph.startTimer();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueTemperature.setText(result + " °C");
				mGraph.addNewData(Double.parseDouble(result));
			}
		});
		//		invalidateUI();	
	}

	@Override
	public void onUiReadRemoteRssiSuccess(final int rssi) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueRSSI.setText(rssi + " db");
			}
		});
		//		invalidateUI();	
	}

	@Override
	public void onUiBonded() {
		invalidateOptionsMenu();
	}
}
