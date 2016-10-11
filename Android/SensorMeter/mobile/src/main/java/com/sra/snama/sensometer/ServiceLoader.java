package com.sra.snama.sensometer;

import android.content.Context;
import android.content.Intent;

import com.sra.snama.sensometer.service.AcceleratorService;
import com.sra.snama.sensometer.service.BarometerService;
import com.sra.snama.sensometer.service.GameRotationService;
import com.sra.snama.sensometer.service.GravityService;
import com.sra.snama.sensometer.service.GyroService;
import com.sra.snama.sensometer.service.HomeWatcher;
import com.sra.snama.sensometer.service.LightService;
import com.sra.snama.sensometer.service.LinearAccService;
import com.sra.snama.sensometer.service.MagneticFieldService;
import com.sra.snama.sensometer.service.ProximityService;
import com.sra.snama.sensometer.service.RotationVectorService;
import com.sra.snama.sensometer.service.StepService;
import com.sra.snama.sensometer.service.WifiManagerService;
import com.sra.snama.sensometer.wear.RemoteSensorService;

/**
 * Created by s.nama on 7/13/2015.
 */
public class ServiceLoader {
    private final String TAG=Common.TAG+"Loader";
    private Context mContext;
    public ServiceLoader(Context ctx)
    {
        mContext=ctx;
    }
    public void Start()
    {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                mContext.startService(new Intent(mContext, HomeWatcher.class));
                mContext.startService(new Intent(mContext, AcceleratorService.class));
                mContext.startService(new Intent(mContext, GyroService.class));
                mContext.startService(new Intent(mContext, MagneticFieldService.class));
                mContext.startService(new Intent(mContext, LightService.class));
                mContext.startService(new Intent(mContext, BarometerService.class));
                mContext.startService(new Intent(mContext, LinearAccService.class));
                mContext.startService(new Intent(mContext, StepService.class));
                mContext.startService(new Intent(mContext, ProximityService.class));
                mContext.startService(new Intent(mContext, GravityService.class));
                mContext.startService(new Intent(mContext, RotationVectorService.class));
                mContext.startService(new Intent(mContext, GameRotationService.class));
                mContext.startService(new Intent(mContext, WifiManagerService.class));
            }
        };

        runnable.run();

    }

    public void Stop()
    {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                mContext.stopService(new Intent(mContext, HomeWatcher.class));
                mContext.stopService(new Intent(mContext, AcceleratorService.class));
                mContext.stopService(new Intent(mContext, GyroService.class));
                mContext.stopService(new Intent(mContext, BarometerService.class));
                mContext.stopService(new Intent(mContext, LightService.class));
                mContext.stopService(new Intent(mContext, LinearAccService.class));
                mContext.stopService(new Intent(mContext, MagneticFieldService.class));
                mContext.stopService(new Intent(mContext, ProximityService.class));
                mContext.stopService(new Intent(mContext, StepService.class));
                mContext.stopService(new Intent(mContext, GravityService.class));
                mContext.stopService(new Intent(mContext, RotationVectorService.class));
                mContext.stopService(new Intent(mContext, GameRotationService.class));
                mContext.stopService(new Intent(mContext, WifiManagerService.class));
            }
        };

        runnable.run();

    }

}
