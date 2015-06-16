/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.serialdevice;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.lairdtech.bt.ble.vsp.FifoAndVspManager;

public class SerialManager extends FifoAndVspManager{
    private SerialManagerUiCallback mSerialManagerUiCallback;
    

    public SerialManager(Activity activity, SerialManagerUiCallback serialManagerUiCallback){
    	super(activity);
    	if(activity == null || serialManagerUiCallback == null) 
    		throw new NullPointerException("Activity or SerialManagerUiCallback parameter passed is NULL");
    	
        mSerialManagerUiCallback = serialManagerUiCallback;
        
    	SEND_DATA_TO_REMOTE_DEVICE_DELAY = 1;
    }
    
    
    /*
     * *************************************
	 * VirtualSerialPortDeviceCallback
     * *************************************
     */
    @Override
    public void onConnected(BluetoothGatt gatt) {
    	super.onConnected(gatt);
    	mSerialManagerUiCallback.onUiConnected(gatt);
    }
    
    @Override
    public void onDisconnected(
            BluetoothGatt gatt) {
        mSerialManagerUiCallback.onUiDisconnect(gatt);
        super.onDisconnected(gatt);
    }
    
    @Override
    public void onConnectionStateChangeFailure(
    		BluetoothGatt gatt, 
            int status,
            int newState){
    	super.onConnectionStateChangeFailure(gatt, status, newState);
        mSerialManagerUiCallback.onUiConnectionFailure(gatt);
    }
    
    @Override
    public void onVspServiceFound(BluetoothGatt gatt) {
        super.onVspServiceFound(gatt);
    }
    
	@Override
	public void onVspServiceNotFound(BluetoothGatt gatt) {
        super.onVspServiceNotFound(gatt);
		mSerialManagerUiCallback.onUiVspServiceNotFound(gatt);
	}
	
	@Override
	public void onVspRxTxCharsFound(BluetoothGatt gatt) {
        super.onVspRxTxCharsFound(gatt);
		mSerialManagerUiCallback.onUiVspRxTxCharsFound(gatt);
	}
	
	@Override
	public void onVspRxTxCharsNotFound(BluetoothGatt gatt) {
        super.onVspRxTxCharsNotFound(gatt);
		mSerialManagerUiCallback.onUiVspRxTxCharsNotFound(gatt);
	}
	
	@Override
	public void onVspCharTxSucceedToEnableNotifications(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor) {
        super.onVspCharTxSucceedToEnableNotifications(gatt, descriptor);
	}
	
	@Override
	public void onVspCharModemOutSucceedToEnableNotifications(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor) {
        super.onVspCharModemOutSucceedToEnableNotifications(gatt, descriptor);
	}
	
	@Override
	public void onVspCharTxFailedToEnableNotifications(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor, int status) {
        super.onVspCharTxFailedToEnableNotifications(gatt, descriptor, status);
	}
	
	@Override
	public void onVspCharModemOutFailedToEnableNotifications(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor, int status) {
        super.onVspCharModemOutFailedToEnableNotifications(gatt, descriptor, status);
	}
	
	@Override
	public void onVspSendDataSuccess(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch) {
		mSerialManagerUiCallback.onUiSendDataSuccess(ch.getStringValue(0));
		super.onVspSendDataSuccess(gatt, ch);
	}
	
	@Override
	public void onVspSendDataFailure(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch, int status) {
		super.onVspSendDataFailure(gatt, ch, status);
	}
	
	@Override
	public void onVspReceiveData(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch) {
		super.onVspReceiveData(gatt, ch);
		
		mRxBuffer.write(ch.getStringValue(0));
        
        while(mRxBuffer.read(mRxDest) != 0){
        	/*
        	 * found data
        	 */
        	String rxBufferDataRead = mRxDest.toString();
        	mRxDest.delete(0, mRxDest.length());
        	mSerialManagerUiCallback.onUiReceiveData(rxBufferDataRead);
        }
	}
	
	@Override
	public void onVspIsBufferSpaceAvailable(
			boolean isBufferSpaceAvailableOldState,
			boolean isBufferSpaceAvailableNewState) {
		super.onVspIsBufferSpaceAvailable(isBufferSpaceAvailableOldState,
				isBufferSpaceAvailableNewState);
	}
	
    /*
     * *************************************
     * callback's for when transferring data
     * *************************************
     */	
	@Override
	public void uploadNextData(){
		uploadNextDataFromFifoToRemoteDevice();
    }
	
	@Override
	public void onUploaded(){
		super.onUploaded();
		mSerialManagerUiCallback.onUiUploaded();
    }
}