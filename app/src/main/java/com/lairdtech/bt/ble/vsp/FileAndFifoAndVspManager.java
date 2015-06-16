/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.bt.ble.vsp;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.net.Uri;

import com.lairdtech.filehandler.FileWrapper;

public abstract class FileAndFifoAndVspManager extends FifoAndVspManager{
	protected static final int MAX_DATA_TO_READ_FROM_TEXT_FILE = 60;
	protected FileWrapper mFileWrapper;
	protected FileState mFileState;


	/*
	 * *************************************
	 * constructor's
	 * *************************************
	 */
	public FileAndFifoAndVspManager(Activity activity) {
		super(activity);

		mFileState = FileState.FILE_NOT_CHOSEN;
	}

	/*
	 * *************************************
	 * getter methods
	 * *************************************
	 */
	public FileState getFileState(){
		return mFileState;
	}

	public FileWrapper getFileWrapper(){
		return mFileWrapper;
	}
	/**
	 * state for if a file is chosen.
	 * 
	 * states: FILE_NOT_CHOSEN, FILE_CHOSEN
	 * 
	 * @author Kyriakos.Alexandrou
	 *
	 */
	public enum FileState {
		FILE_NOT_CHOSEN, FILE_CHOSEN
	}


	/*
	 * *******************
	 * VirtualSerialPortDeviceCallback
	 * *******************
	 */
	@Override
	public void onConnected(BluetoothGatt gatt) {
		super.onConnected(gatt);
	}

	@Override
	public void onDisconnected(BluetoothGatt gatt) {
		super.onDisconnected(gatt);
	}

	@Override
	public void onConnectionStateChangeFailure(BluetoothGatt gatt, int status,
			int newState) {
		super.onConnectionStateChangeFailure(gatt, status, newState);
	}

	@Override
	public void onVspServiceFound(BluetoothGatt gatt) {
		if(mFileState == FileState.FILE_CHOSEN){
			mFifoAndVspManagerState = FifoAndVspManagerState.READY_TO_SEND_DATA;
		}
	}

	@Override
	public void onVspServiceNotFound(BluetoothGatt gatt) {
		super.onVspServiceNotFound(gatt);
	}

	@Override
	public void onVspRxTxCharsFound(BluetoothGatt gatt) {
		super.onVspRxTxCharsFound(gatt);
	}

	@Override
	public void onVspRxTxCharsNotFound(BluetoothGatt gatt) {
		super.onVspRxTxCharsNotFound(gatt);
	}

	@Override
	public void onVspCharTxSucceedToEnableNotifications(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVspCharModemOutSucceedToEnableNotifications(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVspCharTxFailedToEnableNotifications(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor, int status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVspCharModemOutFailedToEnableNotifications(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor, int status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVspSendDataSuccess(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch) {
		super.onVspSendDataSuccess(gatt, ch);
	}

	@Override
	public void onVspSendDataFailure(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch, int status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVspReceiveData(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch) {
		// TODO Auto-generated method stub

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
	public void onUploaded(){
		super.onUploaded();
	}

	@Override
	public void onUploadFailed(final String errorCode){
		super.onUploadFailed(errorCode);
	}


	/*
	 * *************************************
	 * callback's for file operations
	 * *************************************
	 */
	/**
	 * stores the file in a FileWrapper object based on the Uri parameter
	 * and initialises it to its default values
	 * 
	 * @param uri the uri of the file to be initialised
	 */
	public void setFile(Uri uri){
		if(uri == null) return;
		if(mFileWrapper != null){
			mFileWrapper = null;
		}
		mFileWrapper = new FileWrapper(uri, mActivity);

		mFileState = FileState.FILE_CHOSEN;

		if(getVSPDevice().isValidVspDevice() == true){
			mFifoAndVspManagerState = FifoAndVspManagerState.READY_TO_SEND_DATA;
		}
	}

	/**
	 * Send data to remote device
	 * 
	 * @param dataToBeSend the data to send to the remote device
	 */
	public void initialiseFileTransfer(){
        if(getVSPDevice().getBluetoothGatt() == null)  return;
        
        mFifoAndVspManagerState = FifoAndVspManagerState.UPLOADING;
        mFileWrapper.setToDefaultValues();
    }
	
	

	
}
