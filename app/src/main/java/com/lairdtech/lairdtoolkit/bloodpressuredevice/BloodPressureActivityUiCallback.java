/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.bloodpressuredevice;

import com.lairdtech.lairdtoolkit.bases.BaseActivityUiCallback;

public interface BloodPressureActivityUiCallback extends BaseActivityUiCallback {
	
	public void onUIBloodPressureRead(
			final String mValueBloodPressureSystolicResult,
			final String mValueBloodPressureDiastolicResult,
			final String mValueBloodPressureArterialPressureResult);

}
