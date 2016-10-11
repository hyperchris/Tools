package com.sra.snama.sensometer.service;

    /**
     * Created by s.nama on 7/2/2015.
     */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sra.snama.sensometer.Common;


public class HomeWatcher extends Service {
        private final String TAG= Common.TAG+Common.HOMEService;
            private Context mContext;
            private IntentFilter mFilter;
            private InnerRecevier mRecevier;
            public static int count=0;
            private String recordFile;

            public HomeWatcher()
            {
                mRecevier = new InnerRecevier();
                mFilter = new IntentFilter();
                mFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                mFilter.addAction(Intent.ACTION_SCREEN_OFF);
                mFilter.addAction(Intent.ACTION_SCREEN_ON);
                mFilter.addAction(Intent.ACTION_USER_PRESENT);

            }
            @Override
            public void onCreate() {
                super.onCreate();
                mContext = getBaseContext();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                recordFile=prefs.getString(Common.PKey, "");
                recordFile=recordFile+"/"+Common.HOME_DATA_FILE;
            }

            @Override
            public void onDestroy() {

                stopWatch();
                super.onDestroy();
            }

            @Override
            public int onStartCommand(Intent intent, int flags, int startId) {
                count=0;
                startWatch();
                return Service.START_NOT_STICKY ;
            }


            @Override
            public IBinder onBind(Intent intent) {
                return null;
            }
            public void startWatch() {
                if (mRecevier != null) {
                    mContext.registerReceiver(mRecevier, mFilter);
                }
                count=0;

            }

            public void stopWatch() {
                if (mRecevier != null) {
                    mContext.unregisterReceiver(mRecevier);
                }
            }



            class InnerRecevier extends BroadcastReceiver {
                final String SYSTEM_DIALOG_REASON_KEY = "reason";
                final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
                final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
                final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
                final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
                final String SYSTEM_DIALOG_REASON_LOCK= "lock";

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    String time=Common.GetNOWinMs();

                    if(action.equals(Intent.ACTION_SCREEN_OFF))
                    {
                        Common.LOGI(TAG, "TIME:" + time + " Device in SLEEP");
                        String input=time+" "+ "OFF";
                        Common.RecordSensorData(recordFile, input);
                    }
                    else if(action.equals(Intent.ACTION_SCREEN_ON))
                    {
                        Common.LOGI(TAG, "TIME:" + time + " Device in WAKEUP");
                        String input=time+" "+ "ON";
                        Common.RecordSensorData(recordFile, input);
                    }
                    else if(action.equals(Intent.ACTION_USER_PRESENT))
                    {
                        Common.LOGI(TAG, "TIME:" + time + " User UNLOCKED Device");
                        String input=time+" "+ "UNLOCK";
                        Common.RecordSensorData(recordFile, input);
                    }
                    else if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                        String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                        if (reason != null) {
                            //Log.e(TAG, "TIME:"+time+" ACTION:" + action + " REASON:" + reason);

                                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                                    count++;
                                    Log.e(TAG, "TIME:"+time + " HOME KEY: "+count);
                                    String input=time+" "+ "HOME";
                                    Common.RecordSensorData(recordFile, input);

                                } else if (reason.equals(SYSTEM_DIALOG_REASON_LOCK)) {

                                    Log.e(TAG, "TIME:"+time + " Device LOCKED ");
                                    String input=time+" "+ "LOCKED";
                                    Common.RecordSensorData(recordFile,input);
                            }
                        }
                    }
                }

            }


}
