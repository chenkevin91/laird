/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.batchdevice;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.net.Uri;

import com.lairdtech.bt.ble.vsp.FileAndFifoAndVspManager;
import com.lairdtech.misc.DataManipulation;
import com.lairdtech.misc.DebugWrapper;

/**
 * Responsible to read from a textfile and to send data to the module.
 */
public class BatchManager extends FileAndFifoAndVspManager{
	final static private String TAG = "BatchManager";

	/*
	 * *************************************
	 * private variables
	 * *************************************
	 */
	private BatchManagerUiCallback mBatchManagerUiCallback;


	/*
	 * *************************************
	 * constructor
	 * *************************************
	 */
	public BatchManager(
			Activity activity, BatchManagerUiCallback batchManagerUiCallback)
					throws NullPointerException {
		super(activity);

		mBatchManagerUiCallback = batchManagerUiCallback;
	}
	

	/*
	 * *************************************
	 * public methods
	 * *************************************
	 */
	public void startFileTransfer(){
        if(getVSPDevice().getBluetoothGatt() == null)  return;
        
        super.initialiseFileTransfer();
        writeToFifoAndUploadDataToRemoteDevice(getNextFileContentUntilSpecificChar("\r"));
    }
	
	/**
	 * change this manager from UPLOADING state to STOPPED state
	 * @return true if changed to WAITING state or false if no change was made
	 */
	public void stopFileUploading(){
		mFifoAndVspManagerState = FifoAndVspManagerState.STOPPED;
	}
	
	
	/*
	 * *************************************
	 * private methods
	 * *************************************
	 */	
	/**
	 * returns the next available command from the textfile that was set using
	 * the {@link #setFile(Uri uri)}
	 * @param readUntil the string data to search for
	 * @return returns the data from index 0 until the index of the readUntil string
	 */
	private String getNextFileContentUntilSpecificChar(String readUntil){
		if(getVSPDevice().getBluetoothGatt() == null)  return null;
		String content = mFileWrapper.readUntilASpecificChar(readUntil);
		return content;
	}
	

	/*
	 * *************************************
	 * VirtualSerialPortDeviceCallback
	 * *************************************
	 */
	@Override
	public void onConnected(BluetoothGatt gatt) {
		super.onConnected(gatt);
		mBatchManagerUiCallback.onUiConnected(gatt);
	}

	@Override
	public void onDisconnected(
			BluetoothGatt gatt) {
		super.onDisconnected(gatt);
		mBatchManagerUiCallback.onUiDisconnect(gatt);
	}

	@Override
	public void onConnectionStateChangeFailure(BluetoothGatt gatt, int status,
			int newState) {
		super.onConnectionStateChangeFailure(gatt, status, newState);
		mBatchManagerUiCallback.onUiConnectionFailure(gatt);

	}

	@Override
	public void onVspServiceFound(BluetoothGatt gatt) {
		super.onVspServiceFound(gatt);
	}

	@Override
	public void onVspServiceNotFound(BluetoothGatt gatt) {
		super.onVspServiceNotFound(gatt);
		mBatchManagerUiCallback.onUiVspServiceNotFound(gatt);
	}

	@Override
	public void onVspRxTxCharsFound(BluetoothGatt gatt) {
		super.onVspRxTxCharsFound(gatt);
		mBatchManagerUiCallback.onUiVspRxTxCharsFound(gatt);
	}


	@Override
	public void onVspRxTxCharsNotFound(BluetoothGatt gatt) {
		super.onVspRxTxCharsNotFound(gatt);
		mBatchManagerUiCallback.onUiVspRxTxCharsNotFound(gatt);
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
	public void onVspSendDataSuccess(BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch) {
		// no need to call the super method here as we only want to send the next
		// data to the remote device after we receive a response from the remote device
		mBatchManagerUiCallback.onUiSendDataSuccess(ch.getStringValue(0));
	}

	@Override
	public void onVspReceiveData(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch) {

		mRxBuffer.write(ch.getStringValue(0));

		while(mRxBuffer.read(mRxDest, "\r") != 0){

			/*
			 * found data we want
			 */
			switch(mFifoAndVspManagerState){
			case UPLOADING:
				if(mRxDest.toString().contains("\n00\r")){

					mBatchManagerUiCallback.onUiReceiveSuccessData(mRxDest.toString());
					mRxDest.delete(0, mRxDest.length());

					if(getVSPDevice().isBufferSpaceAvailable() == true){
						uploadNextData();
					}

				} else if(mRxDest.toString().contains("\n01\t")){
					//error
					String errorCode = mRxDest.toString();
					/*
					 * get only what is between \t and \r. that is the error code
					 */
					errorCode = DataManipulation.stripStringValue("\t", "\r", errorCode);

					mRxDest.delete(0, mRxDest.length());

					DebugWrapper.infoMsgWithSpecialCharacters("onVspReceiveData before sending to onError: " + errorCode, TAG, DebugWrapper.getDebugMessageVisibility());

					onUploadFailed(errorCode);
				}
				break;

			default:
				break;
			}
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
	protected void uploadNextData(){
		if(mTxBuffer.getSize() > 0){
			// more data to write
			uploadNextDataFromFifoToRemoteDevice();
		} else if(mTxBuffer.getSize() <= 0){
			/*
			 * read more data from the data file
			 */
			final String content = getNextFileContentUntilSpecificChar("\r");

			if(content == null){
				onUploaded();
			}
			else if(mFileWrapper.getIsEOF() == true && !(content.contains("\r"))){
				/*
				 * if the last values of the file do not contain a "\r" the we add a "\r"
				 * so that we can receive a response from the module
				 */
				writeToFifoAndUploadDataToRemoteDevice(content + "\r");
			}
			else{
				writeToFifoAndUploadDataToRemoteDevice(content);
			}
		}
	}

	@Override
	public void onUploaded(){
		super.onUploaded();
		mBatchManagerUiCallback.onUiUploaded();
	}

	@Override
	public void onUploadFailed(final String errorCode){
		super.onUploadFailed(errorCode);
		mBatchManagerUiCallback.onUiReceiveErrorData(errorCode);
	}
}