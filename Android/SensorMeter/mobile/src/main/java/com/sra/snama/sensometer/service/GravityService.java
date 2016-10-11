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

public class GravityService extends Service {
	private final String LOG=Common.TAG+Common.GRVTYService;
	SensorManager sm;
	private String recordFile;
	private SensorEventListener listener=new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
				float axisX = event.values[0];
				float axisY = event.values[1];
				float axisZ = event.values[2];
				String data=Common.GetEventTimeinMs(event.timestamp)+" "+ Float.toString(axisX)+","+Float.toString(axisY)+","+Float.toString(axisZ);
				Common.LOGI(LOG,data);
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
		sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_GRAVITY),
				delay);
		recordFile=prefs.getString(Common.PKey, "");
		recordFile=recordFile+"/"+Common.GRVT_DATA_FILE;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		sm.unregisterListener(listener);
		super.onDestroy();
	}


	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

}