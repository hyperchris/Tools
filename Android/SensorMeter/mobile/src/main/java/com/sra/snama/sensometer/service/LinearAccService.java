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

import static com.sra.snama.sensometer.Common.LINEARService;
import static com.sra.snama.sensometer.Common.LOGI;
import static com.sra.snama.sensometer.Common.TAG;

public class LinearAccService extends Service  {
	private final String LOG= TAG+LINEARService;
	public static double lax=0;
	public static double lay=0;
	public static double laz=0;
	SensorManager sm;
	private String recordFile;

	private SensorEventListener listener=new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			lax = event.values[0];
			lay = event.values[1];
			laz = event.values[2];
			String values=Double.toString(lax)+","+Double.toString(lay)+","+Double.toString(laz);
			String etime=Common.GetEventTimeinMs(event.timestamp);
			String data=etime+" "+values;
			LOGI(LOG,data);
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
		sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
				delay);
		recordFile=prefs.getString(Common.PKey, "");
		recordFile=recordFile+"/"+Common.LINER_DATA_FILE;
	}
	@Override
	public void onDestroy() {
		sm.unregisterListener(listener);
		super.onDestroy();
	}
}
