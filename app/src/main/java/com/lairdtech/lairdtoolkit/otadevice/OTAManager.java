/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.otadevice;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.lairdtech.bt.ble.vsp.FileAndFifoAndVspManager;
import com.lairdtech.misc.DataManipulation;
import com.lairdtech.misc.DebugWrapper;

/**
 * Responsible to read from a text file and send the appropriate length of data to the VSPManager
 */
public class OTAManager extends FileAndFifoAndVspManager{
    final static public String TAG = "OTAManager";

    /*
     * *************************************
     * private variables
     * *************************************
     */
    private CommunicationState mCommunicationState;
    private OTAManagerUiCallback mOTAManagerUiCallback;
    
    
    /*
     * *************************************
     * constructor's
     * *************************************
     */
    public OTAManager(
            Activity activity, OTAManagerUiCallback otaManagerUiCallback)
                    throws NullPointerException {
    	super(activity);
    	
        mOTAManagerUiCallback = otaManagerUiCallback;
        mCommunicationState = CommunicationState.WAITING;
        
    	SEND_DATA_TO_REMOTE_DEVICE_DELAY = 1;
    	MAX_DATA_TO_READ_FROM_BUFFER = 16;

    }
    
    
    /*
     * *************************************
     * private methods
     * *************************************
     */
    private String getDELCommand(){
        if(getVSPDevice().getBluetoothGatt() == null)  return null;
        return "AT+DEL \"" + mFileWrapper.getModuleFileName() + "\"\r";
    }
    
    private String getFOWCommand(){
        if(getVSPDevice().getBluetoothGatt() == null)  return null;
        return "AT+FOW \"" + mFileWrapper.getModuleFileName() + "\"\r";
    }
    
    private String getNextFileContentInHEXString(){
        if(getVSPDevice().getBluetoothGatt() == null)  return null;
        String start = "AT+FWRH \"";
        String end = "\"\r";
        String content = mFileWrapper.readNextHEXStringFromFile(MAX_DATA_TO_READ_FROM_TEXT_FILE);
        String result = null;
        
        if(content != null){
            result = start + content + end;
        }
        return result;
    }
    
    private String getFCLCommand(){
        if(getVSPDevice().getBluetoothGatt() == null)  return null;
        return "AT+FCL\r";
    }
    
    private void closeOpenedFile(){
        flushBuffers();
        writeToFifoAndUploadDataToRemoteDevice("\r" + getFCLCommand());
    }
    
    
    /*
     * *************************************
     * public methods
     * *************************************
     */
    public enum CommunicationState {
        WAITING, DEL, FOW, FWRH, FCL
    }
    
    public void startDataTransfer(){
    	super.initialiseFileTransfer();
        mCommunicationState = CommunicationState.DEL;
        writeToFifoAndUploadDataToRemoteDevice(getDELCommand());
    }
    
    
    public void stopFileUploading(){
    	mFifoAndVspManagerState = FifoAndVspManagerState.STOPPED;
    	mCommunicationState = CommunicationState.WAITING;
    	closeOpenedFile();
    }
    
    
    /*
     * *************************************
	 * VirtualSerialPortDeviceCallback
     * *************************************
     */
    @Override
    public void onConnected(BluetoothGatt gatt) {
    	super.onConnected(gatt);
    	mOTAManagerUiCallback.onUiConnected(gatt);
    }
    
    @Override
    public void onDisconnected(
            BluetoothGatt gatt) {
    	super.onDisconnected(gatt);
        mOTAManagerUiCallback.onUiDisconnect(gatt);
    }
    
	@Override
	public void onConnectionStateChangeFailure(BluetoothGatt gatt, int status,
			int newState) {
    	super.onConnectionStateChangeFailure(gatt, status, newState);
        mOTAManagerUiCallback.onUiConnectionFailure(gatt);
	}

	@Override
	public void onVspServiceFound(BluetoothGatt gatt) {
        super.onVspServiceFound(gatt);
	}
	
	@Override
	public void onVspServiceNotFound(BluetoothGatt gatt) {
        super.onVspServiceNotFound(gatt);
		mOTAManagerUiCallback.onUiVspServiceNotFound(gatt);
	}
	
	@Override
	public void onVspRxTxCharsFound(BluetoothGatt gatt) {
        super.onVspRxTxCharsFound(gatt);
		mOTAManagerUiCallback.onUiVspRxTxCharsFound(gatt);
	}
	
