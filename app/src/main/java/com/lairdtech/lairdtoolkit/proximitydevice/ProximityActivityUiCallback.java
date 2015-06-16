/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.proximitydevice;

import com.lairdtech.lairdtoolkit.bases.BaseActivityUiCallback;

public interface ProximityActivityUiCallback extends BaseActivityUiCallback {

	public void onUiReadTxPower(final byte[] txPower);

}
