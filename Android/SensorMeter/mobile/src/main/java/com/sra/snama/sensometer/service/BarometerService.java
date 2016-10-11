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

public class BarometerService extends Service{
	private final String LOG=Common.TAG+Common.BARService;
	public static double pressureValue;
	SensorManager sm;
	private String recordFile;
	private SensorEventListener listener=new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			pressureValue =event.values[0];
			String data=Common.GetEventTimeinMs(event.timestamp)+" "+ Double.toString(pressureValue);
			Common.LOGI(LOG, data);
			Common.RecordSensorData(recordFile, data);
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
		sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_PRESSURE),
				delay);
		recordFile=prefs.getString(Common.PKey, "");
		recordFile=recordFile+"/"+Common.BAR_DATA_FILE;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sm.unregisterListener(listener);
	}


	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

}