	@Override
	public void onVspRxTxCharsNotFound(BluetoothGatt gatt) {
        super.onVspRxTxCharsNotFound(gatt);
        mOTAManagerUiCallback.onUiVspRxTxCharsNotFound(gatt);
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
		mOTAManagerUiCallback.onUiSendDataSuccess(ch.getStringValue(0));
		super.onVspSendDataSuccess(gatt, ch);
	}
	
	@Override
	public void onVspReceiveData(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch) {
		
		mRxBuffer.write(ch.getStringValue(0));
        
        while(mRxBuffer.read(mRxDest, "\r") != 0){
        	DebugWrapper.errorMsg("onVspReceiveData mRxDest.toString(): " + mRxDest.toString(), TAG, DebugWrapper.getDebugMessageVisibility());
            
            switch(mFifoAndVspManagerState){
            case UPLOADING:
                if(mRxDest.toString().contains("\n00\r")){
                	mRxDest.delete(0, mRxDest.length());
                	
                } else if(mRxDest.toString().contains("\n01\t")){
            		DebugWrapper.infoMsg("errorCode", TAG, DebugWrapper.getDebugMessageVisibility());

                	//error
                    String errorCode = mRxDest.toString();
                    mRxDest.delete(0, mRxDest.length());
                    /*
                     * get only what is between \t and \r. that is the error code
                     */
                    errorCode = DataManipulation.stripStringValue("\t", "\r", errorCode);
                    
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
        switch(mCommunicationState){
        case DEL:
            if(mTxBuffer.getSize() > 0){
                // more data to write
            	uploadNextDataFromFifoToRemoteDevice();

            } else{
                // done deleting the file, start the opening procedure
                mCommunicationState = CommunicationState.FOW;
                writeToFifoAndUploadDataToRemoteDevice(getFOWCommand());
            }
            break;
        case FOW:
            if(mTxBuffer.getSize() > 0){
                // more data to write
            	uploadNextDataFromFifoToRemoteDevice();

            } else{
                // done opening the file for writing, start writing file content
                mCommunicationState = CommunicationState.FWRH;
                writeToFifoAndUploadDataToRemoteDevice(getNextFileContentInHEXString());
            }
            break;            
        case FWRH:
            if(mTxBuffer.getSize() > 0){
                // more data to write
        		DebugWrapper.infoMsg("mTxBuffer.getSize() > 0: " + mTxBuffer.getSize(), TAG, DebugWrapper.getDebugMessageVisibility());

            	uploadNextDataFromFifoToRemoteDevice();
            } else if(mTxBuffer.getSize() <= 0){
        		DebugWrapper.infoMsg("mTxBuffer.getSize() <= 0: " + mTxBuffer.getSize(), TAG, DebugWrapper.getDebugMessageVisibility());

                /*
                 * read more data from the text file
                 */
                final String content = getNextFileContentInHEXString();
                
                if(content != null){
            		DebugWrapper.infoMsg("WRITING NEXT DATA FOUND FROM THE TEXTFILE", TAG, DebugWrapper.getDebugMessageVisibility());

                	writeToFifoAndUploadDataToRemoteDevice(content);
                } else{
                  //done writing text file content, start the closure of the file
                    mCommunicationState = CommunicationState.FCL;
                    writeToFifoAndUploadDataToRemoteDevice(getFCLCommand());
                }
            }
            break;
        case FCL:
            if(mTxBuffer.getSize() > 0){
                // more data to write
            	uploadNextDataFromFifoToRemoteDevice();

            } else{
                /*
                 * file has been closed and file downloading to the module
                 * is finished
                 */
                DebugWrapper.infoMsg("Downloading Finished", TAG, DebugWrapper.getDebugMessageVisibility());
                mCommunicationState = CommunicationState.WAITING;
                onUploaded();
            }
            break;
		default:
			break;
            
        }
    }
    
    @Override
    public void onUploaded(){
    	super.onUploaded();
        mOTAManagerUiCallback.onUiUploaded();
    }
    
    public void resetModule(boolean reset){
    	if(reset == true)
    		startDataTransfer("atz\r");
    }
    
	@Override
	public void onUploadFailed(final String errorCode){
		super.onUploadFailed(errorCode);
        mCommunicationState = CommunicationState.WAITING;
        mOTAManagerUiCallback.onUiReceiveErrorData(errorCode);
    }
}