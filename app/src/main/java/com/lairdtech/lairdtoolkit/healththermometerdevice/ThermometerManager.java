/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.healththermometerdevice;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.lairdtech.bt.ble.BleDefinedUUIDs;
import com.lairdtech.lairdtoolkit.bases.BaseDeviceManager;
import com.lairdtech.misc.DebugWrapper;

public class ThermometerManager extends BaseDeviceManager{
	private static final String TAG = "TemperatureDevice";
	private ThermometerActivityUICallback mThermometerActivityUICallback;
	private BluetoothGattCharacteristic mCharTempMeasurement;
	private String mValueTempMeasurement;

	/*
	 * the array with the Service UUIDs searching for
	 */
	private UUID[] mServicesUUIDs = {
			BleDefinedUUIDs.Service.BATTERY, 
			BleDefinedUUIDs.Service.HEALTH_THERMOMETER
	};
	/*
	 * the array with the characteristics UUIDs searching for    
	 */
	private UUID[] mCharsUUIDs = {
			BleDefinedUUIDs.Characteristic.BATTERY_LEVEL,
			BleDefinedUUIDs.Characteristic.TEMPERATURE_MEASUREMENT
	};

	/*
	 * *************************************
	 * Constructors
	 * *************************************
	 */
	public ThermometerManager(ThermometerActivityUICallback thermometerActivityUICallback,Activity activity) {
		super(activity, thermometerActivityUICallback);
		if(thermometerActivityUICallback == null)
			throw new NullPointerException("TemperatureDeviceUiCallback parameter is NULL");

		mThermometerActivityUICallback = thermometerActivityUICallback;
	}

	/*
	 * *************************************
	 * getters
	 * *************************************
	 */
	public String getValueTempMeasurement(){
		return mValueTempMeasurement;
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
					} else if(BleDefinedUUIDs.Characteristic.TEMPERATURE_MEASUREMENT.equals(currentCharFound.getUuid())){
						/*
						 * we know that the battery characteristic was found first because, we set that characteristic
						 * as the first characteristic in our mServicesUUIDs to search for through 
						 */
						mCharTempMeasurement = currentCharFound;
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
		if(mCharTempMeasurement != null){
			setCharacteristicNotificationOrIndication(mCharTempMeasurement, true);
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
	public void onCharacteristicChangedSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
		super.onCharacteristicChangedSuccess(gatt, ch);
		UUID currentCharUUID = ch.getUuid();

		if(BleDefinedUUIDs.Characteristic.TEMPERATURE_MEASUREMENT.equals(currentCharUUID)){
			float result = ch.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
			mValueTempMeasurement = result + "";
			mThermometerActivityUICallback.onUiTemperatureChange(mValueTempMeasurement);
		}
	}
}
