package com.sra.snama.sensometer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.sra.snama.sensometer.Common;

import java.util.List;

public class WifiManagerService extends Service {
	private final String TAG= Common.TAG+Common.WIFIService;
	private WifiManager mWifiManager;
	private WifiStateReceiver mRecevier;
	private IntentFilter mFilter;
	private String recordFile;
	private Context mContext;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mRecevier != null) {
			mContext.registerReceiver(mRecevier, mFilter);
		}
		return Service.START_NOT_STICKY ;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		recordFile=prefs.getString(Common.PKey, "");
		recordFile=recordFile+"/"+Common.WIFI_DATA_FILE;
		mContext = getBaseContext();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mRecevier != null) {
			mContext.unregisterReceiver(mRecevier);
		}
	}


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public WifiManagerService()
	{

		mRecevier = new WifiStateReceiver();
		mFilter = new IntentFilter();
		mFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

	}

	public class WifiStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action=intent.getAction();
			WifiInfo wi;
			String SSID="";
			String MAC="";
			Common.LOGI(TAG,"Action:"+action);
			if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)||action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
				NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (networkInfo != null) {
					Common.LOGI(TAG, "Type : " + networkInfo.getTypeName() + "State : " + networkInfo.getState());
					if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
						//get the different network states
						if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
							wi = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
							int RSSI = wi.getRssi();
							SSID = wi.getSSID();
							MAC = wi.getMacAddress();
							String data = Common.GetNOWinMs() + " " + SSID + "," + MAC + "," + RSSI;
							Common.LOGI(TAG, data);
							Common.RecordSensorData(recordFile, data);
						}

					}
				}
			}
					else if(action.equals(WifiManager.RSSI_CHANGED_ACTION))
						{
							/*int newRSSI=intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI,0);
							String data=Common.GetNOWinMs()+" "+SSID+","+MAC+","+newRSSI;
							Common.LOGI(TAG,data);
							Common.RecordSensorData(recordFile,data);*/

						}
					else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
						{
							mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
							List<ScanResult> scanresults=mWifiManager.getScanResults();
							Common.LOGI(TAG,"Size:"+scanresults.size());
							for(int i=0;i<scanresults.size();i++)
							{
								String data=Common.GetEventTimeinMs(scanresults.get(i).timestamp)+" "+scanresults.get(i).SSID+","+"UNKNOWN"+","+Integer.toString(scanresults.get(i).level);
								Common.LOGI(TAG,data);
								Common.RecordSensorData(recordFile,data);
							}

						}
				}
			}

}
