package com.sra.snama.sensometer.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.sra.snama.sensometer.Common;

import static com.sra.snama.sensometer.Common.ACCService;
import static com.sra.snama.sensometer.Common.LOGI;
import static com.sra.snama.sensometer.Common.TAG;


public class AcceleratorService extends Service {
	private final String LOG= TAG+ACCService;
	public static double ax=0;
	public static double ay=0;
	public static double az=0;
	SensorManager sm;
	private String recordFile;

	//
	private SensorEventListener listener=new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			ax = event.values[0];
			ay = event.values[1];
			az = event.values[2];
			String values=Double.toString(ax)+","+Double.toString(ay)+","+Double.toString(az);
			String etime=Common.GetEventTimeinMs(event.timestamp);
			String data=etime+" "+values;
			LOGI(LOG,data);
			Common.RecordSensorData(recordFile,data);
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int delay = prefs.getInt(Common.SAMPLE, SensorManager.SENSOR_DELAY_UI);
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),delay);
		recordFile=prefs.getString(Common.PKey, "");
		recordFile=recordFile+"/"+Common.ACC_DATA_FILE;
	}

	@Override
	public void onDestroy() {
		sm.unregisterListener(listener);
		super.onDestroy();
	}
}
