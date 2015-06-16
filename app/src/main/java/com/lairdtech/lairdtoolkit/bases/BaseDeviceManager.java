/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.bases;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.lairdtech.bt.ble.BleDefinedUUIDs;
import com.lairdtech.bt.ble.BleDeviceBase;

/**
 *Provides all the general/shared methods for BLE device types, each device type (thermometer, heart rate, ect.) will have their own manager but extend the generalDeviceManager.
 *This removes a lot of duplicate code so class' are cleaner and more consise. 
 * @author ben.hammonds
 *
 */
public class BaseDeviceManager extends BleDeviceBase{
	private static final Integer RSSI_UPDATE_INTERVAL = 2000;
	@SuppressWarnings("unused")
	private static final String TAG = "BaseDeviceManager";

    private BaseActivityUiCallback mBaseActivityUiCallback;
    protected BluetoothGattCharacteristic mCharBattery;
    protected String mValueBattery;

    /*
     * *************************************
     * Constructors
     * *************************************
     */
    public BaseDeviceManager(Activity activity, BaseActivityUiCallback baseActivityUiCallback) {
		super(activity);
		mBaseActivityUiCallback = baseActivityUiCallback;
	}
	
    /*
     * *************************************
     * getters
     * *************************************
     */
    public String getValueBattery(){
        return mValueBattery;
    }
    
    /*
     * *************************************
     * overridden remote device operation callback's
     * *************************************
     */
    @Override
    public void onBleConnected(BluetoothGatt gatt) {
    	mBaseActivityUiCallback.onUiConnected(gatt);
        /*
         * once we connect with a device we search for the services and chars.
         * When this operation finishes the callback onServicesAndCharsFound()
         * method is called with the found services, chars and descriptors.
         */
    	readRssiPeriodicaly(true, RSSI_UPDATE_INTERVAL);
        discoverServicesAndChars();
    }
	
    @Override
    public void onBleDisconnected(BluetoothGatt gatt) {
    	mBaseActivityUiCallback.onUiDisconnect(gatt);
        /*
         * the GATT client should always be closed once we are done entirely with the
         * remote device
         */
        closeGatt();
    }

    @Override
    public void onBleConnectionStateChangeFailure(BluetoothGatt gatt, int status, int newState) {
        super.onBleConnectionStateChangeFailure(gatt, status, newState);
        mBaseActivityUiCallback.onUiConnectionFailure(gatt);
    }
    
    @Override
    public void onCharacteristicReadSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
        super.onCharacteristicReadSuccess(gatt, ch);
        UUID currentCharUUID = ch.getUuid();
        
        if(BleDefinedUUIDs.Characteristic.BATTERY_LEVEL.equals(currentCharUUID)){
            int result = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            mValueBattery = result + " %";
            mBaseActivityUiCallback.onUiBatteryReadSuccess(mValueBattery);
        }
    }
    
    @Override
    protected void onReadRemoteRssiSuccess(BluetoothGatt gatt, int rssi) {
    	super.onReadRemoteRssiSuccess(gatt, rssi);
    	
    	mBaseActivityUiCallback.onUiReadRemoteRssiSuccess(rssi);
    }
    
    
    @Override
    protected void onBonded() {
//    	mGeneralActivityUiCallback.onUiBonded();
//    	if(isConnected() == false){
    		// we got disconnected while bonding so let's close the GATT client
//    		closeGatt();
//    	}
    }
    
}
