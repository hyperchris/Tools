package com.sra.snama.sensometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.SparseLongArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DeviceClient {
    private static final String TAG = "Sensowear/DeviceClient";
    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    public static DeviceClient instance;

    public static DeviceClient getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceClient(context.getApplicationContext());
        }

        return instance;
    }

    private Context context;
    private GoogleApiClient googleApiClient;
    private ExecutorService executorService;
    private int mSample;



    private DeviceClient(Context context) {
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        executorService = Executors.newCachedThreadPool();
        mSample= SensorManager.SENSOR_DELAY_UI;

    }

    public void sendSensorData(final int sensorType, final int accuracy, final long timestamp, final float[] values) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendSensorDataInBackground(sensorType, accuracy, timestamp, values);
            }
        });
    }
    public void sendStateData(final boolean state) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendStateDataLocal(state);
            }
        });
    }

    public void setSampleRate(final int srate)
    {
        this.mSample=srate;
    }
    public int getSampleRate()
    {
        return this.mSample;
    }

    private void sendStateDataLocal(boolean state) {

        PutDataMapRequest dataMap = PutDataMapRequest.create("/service");
        dataMap.getDataMap().putBoolean("state", state);
        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);
    }


    private void sendSensorDataInBackground(int sensorType, int accuracy, long timestamp, float[] values) {

        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/" + sensorType);
        dataMap.getDataMap().putInt(Common.ACCURACY, accuracy);
        dataMap.getDataMap().putLong(Common.TIMESTAMP, timestamp);
        dataMap.getDataMap().putFloatArray(Common.VALUES, values);
        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);
    }

    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    private void send(PutDataRequest putDataRequest) {
        if (validateConnection()) {
            Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus().isSuccess());
                }
            });
        }
    }
}
