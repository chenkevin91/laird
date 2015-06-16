/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.batchdevice;

import android.bluetooth.BluetoothGatt;

import com.lairdtech.bt.ble.vsp.FifoAndVspUiBindingCallback;
import com.lairdtech.lairdtoolkit.bases.BaseActivityUiCallback;

public interface BatchManagerUiCallback extends BaseActivityUiCallback, FifoAndVspUiBindingCallback{

	/*
	 * *************************************
	 * VSP and BLE callback's for the UI
	 * *************************************
	 */
	/**
	 * Callback indicating that the VSP service was not found on the remote BLE device
	 * 
	 * @param gatt GATT client
	 */
	public void onUiVspServiceNotFound(
			final BluetoothGatt gatt);

	/**
	 * called when the VSP Rx and Tx characteristics are found
	 * 
	 * @param gatt
	 */
	public void onUiVspRxTxCharsFound(
			final BluetoothGatt gatt);

	/**
	 * called when the VSP Rx and Tx characteristics are notfound
	 * 
	 * @param gatt
	 */
	public void onUiVspRxTxCharsNotFound(
			final BluetoothGatt gatt);

	/*
	 * *************************************
	 * responses
	 * *************************************
	 */
	/**
	 * Callback that notifies us that the data that was send was written
	 * successful to the remote BLE device
	 * 
	 * @param dataSend the value that was successfully send to the remote BLE device
	 */
	public void onUiSendDataSuccess(
			final String dataSend);

	/**
	 * Callback for when a success response data was sent
	 * from the remote device
	 * 
	 * @param dataReceived the success data that was send from the remote device
	 */
	public void onUiReceiveSuccessData(String dataReceived);

	/**
	 * Callback for when an error response data was sent
	 * from the remote device
	 * 
	 * @param errorCode the error code value that was send from the remote device
	 */
	public void onUiReceiveErrorData(
			final String errorCode);
}
