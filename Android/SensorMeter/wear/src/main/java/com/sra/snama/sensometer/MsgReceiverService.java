package com.sra.snama.sensometer;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;


public class MsgReceiverService extends WearableListenerService {
    private static final String TAG = "Sensowear/MsgService";
    private DeviceClient deviceClient;
    @Override
    public void onCreate() {
        super.onCreate();
        deviceClient=DeviceClient.getInstance(this);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        Log.d(TAG, "Peer disconnected" + peer.getDisplayName());
        stopService(new Intent(this, SensorService.class));
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.d(TAG, "Peer connected" + peer.getDisplayName());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith("/sample")) {
                    DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                    int s = dataMap.getInt("srate");
                    deviceClient.setSampleRate(s);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "Received message: " + messageEvent.getPath());

        if (messageEvent.getPath().equals(Common.START_MEASUREMENT)) {
            startService(new Intent(MsgReceiverService.this, SensorService.class));
        }

        if (messageEvent.getPath().equals(Common.STOP_MEASUREMENT)) {
            stopService(new Intent(MsgReceiverService.this, SensorService.class));
        }
    }
}
