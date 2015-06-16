/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.proximitydevice;

import java.math.BigInteger;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.R;
import com.lairdtech.lairdtoolkit.bases.BaseActivity;

public class ProximityActivity extends BaseActivity implements ProximityActivityUiCallback, OnClickListener, OnCheckedChangeListener {
	private ProximityManager mProximityManager;
	private TextView mValueTxPower;
	private Button btnImmediateAlert;
	private RadioGroup radioGroupLinkLoss;
	private RadioGroup radioGroupImmediateAlert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_proximity);
		super.onCreate(savedInstanceState);
		
		mProximityManager = new ProximityManager(this, mActivity);
		setBleDeviceBase(mProximityManager);
		
		initialiseDialogAbout(getResources().getString(R.string.about_proximity));
		initialiseDialogFoundDevices("Proximity");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.proximity, menu);
		getActionBar().setIcon(R.drawable.icon_proximity);
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
		mValueTxPower = (TextView) findViewById(R.id.valueTxPower);
		radioGroupLinkLoss = (RadioGroup) findViewById(R.id.radioGroupLinkLoss);
		radioGroupImmediateAlert = (RadioGroup) findViewById(R.id.radioGroupImmediateAlert);
		btnImmediateAlert = (Button) findViewById(R.id.btnImmediateAlert);  
	};
	
	@Override
	public void setListeners(){
		super.setListeners();
		btnImmediateAlert.setOnClickListener(this);
		radioGroupLinkLoss.setOnCheckedChangeListener(this);
		radioGroupImmediateAlert.setOnCheckedChangeListener(this);
		
	}

	public void onClick(View view){
		int btnId = view.getId();
		switch(btnId){
		case R.id.btnImmediateAlert:
			int checkedRadioBtn = radioGroupImmediateAlert.getCheckedRadioButtonId();

			if(checkedRadioBtn == R.id.radioImmediateAlertLow){
				// low value chosen for Immediate Alert
				mProximityManager.writeAlertCharValue("0x00", 1);
			} else if(checkedRadioBtn == R.id.radioImmediateAlertMedium){
				// medium value chosen for Immediate Alert
				mProximityManager.writeAlertCharValue("0x01", 1);
			} else if(checkedRadioBtn == R.id.radioImmediateAlertHigh){
				// high value chosen for Immediate Alert
				mProximityManager.writeAlertCharValue("0x02", 1);
			} else {
				// no radio button is chosen yet
			}
			break;
		}
	}
	
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int radioGroupId = group.getId();
        RadioButton radioButton;
        int radioButtonId;
        // get selected radio button
        radioButton = (RadioButton) group.findViewById(checkedId);
        radioButtonId = radioButton.getId();
        
        switch(radioGroupId){
            case R.id.radioGroupLinkLoss:
                if(radioButtonId == R.id.radioLinkLossAlertLow){
                    // low value chosen for Link loss
                	mProximityManager.writeAlertCharValue("0x00", 0);
                } else if(radioButtonId == R.id.radioLinkLossAlertMedium){
                    // medium value chosen for Link loss
                	mProximityManager.writeAlertCharValue("0x01", 0);
                } else if(radioButtonId == R.id.radioLinkLossAlertHigh){
                    // high value chosen for Link loss
                	mProximityManager.writeAlertCharValue("0x02", 0);
                } else{
                    // no radio button is checked from this radio group
                }
                break;
        }
    }

	@Override
	public void onUiConnected(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnScan.setText(getResources().getString(R.string.btn_disconnect));
				mValueName.setText(mProximityManager.getName());
			}
		});
	}

	@Override
	public void onUiDisconnect(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnScan.setText(getResources().getString(R.string.btn_scan));
				mValueBattery.setText(getResources().getString(R.string.non_applicable));
				mValueName.setText(getResources().getString(R.string.non_applicable));		
				mValueRSSI.setText(getResources().getString(R.string.non_applicable));
				mValueTxPower.setText(getResources().getString(R.string.non_applicable));
			}
		});
	}

	@Override
	public void onUiConnectionFailure(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnScan.setText(getResources().getString(R.string.btn_scan));
				mValueBattery.setText(getResources().getString(R.string.non_applicable));
				mValueName.setText(getResources().getString(R.string.non_applicable));		
				mValueRSSI.setText(getResources().getString(R.string.non_applicable));
				mValueTxPower.setText(getResources().getString(R.string.non_applicable));
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
	public void onUiReadTxPower(final byte[] mTxValue) {
		
		final BigInteger result = new BigInteger(mTxValue);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueTxPower.setText(result+" dB");
			}
		});
	}

	@Override
	public void onUiReadRemoteRssiSuccess(final int rssi) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueRSSI.setText(rssi + " dB");
			}
		});
	}

	@Override
	public void onUiBonded() {
	}

}
