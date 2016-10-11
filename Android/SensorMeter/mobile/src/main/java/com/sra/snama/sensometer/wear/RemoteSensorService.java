package com.sra.snama.sensometer.wear;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;
import com.sra.snama.sensometer.Common;


import java.util.Arrays;

public class RemoteSensorService extends WearableListenerService {
    private static final String TAG = Common.TAG+"SensorRcvr";

    private RemoteSensorManager sensorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = RemoteSensorManager.getInstance(this);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.i(TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        boolean state=prefs.getBoolean(Common.MAPPRUNKey,false);
        if(state)
            sensorManager.Start();
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        Log.i(TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putBoolean(Common.WAPPRUNKey,false).commit();
        Intent intent=new Intent();
        intent.setAction("Wear.SensorService.Start");
        intent.putExtra("state", false);
        sendBroadcast(intent);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        //Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith("/sensors/")) {
                    unpackSensorData(
                        Integer.parseInt(uri.getLastPathSegment()),
                        DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
                else if(path.startsWith("/service"))
                {
                    DataMap dataMap=DataMapItem.fromDataItem(dataItem).getDataMap();
                    boolean start=dataMap.getBoolean("state");
                    Intent intent=new Intent();
                    intent.setAction("Wear.SensorService.Start");
                    intent.putExtra("state", start);
                    sendBroadcast(intent);
                    Log.d(TAG, path + " " + start);
                    SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
                    pref.edit().putBoolean(Common.WAPPRUNKey,start).commit();

                }
            }
        }
    }

    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy=dataMap.getInt(Common.ACCURACY);
        long timestamp = dataMap.getLong(Common.TIMESTAMP);
        float[] values = dataMap.getFloatArray(Common.VALUES);

      Common.LOGI(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));

        sensorManager.addSensorData(sensorType, accuracy, timestamp, values);
    }
}
