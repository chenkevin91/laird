/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.bt.ble.vsp;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * interface to be able to receive callback's for when doing operations on a remote BLE device
 * that has the Virtual Serial Port (VSP) service
 * 
 * Use the VirtualSerialPortDevice class to do VSP operations
 * @author Kyriakos.Alexandrou
 *
 */
public interface VirtualSerialPortDeviceCallback{
	/**
	 * Callback indicating when the remote Virtual Serial Port (VSP) device has been connected to/from this device
	 * 
	 * @param gatt GATT client 
	 */
	public void onConnected(
			final BluetoothGatt gatt);
	/**
	 * Callback indicating when the remote VSP device has been disconnected to/from this device
	 * 
	 * @param gatt GATT client 
	 */
	public void onDisconnected(
			final BluetoothGatt gatt);

	/**
	 * Callback indicating that a GATT operation failed while connecting/disconnecting
	 * 
	 * @param gatt GATT client 
	 * @param status error of the failure 
	 * @param newState Returns the new connection state. Can be one of
	 * <a href="http://developer.android.com/reference/android/bluetooth/BluetoothProfile.html#STATE_CONNECTED"> STATE_DISCONNECTED </a>
	 * or <a href="http://developer.android.com/reference/android/bluetooth/BluetoothProfile.html#STATE_DISCONNECTED"> STATE_CONNECTED </a>
	 */
	public void onConnectionStateChangeFailure(
			final BluetoothGatt gatt, 
			final int status,
			final int newState);

	/**
	 * Callback indicating that the VSP service was found on the remote BLE device
	 * 
	 * @param gatt GATT client
	 */
	public void onVspServiceFound(
			final BluetoothGatt gatt);

	/**
	 * Callback indicating that the VSP service was not found on the remote BLE device
	 * 
	 * @param gatt GATT client
	 */
	public void onVspServiceNotFound(
			final BluetoothGatt gatt);

	/**
	 * called when the VSP Rx and Tx characteristics are found
	 * 
	 * @param gatt GATT client
	 */
	public void onVspRxTxCharsFound(
			final BluetoothGatt gatt);

	/**
	 * called when the VSP Rx and Tx characteristics are not found
	 * 
	 * @param gatt
	 */
	public void onVspRxTxCharsNotFound(
			final BluetoothGatt gatt);

	/**
	 * Callback indicating that it enabled notifications for the TX characteristic.
	 * 
	 * This means data send from the remote device will not be received
	 * 
	 * @param gatt GATT client
	 * @param descriptor Descriptor that was written to the associated remote device. 
	 */
	public void onVspCharTxSucceedToEnableNotifications(
			final BluetoothGatt gatt,
			final BluetoothGattDescriptor descriptor);

	/**
	 * Callback indicating that it enabled notifications for the modem out characteristic.
	 * 
	 * This means data send from the remote device will not be received
	 * 
	 * @param gatt GATT client
	 * @param descriptor Descriptor that was written to the associated remote device. 
	 */
	public void onVspCharModemOutSucceedToEnableNotifications(
			final BluetoothGatt gatt,
			final BluetoothGattDescriptor descriptor);

	/**
	 * Callback indicating that it failed to enable notifications for the TX characteristic.
	 * 
	 * This means data send from the remote device will not be received
	 * 
	 * @param gatt GATT client
	 * @param descriptor Descriptor that was written to the associated remote device. 
	 * @param status error of the failure 
	 */
	public void onVspCharTxFailedToEnableNotifications(
			final BluetoothGatt gatt,
			final BluetoothGattDescriptor descriptor,
			final int status);

	/**
	 * Callback indicating that it failed to enable notifications for the modem out characteristic.
	 * 
	 * This means that the remote device will not be able to notify us if it's buffer is full
	 * 
	 * @param gatt GATT client
	 * @param descriptor Descriptor that was written to the associated remote device. 
	 * @param status error of the failure 
	 */
	public void onVspCharModemOutFailedToEnableNotifications(
			final BluetoothGatt gatt,
			final BluetoothGattDescriptor descriptor,
			final int status);

	/**
	 * Callback for whenever data was send to the
	 * remote device successful
	 * 
	 * @param gatt GATT client
	 * @param ch the RX characteristic with the updated value
	 */
	public void onVspSendDataSuccess(
			final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch);

	/**
	 * Callback for whenever data failed to be send to the
	 * remote device
	 * 
	 * @param gatt GATT client
	 * @param ch the RX characteristic with the updated value
	 * @param status error of the failure 
	 */
	public void onVspSendDataFailure(
			final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch,
			final int status);

	/**
	 * Callback for when data is received from the remote device
	 * 
	 * @param gatt GATT client
	 * @param ch the TX characteristic with the updated value
	 */
	public void onVspReceiveData(
			final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch);
	/**
	 * Callback that notifies the android device if the remote device can currently accept data or not
	 * 
	 * if the new state is false it means it cannot receive any more data as its buffer is full,
	 * when true it can receive data
	 * 
	 * @param isBufferSpaceAvailableOldState the old state of the buffer
	 * @param isBufferSpaceAvailableNewState the new state of the buffer
	 */
	public void onVspIsBufferSpaceAvailable(
			final boolean isBufferSpaceAvailableOldState,
			final boolean isBufferSpaceAvailableNewState);
}