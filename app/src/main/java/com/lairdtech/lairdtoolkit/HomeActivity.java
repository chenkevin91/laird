/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.lairdtech.lairdtoolkit.bases.BaseActivity;
import com.lairdtech.lairdtoolkit.batchdevice.BatchActivity;
import com.lairdtech.lairdtoolkit.bloodpressuredevice.BloodPressureActivity;
import com.lairdtech.lairdtoolkit.healththermometerdevice.ThermometerActivity;
import com.lairdtech.lairdtoolkit.heartratedevice.HeartRateActivity;
import com.lairdtech.lairdtoolkit.otadevice.OTAActivity;
import com.lairdtech.lairdtoolkit.proximitydevice.ProximityActivity;
import com.lairdtech.lairdtoolkit.serialdevice.SerialActivity;

public class HomeActivity extends BaseActivity{
	private GridView mAppsHolderGrid;


	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_home);
		super.onCreate(savedInstanceState);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) findViewById(R.id.logoLaird).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		initialiseDialogAbout(getResources().getString(R.string.disclaimer_text));

		final int[] images = {
				R.drawable.icon_blood_pressure,
				R.drawable.icon_heart_rate,
				R.drawable.icon_proximity,
				R.drawable.icon_temp,
				R.drawable.icon_serial,
				R.drawable.icon_ota,
				R.drawable.icon_batch
		};

		final String[] names = {
				getResources().getString(R.string.label_blood),
				getResources().getString(R.string.label_heart),
				getResources().getString(R.string.label_prox),
				getResources().getString(R.string.label_thermometer),
				getResources().getString(R.string.label_serial),
				getResources().getString(R.string.label_ota),
				getResources().getString(R.string.label_batch)
		};


		AppsHolder appsHolderAdapter = new AppsHolder(this, names, images);
		mAppsHolderGrid=(GridView)findViewById(R.id.grid);
		mAppsHolderGrid.setAdapter(appsHolderAdapter);
		mAppsHolderGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent;
				switch (position) {

				case 0:
					isInNewScreen = true;

					intent = new Intent(HomeActivity.this, BloodPressureActivity.class);
					startActivity(intent);
					break;

				case 1:
					isInNewScreen = true;

					intent = new Intent(HomeActivity.this, HeartRateActivity.class);
					startActivity(intent);
					break;

				case 2:
					isInNewScreen = true;

					intent = new Intent(HomeActivity.this, ProximityActivity.class);
					startActivity(intent);
					break;

				case 3:
					isInNewScreen = true;

					intent = new Intent(HomeActivity.this, ThermometerActivity.class);
					startActivity(intent);
					break;

				case 4:
					isInNewScreen = true;

					intent = new Intent(HomeActivity.this, SerialActivity.class);
					startActivity(intent);
					break;
					
				case 5:
					isInNewScreen = true;

					intent = new Intent(HomeActivity.this, OTAActivity.class);
					startActivity(intent);
					break;
					
				case 6:
					isInNewScreen = true;

					intent = new Intent(HomeActivity.this, BatchActivity.class);
					startActivity(intent);
					break;

				}
			}
		});
	} 

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override 
	protected void setListeners() {
		/*
		 *  DO NOT REMOVE THIS METHOD
		 *  WE DO NOT WANT THE SUPER TO BE CALLED AS IT WILL TRY TO FIND THE SCAN
		 *  BUTTON WHICH DOES NOT CURRENTLY EXISTS IN THE HOME CLASS
		 */
	}
}

