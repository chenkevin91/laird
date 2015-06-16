/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.proximitydevice;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.lairdtech.bt.ble.BleDefinedUUIDs;
import com.lairdtech.lairdtoolkit.bases.BaseDeviceManager;
import com.lairdtech.misc.DebugWrapper;

public class ProximityManager extends BaseDeviceManager{
	private static final String TAG = "Proximity Device";
	private ProximityActivityUiCallback mProximityActivityUiCallback;
	private BluetoothGattCharacteristic mCharImmediateAlert;
	private BluetoothGattCharacteristic mCharLinkLoss; 
	private BluetoothGattCharacteristic mCharTxPower;  

	private final UUID[] mServicesUUIDs = {
			BleDefinedUUIDs.Service.BATTERY,
			BleDefinedUUIDs.Service.LINK_LOSS,
			BleDefinedUUIDs.Service.IMMEDIATE_ALERT,
			BleDefinedUUIDs.Service.TX_POWER
	};
	private final UUID[] mCharsUUIDs = {
			BleDefinedUUIDs.Characteristic.ALERT_LEVEL,
			BleDefinedUUIDs.Characteristic.TX_POWER_LEVEL,
			BleDefinedUUIDs.Characteristic.BATTERY_LEVEL
	};

	public ProximityManager(ProximityActivityUiCallback proximityActivityUiCallback,
			Activity mActivity) {
		super(mActivity, proximityActivityUiCallback);

		if(proximityActivityUiCallback == null)
			throw new NullPointerException("ProximityDeviceUiCallback parameter is NULL");
		mProximityActivityUiCallback = proximityActivityUiCallback;
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
					}else if(BleDefinedUUIDs.Characteristic.TX_POWER_LEVEL.equals(currentCharFound.getUuid())){
						mCharTxPower = currentCharFound;

					}else if(BleDefinedUUIDs.Service.LINK_LOSS.equals(currentServiceFound.getUuid()) &&
							BleDefinedUUIDs.Characteristic.ALERT_LEVEL.equals(currentCharFound.getUuid())){
						mCharLinkLoss = currentCharFound;

					} else if(BleDefinedUUIDs.Service.IMMEDIATE_ALERT.equals(currentServiceFound.getUuid()) && 
							BleDefinedUUIDs.Characteristic.ALERT_LEVEL.equals(currentCharFound.getUuid())){
						mCharImmediateAlert = currentCharFound;
					}
				}
			}else{
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
		if(mCharBattery != null){
			readCharacteristic(mCharBattery);
		}
	}


	@Override
	public void onDescriptorWriteSuccess(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {
		super.onDescriptorWriteSuccess(gatt, descriptor);
	}


	@Override
	public void onCharacteristicReadSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
		super.onCharacteristicReadSuccess(gatt, ch);
		UUID currentCharUUID = ch.getUuid();
		if(BleDefinedUUIDs.Characteristic.BATTERY_LEVEL.equals(currentCharUUID)){
			readCharacteristic(mCharTxPower);
		}
		if(BleDefinedUUIDs.Characteristic.TX_POWER_LEVEL.equals(currentCharUUID)){
			byte[] txPower = ch.getValue();
			DebugWrapper.debugMsg(txPower+"", "tx", DebugWrapper.getDebugMessageVisibility());
			mProximityActivityUiCallback.onUiReadTxPower(txPower);
		}
	}

	public void writeAlertCharValue(final String hex, int linkLossOrImmediateAlert){
		if(linkLossOrImmediateAlert == 0){
			if(mCharLinkLoss == null) return;
			// first set it locally....      
			mCharLinkLoss.setValue(parseHexStringToBytes(hex));
			// ... and then "commit" changes to the peripheral
			writeCharacteristic(mCharLinkLoss);
			readCharacteristic(mCharTxPower); 

		}else if (linkLossOrImmediateAlert == 1){
			if(mCharImmediateAlert == null) return;
			// first set it locally....          
			mCharImmediateAlert.setValue(parseHexStringToBytes(hex));
			// ... and then "commit" changes to the peripheral
			writeCharacteristic(mCharImmediateAlert);
		}
	}

	public byte[] parseHexStringToBytes(final String hex) {
		String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
		byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally
		String part = "";

		for(int i = 0; i < bytes.length; ++i) {
			part = "0x" + tmp.substring(i*2, i*2+2);
			bytes[i] = Long.decode(part).byteValue();
		}
		return bytes;
	}


	@Override
	protected void onCharacteristicReadFailure(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch, int status) {
		super.onCharacteristicReadFailure(gatt, ch, status);
		readCharacteristic(ch);
	}
	
	
}

