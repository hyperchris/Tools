package com.sra.snama.sensometer;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by s.nama on 7/8/2015.
 */
public class Common {
    private static boolean DEBUG=false;
    //Log Tags:
    public static final String TAG="Senso.";
    public static final String HOMEService="HomeWatcher";
    public static final String ACCService="Accelerator";
    public static final String BARService="Barometer";
    public static final String GYROService="Gyro";
    public static final String LIGHTService="Light";
    public static final String LINEARService="LinearAcc";
    public static final String MAGService="MagneticField";
    public static final String PROXService="Proximity";
    public static final String STEPService="Step";
    public static final String ROTVECService="RotVec";
    public static final String GAMERVService="GameRV";
    public static final String GRVTYService="Grvty";
    public static final String WIFIService="wifi";


    //Mobile sensor data file name Constants
    public static final String HOME_DATA_FILE = "home.dat";
    public static final String ACC_DATA_FILE = "acc.dat";
    public static final String BAR_DATA_FILE = "bar.dat";
    public static final String GYRO_DATA_FILE = "gyro.dat";
    public static final String LIGHT_DATA_FILE = "light.dat";
    public static final String LINER_DATA_FILE = "lin.dat";
    public static final String MAG_DATA_FILE = "mag.dat";
    public static final String PROX_DATA_FILE = "prox.dat";
    public static final String STEP_DATA_FILE = "step.dat";
    public static final String GRVT_DATA_FILE = "grvt.dat";
    public static final String RV_DATA_FILE = "rv.dat";
    public static final String GMRV_DATA_FILE = "gmrv.dat";
    public static final String WIFI_DATA_FILE = "wifi.dat";

    // Wear sensor data file name constants
    public static final String WACC_DATA_FILE = "wacc.dat";
    public static final String WGYRO_DATA_FILE = "wgyro.dat";
    public static final String WMAG_DATA_FILE = "wmag.dat";

    // Key values
    public static final String SAMPLE="sample";
    public static final String PKey="fpath";
    public static final String MAPPRUNKey="mrun";
    public static final String WAPPRUNKey="wrun";
    public static final String START_MEASUREMENT = "/start";
    public static final String STOP_MEASUREMENT = "/stop";
    public static final String ACCURACY = "accuracy";
    public static final String TIMESTAMP = "timestamp";
    public static final String VALUES = "values";

    //Utility functions
    public static void LOGI(String Tag,String Text)
    {
        if(DEBUG)
        Log.d(Tag,Text);
    }
    private static long getBootTimeinMs()
    {
        long bs=System.currentTimeMillis()-SystemClock.elapsedRealtime();
        return bs;
    }
    public static String GetEventTimeinMs(long nanosec)
    {
        long ns=getBootTimeinMs()+TimeUnit.NANOSECONDS.toMillis(nanosec);
        return Long.toString(ns);
    }
    public static String GetNOWinMs()
    {
        String format=Long.toString(System.currentTimeMillis());
        return format;
    }
    public static String GetNOWinTs()
    {
        SimpleDateFormat s = new SimpleDateFormat("MM dd yyyy hh:mm:ss");
        String format = s.format(new Date());
        return format;
    }
    //
    public synchronized static void CreateSensorDataFiles(String path)
    {
        ArrayList<String> files=new ArrayList<>();
        files.add(Common.HOME_DATA_FILE);
        files.add(Common.ACC_DATA_FILE);
        files.add(Common.BAR_DATA_FILE);
        files.add(Common.GYRO_DATA_FILE);
        files.add(Common.LIGHT_DATA_FILE);
        files.add(Common.LINER_DATA_FILE);
        files.add(Common.MAG_DATA_FILE);
        files.add(Common.PROX_DATA_FILE);
        files.add(Common.STEP_DATA_FILE);
        files.add(Common.GRVT_DATA_FILE);
        files.add(Common.RV_DATA_FILE);
        files.add(Common.GMRV_DATA_FILE);
        files.add(Common.WIFI_DATA_FILE);
        files.add(Common.WACC_DATA_FILE);
        files.add(Common.WGYRO_DATA_FILE);
        files.add(Common.WMAG_DATA_FILE);
        for(String f:files)
        {
            File newfile=new File(path+"/"+f);
            if(!newfile.exists())
                try {
                    newfile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

    //
    public static String SetEnv(Context ctx)
    {
        String root= Environment.getExternalStorageDirectory().getAbsolutePath();
        //String packagename = ctx.getPackageName();
        String packagename = "SensorMeter";
        File appdir=new File(root+"/"+packagename);
        if(!appdir.exists())
            appdir.mkdir();
        return appdir.getAbsolutePath();
    }
    //
    public static void RecordSensorData(String filename,String data)
    {
        if(filename.isEmpty())
            return;
        File file = new File(filename);
        FileOutputStream outstream;
        data=data+"\n";
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            outstream = new FileOutputStream(file,true);
            outstream.write(data.getBytes());
            outstream.close();
        }catch(IOException e){
            e.printStackTrace();
        }

    }
}
