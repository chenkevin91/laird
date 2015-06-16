/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.bt.ble.vsp;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.lairdtech.bt.ble.BleDeviceBase;
import com.lairdtech.misc.DebugWrapper;


/**
 * Responsible for the communication between the android device and a module that has the
 * Virtual Serial Port (VSP) service
 * 
 * <br>Include's the error codes that the module can send in case of an error and the UUIDs of the
 * service and of the characteristics
 * 
 * <br>Give callback's to the class that implements the VirtualSerialPortDeviceCallback interface. 
 * 
 */
public class VirtualSerialPortDevice extends BleDeviceBase{

	public final static UUID VSP_SERVICE = UUID.fromString("569a1101-b87f-490c-92cb-11ba5ea5167c");
	public final static UUID VSP_CHAR_TX = UUID.fromString("569a2000-b87f-490c-92cb-11ba5ea5167c");
	public final static UUID VSP_CHAR_RX = UUID.fromString("569a2001-b87f-490c-92cb-11ba5ea5167c");
	public final static UUID VSP_CHAR_MODEM_OUT = UUID.fromString("569a2002-b87f-490c-92cb-11ba5ea5167c");
	public final static UUID VSP_CHAR_MODEM_IN = UUID.fromString("569a2003-b87f-490c-92cb-11ba5ea5167c");

	/*
	 * module VSP responses when in command mode
	 */
	public final static String SUCCESS_CODE = "00";
	public final static String ERROR_CODE_NO_FILE_TO_CLOSE = "E037";
	public final static String ERROR_CODE_MEMORY_FULL = "5002";
	public final static String ERROR_CODE_FSA_FAIL_OPENFILE = "1809";
	public final static String ERROR_CODE_FSA_FILENAME_TOO_LONG = "1803";
	public final static String ERROR_CODE_FILE_NOT_OPEN = "E038";
	public final static String ERROR_CODE_INCORRECT_MODE = "E00E";
	public final static String ERROR_CODE_UNKNOWN_COMMAND = "E007";
	public final static String ERROR_CODE_UNKNOWN_SUBCOMMAND = "E00D";
	public final static String ERROR_CODE_UNEXPECTED_PARM = "E002";

	/*
	 * *************************************
	 * private variables
	 * *************************************
	 */
	private static final String TAG = "VirtualSerialPortDevice";
	private boolean mIsValidVspDevice = false;


	/*
	 * the array with the Service UUIDs searching for
	 */
	private UUID[] mServicesUUIDs = {
			VSP_SERVICE
	};
	/*
	 * the array with the characteristics UUIDs searching for    
	 */
	private UUID[] mCharsUUIDs = {
			VSP_CHAR_TX,
			VSP_CHAR_RX,
			VSP_CHAR_MODEM_OUT,
			VSP_CHAR_MODEM_IN
	};

	private VirtualSerialPortDeviceCallback mVSPDeviceCallback;
	/**
	 * Service that can send and receive data over the air
	 */
	private BluetoothGattService mServiceVsp;
	/**
	 * module char that retrieves data.
	 * This means we use it to send data from the android device to the remote module
	 */
	private BluetoothGattCharacteristic mCharRx;
	/**
	 * module char that sends data.
	 * This means we use it to receive data through notifications from the remote module
	 */
	private BluetoothGattCharacteristic mCharTx;
	/**
	 * module: i can send some data to the other device now 
	 * Note: This char is found if the BLE device is in bridge mode, otherwise if the
	 * module is not in bridge mode it will need to be enabled.
	 * This characteristic is currently not used as android does not have a
	 * limited buffer size
	 */
	private BluetoothGattCharacteristic mCharModemIn;
	/**
	 * module: am ready to retrieve some data
	 * - when value is 1 it means that the module buffer is not full and it can retrieve more data
	 * - when the value is 0 it means that the module buffer is full and therefore if we
	 *   send it any more data they will get lost
	 * Note: This char is found if the BLE device is in bridge mode, otherwise if the
	 * module is not in bridge mode it will need to be enabled
	 */
	private BluetoothGattCharacteristic mCharModemOut;
	/**
	 * BLE module buffer full or not?
	 */
	private boolean mIsBufferSpaceAvailableNewState = true;

