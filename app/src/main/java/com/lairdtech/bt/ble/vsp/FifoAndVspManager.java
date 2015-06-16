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
import android.os.Handler;

import com.lairdtech.misc.DebugWrapper;
import com.lairdtech.misc.FifoQueue;

public abstract class FifoAndVspManager implements VirtualSerialPortDeviceCallback{
	/*
	 * *************************************
	 * private variables
	 * *************************************
	 */
	private static final String TAG = "FifoAndVspManager";
	private VirtualSerialPortDevice mVSPDevice;


	/*
	 * *************************************
	 * protected variables
	 * *************************************
	 */
	protected Activity mActivity;
	protected FifoAndVspManagerState mFifoAndVspManagerState;
	/**
	 * this should be no more than 20 as the Laird module can only receive a total of 20 bytes on every sent
	 */
	protected static int MAX_DATA_TO_READ_FROM_BUFFER = 15;
	/**
	 * Sending data to module
	 */
	protected FifoQueue mTxBuffer;
	/**
	 * Receiving data from module
	 */
	protected FifoQueue mRxBuffer;
	protected Handler sendDataHandler = new Handler();
	protected int SEND_DATA_TO_REMOTE_DEVICE_DELAY = 10;
	/**
	 * this is used to get the previously read data from the
	 * TX buffer and store it temporary into this variable
	 */
	protected StringBuilder mTxDest = new StringBuilder();
	/**
	 * this is used to get the previously read data from the
	 * RX buffer and store it temporary into this variable
	 */
	protected StringBuilder mRxDest = new StringBuilder();


	/*
	 * *************************************
	 * constructor's
	 * *************************************
	 */
	public FifoAndVspManager(Activity activity) {
		mRxBuffer = new FifoQueue();
		mTxBuffer = new FifoQueue();

		mActivity = activity;
		mVSPDevice = new VirtualSerialPortDevice(activity, this);
		mFifoAndVspManagerState = FifoAndVspManagerState.WAITING;
	}


	/*
	 * *************************************
	 * getter methods
	 * *************************************
	 */
	public FifoAndVspManagerState getFifoAndVspManagerState(){
		return mFifoAndVspManagerState;
	}
	/**
	 * getting the VSP device object that is responsible for the communication with the
	 * remote device
	 * @return
	 */
	public VirtualSerialPortDevice getVSPDevice(){
		return mVSPDevice;
	}

	public FifoQueue getRxBuffer(){
		return mRxBuffer;
	}

	public FifoQueue getTxBuffer(){
		return mTxBuffer;
	}


	/*
	 * *************************************
	 * public methods
	 * *************************************
	 */
	public enum FifoAndVspManagerState {
		WAITING, READY_TO_SEND_DATA, UPLOADING,
		UPLOADED, STOPPED, FAILED
	}
	
	/**
	 * Send data to remote device
	 * 
	 * @param dataToBeSend the data to send to the remote device
	 */
	public void startDataTransfer(String dataToBeSend){
		mFifoAndVspManagerState = FifoAndVspManagerState.UPLOADING;
		writeToFifoAndUploadDataToRemoteDevice(dataToBeSend);
	}

	/**
	 * reads from the RX buffer content based on the MAX_DATA_TO_READ_FROM_BUFFER
	 * and sends it to the remote device
	 */
	protected void uploadNextDataFromFifoToRemoteDevice(){
		if(mVSPDevice.getBluetoothGatt() == null ||
				mVSPDevice.isConnected() == false) {
			return;
		}

		if(mTxBuffer.read(mTxDest, MAX_DATA_TO_READ_FROM_BUFFER) != 0){
			String dataToWriteToRemoteBleDevice = mTxDest.toString();
			mTxDest.delete(0, MAX_DATA_TO_READ_FROM_BUFFER);
			DebugWrapper.infoMsgWithSpecialCharacters("uploadNextDataFromFifoToRemoteDevice: " + dataToWriteToRemoteBleDevice, TAG, DebugWrapper.getDebugMessageVisibility());

			mVSPDevice.sendToModule(dataToWriteToRemoteBleDevice);
		} else{
			onUploaded(); 		
		}
	}

	/**
	 * writes the string data passed to the TX buffer and then sends the data to the
	 * remote device
	 * 
	 * @param data the data to write to the TX buffer and to send to the remote device
	 */
	protected void writeToFifoAndUploadDataToRemoteDevice(String data){
		mTxBuffer.write(data);
		uploadNextDataFromFifoToRemoteDevice();
	};
	

	/*
	 * *******************
	 * VirtualSerialPortDeviceCallback
	 * *******************
	 */
	@Override
	public void onConnected(BluetoothGatt gatt) {
		mFifoAndVspManagerState = FifoAndVspManagerState.WAITING;
	}

	@Override
	public void onDisconnected(BluetoothGatt gatt) {
		mFifoAndVspManagerState = FifoAndVspManagerState.WAITING;
		flushBuffers();
	}

	@Override
	public void onConnectionStateChangeFailure(BluetoothGatt gatt, int status,
			int newState) {
		mFifoAndVspManagerState = FifoAndVspManagerState.WAITING;
		flushBuffers();
	}

	@Override
	public void onVspServiceFound(BluetoothGatt gatt) {
		mFifoAndVspManagerState = FifoAndVspManagerState.READY_TO_SEND_DATA;
	}

	@Override
	public void onVspServiceNotFound(BluetoothGatt gatt) {
		DebugWrapper.toastMsg(mActivity, "VSP service was not found, disconnecting...");
		getVSPDevice().disconnect();
	}

	@Override
	public void onVspRxTxCharsFound(BluetoothGatt gatt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVspRxTxCharsNotFound(BluetoothGatt gatt) {
		// TODO Auto-generated method stub

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

		sendDataHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				switch(mFifoAndVspManagerState){
				case UPLOADING:
					/*
					 * what to do after the data was send successfully
					 */
					if(getVSPDevice().isBufferSpaceAvailable() == true){
						uploadNextData();
					}
					break;

				default:
					break;

				} 
			}
		}, SEND_DATA_TO_REMOTE_DEVICE_DELAY);
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

		switch(mFifoAndVspManagerState){
		case UPLOADING:
			/*
			 * callback for what to do when data was send successfully from the android device
			 * and when the module buffer was full and now it has been cleared,
			 * which means it now has available space
			 */
			if(isBufferSpaceAvailableOldState == false
			&& isBufferSpaceAvailableNewState == true
					){
				uploadNextData();
			}
			break;

		default:
			break;

		}
	}


	/*
	 * *************************************
	 * callback's for when transferring data
	 * *************************************
	 */
	/**
	 * override this method to define what to do whenever data is send to the remote
	 * device (usually it's used to send more data to the remote device).
	 */
	protected void uploadNextData(){}

	protected void onUploaded(){
		mFifoAndVspManagerState = FifoAndVspManagerState.UPLOADED;
		flushBuffers();
	}

	/**
	 * used when sending data fails because of a response error from the remote device and not because of a disconnection issue.
	 * For example if the memory of the module has become full it will give a response error that it cannot store any more data
	 */
	protected void onUploadFailed(final String errorCode){
		mFifoAndVspManagerState = FifoAndVspManagerState.FAILED;
		flushBuffers();
	}


	/*
	 * *************************************
	 * protected methods
	 * *************************************
	 */
	/**
	 * clears the RX buffer and the TX buffer
	 */
	protected void flushBuffers(){
		mRxBuffer.flush();
		mTxBuffer.flush();
	};
}
