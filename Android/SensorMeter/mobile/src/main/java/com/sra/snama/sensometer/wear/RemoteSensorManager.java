package com.sra.snama.sensometer.wear;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.preference.PreferenceManager;
import android.util.Log;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import com.sra.snama.sensometer.Common;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RemoteSensorManager {
    private static final String TAG = Common.TAG+"RemoteSensor";
    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    private static RemoteSensorManager instance;

    private Context context;
    private ExecutorService executorService;
    private GoogleApiClient googleApiClient;
    private String recordFile;

    public static synchronized RemoteSensorManager getInstance(Context context) {
        if (instance == null) {
            instance = new RemoteSensorManager(context.getApplicationContext());
        }
        return instance;
    }

    private RemoteSensorManager(Context context) {
        this.context = context;
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
        this.executorService = Executors.newCachedThreadPool();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        recordFile=prefs.getString(Common.PKey, "");

    }

    private String ConvertData(long timestamp, float[] values )
    {
        String Data;
        float x=values[0];
        float y=values[1];
        float z=values[2];
        Data=Common.GetEventTimeinMs(timestamp)+" "+Float.toString(x)+","+Float.toString(y)+","+Float.toString(z);
        return  Data;
    }

    public synchronized void addSensorData(int sensorType, int accuracy, long timestamp, float[] values) {

        String fname="",path="";
        String data;
        data=ConvertData(timestamp,values);
        if(sensorType==Sensor.TYPE_ACCELEROMETER)
            fname=Common.WACC_DATA_FILE;
        else if(sensorType==Sensor.TYPE_GYROSCOPE)
            fname=Common.WGYRO_DATA_FILE;
        else if(sensorType==Sensor.TYPE_MAGNETIC_FIELD)
            fname=Common.WMAG_DATA_FILE;
        if(recordFile.isEmpty())
        {
            Log.d(Common.TAG,"Empty Recod path:");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            recordFile=prefs.getString(Common.PKey, "");
        }

        path=recordFile+"/"+fname;
        Log.d(TAG, path + ":" + data);
        Common.RecordSensorData(path, data);
    }

    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }
        ConnectionResult result = googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        return result.isSuccess();
    }

    public void Start() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(Common.START_MEASUREMENT);
            }
        });
    }

    public void Stop() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(Common.STOP_MEASUREMENT);
            }
        });
    }

    public void SendSampleRate(final int samplerate) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                sendsample(samplerate);
            }
        });
    };

    private void sendsample(final int rate)
    {
        Log.d(TAG,"Sending sample rate..");
    if (validateConnection()) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/sample");
        dataMap.getDataMap().putInt("srate", rate);
        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "Send sample rate " + rate + ":" + dataItemResult.getStatus().isSuccess());
            }
        });
    }
    }

    private void controlMeasurementInBackground(final String path) {
        if (validateConnection()) {
            List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
            Log.d(TAG, "Sending to nodes: " + nodes.size());

            for (Node node : nodes) {
                Log.d(TAG, "-> " + node.getDisplayName()+node.isNearby());
                Wearable.MessageApi.sendMessage(
                        googleApiClient, node.getId(), path, null
                ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Log.d(TAG, "controlMeasurementInBackground(" + path + "): " + sendMessageResult.getStatus().isSuccess());
                    }
                });
            }
        } else {
            Log.w(TAG, "No connection possible");
        }
    }
}
