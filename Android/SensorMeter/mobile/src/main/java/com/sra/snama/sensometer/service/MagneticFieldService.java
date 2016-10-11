package com.sra.snama.sensometer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sra.snama.sensometer.Common;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.sra.snama.sensometer.Common.LOGI;
import static com.sra.snama.sensometer.Common.MAGService;
import static com.sra.snama.sensometer.Common.TAG;

public class MagneticFieldService extends Service  {
	private final String LOG= TAG+MAGService;
	public static double megx=0;
	public static double megy=0;
	public static double megz=0;
	SensorManager sm;
	private String recordFile;
	private SensorEventListener listener=new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			megx = event.values[0];
			megy = event.values[1];
			megz = event.values[2];
			String values=Double.toString(megx)+","+Double.toString(megy)+","+Double.toString(megz);
			String etime=Common.GetEventTimeinMs(event.timestamp);
			String data=etime+" "+values;
			LOGI(LOG,data);
			Common.RecordSensorData(recordFile,data);		}
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
		sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				delay);
		recordFile=prefs.getString(Common.PKey, "");
		recordFile=recordFile+"/"+Common.MAG_DATA_FILE;
	}
	@Override
	public void onDestroy() {
		sm.unregisterListener(listener);
		super.onDestroy();
	}
}