	/*
	 * counters for the total data send and received
	 */
	private int mRxCounter;
	private int mTxCounter;



	/*
	 * *************************************
	 * constructor's
	 * *************************************
	 */
	public VirtualSerialPortDevice(Activity activity, VirtualSerialPortDeviceCallback virtualSerialPortDeviceCallback){
		super(activity);
		if(virtualSerialPortDeviceCallback == null)
			throw new NullPointerException("VirtualSerialPortDeviceCallback parameter is NULL");

		mVSPDeviceCallback = virtualSerialPortDeviceCallback;
	}


	/*
	 * *************************************
	 * getter methods
	 * *************************************
	 */
	public BluetoothGattService getServiceVsp() {return mServiceVsp;}
	public BluetoothGattCharacteristic getCharRx() {return mCharRx;}
	public BluetoothGattCharacteristic getCharTx() {return mCharTx;}
	public BluetoothGattCharacteristic getCharModemIn() {return mCharModemIn;}
	public BluetoothGattCharacteristic getCharModemOut() {return mCharModemOut;}
	public boolean isBufferSpaceAvailable() {return mIsBufferSpaceAvailableNewState;}
	public int getRxCounter() { return mRxCounter;}
	public int getTxCounter() {return mTxCounter;}
	public boolean isValidVspDevice() {return mIsValidVspDevice;}


	/*
	 * *************************************
	 * setter methods
	 * *************************************
	 */
	public void clearRxCounter() {
		mRxCounter = 0;
	}

	public void clearTxCounter() {
		mTxCounter = 0;
	}

	public void clearRxAndTxCounter() {
		mRxCounter = 0;
		mTxCounter = 0;
	}


	/*
	 * *************************************
	 * public methods
	 * *************************************
	 */
	protected boolean sendToModule(String dataToBeSend){
		DebugWrapper.errorMsg("sendToModule", TAG, DebugWrapper.getDebugMessageVisibility());
		mCharRx.setValue(dataToBeSend);

		return writeCharacteristic(mCharRx);
	}


	/*
	 * *************************************
	 * BleDeviceBase Callback's
	 * *************************************
	 */
	@Override
	protected void onBleConnected(
			BluetoothGatt gatt) {
		discoverServicesAndChars();
		mVSPDeviceCallback.onConnected(gatt);
	}
	
	@Override
	protected void onBleDisconnected(
			BluetoothGatt gatt) {
		/*
		 * call the UI callback before closing the GATT client as we might need
		 * to do something with the GATT client just before disconnecting
		 */
		mVSPDeviceCallback.onDisconnected(gatt);
		setToDefault();

		if(getBluetoothGatt() != null){
			closeGatt();
		}
	}

	@Override
	protected void onBleConnectionStateChangeFailure(BluetoothGatt gatt,
			int status, int newState) {
		mVSPDeviceCallback.onConnectionStateChangeFailure(gatt, status, newState);
		
		setToDefault();

		if(getBluetoothGatt() != null){
			closeGatt();
		}
	}

