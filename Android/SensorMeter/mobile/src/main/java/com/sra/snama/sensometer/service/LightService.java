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

public class LightService extends Service {
	private final String LOG= Common.TAG+Common.LIGHTService;
	public static float light=0;
	SensorManager sm;
	private String recordFile;

	private SensorEventListener listener=new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			light =event.values[0];
			String data=Common.GetEventTimeinMs(event.timestamp)+" "+ Float.toString(light);
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
		sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_LIGHT),
				delay);
		recordFile=prefs.getString(Common.PKey, "");
		recordFile=recordFile+"/"+Common.LIGHT_DATA_FILE;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sm.unregisterListener(listener);
	}

}

