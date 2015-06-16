/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.heartratedevice;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.lairdtech.bt.ble.BleDefinedUUIDs;
import com.lairdtech.bt.ble.BleNamesResolver;
import com.lairdtech.lairdtoolkit.bases.BaseDeviceManager;
import com.lairdtech.misc.DebugWrapper;

public class HeartRateManager extends BaseDeviceManager {

	
	private static final String TAG = "HeartRateDevice";
	private HeartRateActivityUiCallback mHeartRateActivityUiCallback;
	private String mValueHeartRateMeasurement;
	private String mValueBodySensorLocation;
	private BluetoothGattCharacteristic mCharHrMeasurement;
	private BluetoothGattCharacteristic mCharBodySensorLocation;
	
	
	/*
	 * the array with the Service UUIDs searching for
	 */
	private UUID[] mServicesUUIDs = {
			BleDefinedUUIDs.Service.BATTERY,
			BleDefinedUUIDs.Service.HEART_RATE
	};
	
	/*
	 * the array with the characteristics UUIDs searching for    
	 */
	private UUID[] mCharsUUIDs = {
			BleDefinedUUIDs.Characteristic.BATTERY_LEVEL,
			BleDefinedUUIDs.Characteristic.HEART_RATE_MEASUREMENT,
			BleDefinedUUIDs.Characteristic.BODY_SENSOR_LOCATION
	};
	
	/*
	 * *************************************
	 * Constructors
	 * *************************************
	 */
	public HeartRateManager(HeartRateActivityUiCallback heartRateActivityUiCallback, Activity mActivity) {
		super(mActivity, heartRateActivityUiCallback);
		if(heartRateActivityUiCallback == null)
			throw new NullPointerException("HeartRateActivityUiCallback parameter is NULL");

		mHeartRateActivityUiCallback = heartRateActivityUiCallback;	
		
	}

	/*
	 * *************************************
	 * overridden remote device operation callback's
	 * *************************************
	 */
	@Override
	protected void onServicesDiscoveredSuccess(BluetoothGatt gatt) {
		/*
		 * looping only through the services we are looking for
		 */
		for(int i=0; i < mServicesUUIDs.length; i++){
			BluetoothGattService currentServiceFound = getService(mServicesUUIDs[i]);

			if(currentServiceFound != null){
				DebugWrapper.infoMsg("Service Found with UUID='" + currentServiceFound.getUuid(), TAG, DebugWrapper.getDebugMessageVisibility());
				/*
				 * if no requested chars were given then we skip characteristics searching,
				 * else we loop through the chars found in this service
				 */
				if(mCharsUUIDs == null) continue;
				for(int j=0; j < mCharsUUIDs.length; j++){
					BluetoothGattCharacteristic currentCharFound = currentServiceFound.getCharacteristic(mCharsUUIDs[j]);

					if(currentCharFound == null) continue;
					DebugWrapper.infoMsg("Characteristic Found with UUID='" + currentCharFound.getUuid(), TAG, DebugWrapper.getDebugMessageVisibility());

					if(BleDefinedUUIDs.Characteristic.BATTERY_LEVEL.equals(currentCharFound.getUuid())){
						mCharBattery = currentCharFound;
					} else if(BleDefinedUUIDs.Characteristic.HEART_RATE_MEASUREMENT.equals(currentCharFound.getUuid())){
						mCharHrMeasurement = currentCharFound;
					} else if(BleDefinedUUIDs.Characteristic.BODY_SENSOR_LOCATION.equals(currentCharFound.getUuid())){
						/*
						 * we know that the battery characteristic was found first because, we set that characteristic
						 * as the first characteristic in our mServicesUUIDs to search for through 
						 */
						mCharBodySensorLocation = currentCharFound;
					}
				}
			} else{
				/*
				 * at least one of the characteristics requested was not found,
				 * lets disconnect from the device.
				 */
				System.out.println("One or more Characteristics were not found, disconnecting...");
				DebugWrapper.toastMsg(mActivity, "One or more Characteristics were not found, disconnecting...");
				disconnect();
				break;
			}
		}
		if(mCharHrMeasurement != null){
			setCharacteristicNotificationOrIndication(mCharHrMeasurement, true);
		}
	}
 
	@Override
	public void onDescriptorWriteSuccess(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {
		super.onDescriptorWriteSuccess(gatt, descriptor);
		/*
		 * no more characteristics to enable notifications/indications so lets start
		 * reading characteristics values
		 */
		readCharacteristic(mCharBattery);
	}

	@Override
	public void onCharacteristicReadSuccess(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch) {
		if(BleDefinedUUIDs.Characteristic.BATTERY_LEVEL.equals(ch.getUuid())){
			readCharacteristic(mCharBodySensorLocation);
		} else if(BleDefinedUUIDs.Characteristic.BODY_SENSOR_LOCATION.equals(ch.getUuid())){
			int result = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			
			mValueBodySensorLocation = BleNamesResolver.resolveHeartRateSensorLocation(result);
			mHeartRateActivityUiCallback.onUiSensorPosition(mValueBodySensorLocation);
		}
		super.onCharacteristicReadSuccess(gatt, ch);
	}
	
	@Override
	protected void onCharacteristicReadFailure(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch, int status) {
		if(BleDefinedUUIDs.Characteristic.BATTERY_LEVEL.equals(ch.getUuid())){
			readCharacteristic(mCharBodySensorLocation);
		}
		super.onCharacteristicReadFailure(gatt, ch, status);
	}

	@Override
	public void onCharacteristicChangedSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
		super.onCharacteristicChangedSuccess(gatt, ch);
		UUID currentCharUUID = ch.getUuid();

		if(BleDefinedUUIDs.Characteristic.HEART_RATE_MEASUREMENT.equals(currentCharUUID)){
			int result = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
			mValueHeartRateMeasurement = result + "";
			mHeartRateActivityUiCallback.onUiHeartRateChange(mValueHeartRateMeasurement);
		}
	}
	
}