	@Override
	protected void onServicesDiscoveredSuccess(
			BluetoothGatt gatt) {

		if(mServicesUUIDs == null || mCharsUUIDs == null)
			throw new NullPointerException("mServicesUUIDs and mCharsUUIDs variables are null");

		/*
		 * looping only through the services and characteristics we are looking for and storing them
		 */
		for(int i=0; i < mServicesUUIDs.length; i++){

			BluetoothGattService currentServiceFound = getService(mServicesUUIDs[i]);

			if(currentServiceFound != null){
				DebugWrapper.infoMsg("Service Found with UUID: '" + currentServiceFound.getUuid(), TAG, DebugWrapper.getDebugMessageVisibility());

				if(VSP_SERVICE.equals(currentServiceFound.getUuid())){
					mServiceVsp = currentServiceFound;
				}

				for(int j=0; j < mCharsUUIDs.length; j++){
					BluetoothGattCharacteristic currentCharFound = currentServiceFound.getCharacteristic(mCharsUUIDs[j]);

					if(currentCharFound == null) continue;

					DebugWrapper.infoMsg("Characteristic Found with UUID: '" + currentCharFound.getUuid(), TAG, DebugWrapper.getDebugMessageVisibility());

					if(VSP_CHAR_RX.equals(currentCharFound.getUuid())){
						mCharRx = currentCharFound;

					} else if(VSP_CHAR_TX.equals(currentCharFound.getUuid())){
						mCharTx = currentCharFound;

					} else if(VSP_CHAR_MODEM_OUT.equals(currentCharFound.getUuid())){
						mCharModemOut = currentCharFound;

					} else if(VSP_CHAR_MODEM_IN.equals(currentCharFound.getUuid())){
						mCharModemIn = currentCharFound;

					}
				}
			}
		}

		/*
		 * callback's for the services and characteristics logic
		 */
		if(mServiceVsp == null) {
			DebugWrapper.errorMsgWithSpecialCharacters("VSP service not found, Disconnecting!", TAG, DebugWrapper.getDebugMessageVisibility());
			setToDefault();
			disconnect();
			mVSPDeviceCallback.onVspServiceNotFound(gatt);
		} else{
			DebugWrapper.errorMsgWithSpecialCharacters("VSP service found", TAG, DebugWrapper.getDebugMessageVisibility());
			mIsValidVspDevice = true;
			mVSPDeviceCallback.onVspServiceFound(gatt);
		}

		if(mCharTx == null || mCharRx == null){
			DebugWrapper.errorMsgWithSpecialCharacters("RX and TX characteristics not found, Disconnecting!!", TAG, DebugWrapper.getDebugMessageVisibility());
			setToDefault();
			disconnect();
			mVSPDeviceCallback.onVspRxTxCharsNotFound(gatt);
		} else{
			DebugWrapper.errorMsgWithSpecialCharacters("RX and TX characteristics found", TAG, DebugWrapper.getDebugMessageVisibility());
			setCharacteristicNotificationOrIndication(mCharTx, true);
			mVSPDeviceCallback.onVspRxTxCharsFound(gatt);
		}
	}

	@Override
	protected void onCharacteristicWriteSuccess(
			BluetoothGatt gatt,
			BluetoothGattCharacteristic ch) {
		UUID serviceUUID = ch.getService().getUuid();
		UUID charUUID = ch.getUuid();

		if(VSP_SERVICE.equals(serviceUUID)){

			if(VSP_CHAR_RX.equals(charUUID)){
				DebugWrapper.errorMsg("onCharacteristicWriteSuccess VSP_CHAR_RX", TAG, DebugWrapper.getDebugMessageVisibility());

				// keep count of total bytes send to the remote BLE device
				mTxCounter = mTxCounter + ch.getValue().length;
				mVSPDeviceCallback.onVspSendDataSuccess(gatt, ch);
			}
		}
	}

	@Override
	protected void onCharacteristicWriteFailure(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch, int status) {
		DebugWrapper.errorMsgWithSpecialCharacters("onCharacteristicWriteFailure with status: " + status, TAG, DebugWrapper.getDebugMessageVisibility());
		UUID serviceUUID = ch.getService().getUuid();
		UUID charUUID = ch.getUuid();

		if(VSP_SERVICE.equals(serviceUUID)){

			if(VSP_CHAR_RX.equals(charUUID)){
				DebugWrapper.errorMsg("onCharacteristicWriteFailure VSP_CHAR_RX", TAG, DebugWrapper.getDebugMessageVisibility());

				mVSPDeviceCallback.onVspSendDataFailure(gatt, ch, status);

			}
		}
	}

	@Override
	protected void onCharacteristicChangedSuccess(
			BluetoothGatt gatt,
			BluetoothGattCharacteristic ch) {
		UUID serviceUUID = ch.getService().getUuid();
		UUID charUUID = ch.getUuid();

		if(VSP_SERVICE.equals(serviceUUID)){
			if(VSP_CHAR_TX.equals(charUUID)){
				DebugWrapper.errorMsgWithSpecialCharacters("onCharacteristicChangedSuccess VSP_CHAR_TX: " + ch.getStringValue(0), TAG, DebugWrapper.getDebugMessageVisibility());

				// keep count of total bytes received from the remote BLE device
				mRxCounter = mRxCounter + ch.getValue().length;
				mVSPDeviceCallback.onVspReceiveData(gatt, ch);

			} else if(VSP_CHAR_MODEM_OUT.equals(charUUID)){
				/*
				 * getting the buffer space state and then we use it to identify if there is space in the
				 * remote device or not.
				 * 
				 * when the buffer old state is 0 and the buffer new state is 1 then it means that there
				 * was a transition from the remote device not able to receive data to able to receive data.
				 */
				boolean isBufferSpaceAvailableOldState = mIsBufferSpaceAvailableNewState;

				int isBufferSpaceAvailableNewState = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

				if(isBufferSpaceAvailableNewState == 1){
					mIsBufferSpaceAvailableNewState = true;
				} else{
					mIsBufferSpaceAvailableNewState = false;			
				}

				DebugWrapper.infoMsg("isBufferSpaceAvailableOldState: " + isBufferSpaceAvailableOldState, TAG, DebugWrapper.getDebugMessageVisibility());
				DebugWrapper.infoMsg("isBufferSpaceAvailableNewState: " + mIsBufferSpaceAvailableNewState, TAG, DebugWrapper.getDebugMessageVisibility());

				mVSPDeviceCallback.onVspIsBufferSpaceAvailable(isBufferSpaceAvailableOldState, mIsBufferSpaceAvailableNewState);
			}
		}
	}

	@Override
	protected void onDescriptorWriteSuccess(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor) {
		BluetoothGattCharacteristic ch = descriptor.getCharacteristic();
		UUID serviceUUID = ch.getService().getUuid();
		UUID charUUID = ch.getUuid();

		if(VSP_SERVICE.equals(serviceUUID)){
			if(VSP_CHAR_TX.equals(charUUID)){

				mVSPDeviceCallback.onVspCharTxSucceedToEnableNotifications(gatt, descriptor);

				/*
				 * if found enable modem out notifications to notify us if we can send data to the remote device.
				 * Note: This char is found if the BLE device is in bridge mode
				 */
				if(mCharModemOut != null){
					setCharacteristicNotificationOrIndication(mCharModemOut, true);
				}

			} else if(VSP_CHAR_MODEM_OUT.equals(charUUID)){
				mVSPDeviceCallback.onVspCharModemOutSucceedToEnableNotifications(gatt, descriptor);
			}
		}
	}

	@Override
	protected void onDescriptorWriteFailure(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor, int status) {
		UUID serviceUUID = descriptor.getCharacteristic().getService().getUuid();
		UUID charUUID = descriptor.getCharacteristic().getUuid();

		if(VSP_SERVICE.equals(serviceUUID)){
			if(VSP_CHAR_TX.equals(charUUID)){
				mVSPDeviceCallback.onVspCharTxFailedToEnableNotifications(gatt, descriptor, status);

			} else if(VSP_CHAR_MODEM_OUT.equals(charUUID)){
				mVSPDeviceCallback.onVspCharModemOutFailedToEnableNotifications(gatt, descriptor, status);
			}
		}
	}


	/*
	 * *************************************
	 * private methods
	 * *************************************
	 */
	/**
	 * Clears all values
	 */
	private void setToDefault(){
		mIsValidVspDevice = false;
		mServiceVsp = null;
		mCharTx = null;
		mCharRx = null;
		mCharModemOut = null;
		mCharModemIn = null;
	}
}