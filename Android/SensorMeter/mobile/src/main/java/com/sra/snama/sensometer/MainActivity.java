    package com.sra.snama.sensometer;

    import android.app.Activity;
    import android.app.AlertDialog;
    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.SharedPreferences;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.preference.PreferenceManager;
    import android.provider.Settings;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.LinearLayout;
    import android.widget.TextView;

    import com.sra.snama.sensometer.wear.RemoteSensorManager;

    import java.io.File;


    public class MainActivity extends Activity   {
        static final String TAG = Common.TAG+"main";
        EditText delay;
        private Context context;
        private String mUserName,apppath;
        private final String FKey="ftime";
        private ServiceLoader LmSensorManager;
        SharedPreferences prefs;
        SharedPreferences.Editor editor;
        private TextView txStatem;
        private TextView txStatew;
        private RemoteSensorManager RmSensorManager;
        private BroadcastReceiver receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean bl=intent.getBooleanExtra("state",false);
                txStatew.setTextColor(bl?Color.GREEN:Color.RED);
                txStatew.setText(StateText(bl));
            }
        };
        @Override
        protected void onStop() {
            super.onStop();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
           // stopServices(); //Comment to run services even after destroying the app.
        }

        @Override
        protected void onResume() {
            super.onResume();
            IntentFilter inf=new IntentFilter();
            inf.addAction("Wear.SensorService.Start");
            context.registerReceiver(receiver, inf);
            RmSensorManager =RemoteSensorManager.getInstance(context);
        }

        @Override
        protected void onPause() {
            super.onPause();
            context.unregisterReceiver(receiver);

        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Button start=(Button)findViewById(R.id.btnStart);
            Button stop=(Button)findViewById(R.id.btnStop);
            Button delaybtn=(Button)findViewById(R.id.btnDelay);
            delay=(EditText)findViewById(R.id.txtDelay);
            txStatem=(TextView)findViewById(R.id.txtStatem);
            txStatew=(TextView)findViewById(R.id.txtStatew);
            context=getBaseContext();
            LmSensorManager=new ServiceLoader(context);
            prefs=PreferenceManager.getDefaultSharedPreferences(context);
            editor = prefs.edit();
            boolean mrun=prefs.getBoolean(Common.MAPPRUNKey,false);
            boolean wrun=prefs.getBoolean(Common.WAPPRUNKey,false);
            txStatem.setTextColor(mrun?Color.GREEN:Color.RED);
            txStatew.setTextColor(wrun?Color.GREEN:Color.RED);
            txStatem.setText(StateText(mrun));
            txStatew.setText(StateText(wrun));
            boolean ftime=prefs.getBoolean(FKey,true);
            if(ftime) {
                UserInfo();
            }
            else
            {
                apppath=prefs.getString(Common.PKey, "");
                Log.d(TAG,apppath);
                File nm=new File(apppath);
                if(!nm.exists()) {
                    apppath = Common.SetEnv(getApplicationContext());
                    UserInfo();
                }

            }
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startServices();
                    editor.putBoolean(Common.MAPPRUNKey, true);
                    editor.commit();
                    txStatem.setTextColor(Color.GREEN);
                    txStatem.setText(StateText(true));

                }
            });

            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopServices();
                    editor.putBoolean(Common.MAPPRUNKey, false);
                    editor.commit();
                    txStatem.setTextColor(Color.RED);
                    txStatem.setText(StateText(false));
                }
            });
            delaybtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = delay.getText().toString();
                    if (!s.isEmpty()){
                        editor.putInt(Common.SAMPLE, Integer.valueOf(s));
                        editor.commit();
                        RmSensorManager.SendSampleRate(Integer.valueOf(s));
                        delay.setText("");
                }
            }
            });
     }

        private  String  StateText(boolean bl) {
                if (bl)
                    return "RUNNING";
                else
                    return "NOT RUNNING";
        }

        private  void  startServices() {
            //Starting service for Mobile/Device sensors measurement
            LmSensorManager.Start();
            //Starting service for Wear sensors measurement
            RmSensorManager.Start();
      }

        private void stopServices() {
            //Stopping service for Mobile/Device sensors measurement
            LmSensorManager.Stop();
            //Stopping service for Wear sensors measurement
            RmSensorManager.Stop();
        }


        private String getDeviceUniqueID(){
            String device_unique_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            return device_unique_id;
        }


        private synchronized void UserInfo()
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("USER NAME");
            alertDialog.setMessage("Enter your Name");

            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);

            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            mUserName = input.getText().toString();
                            apppath = Common.SetEnv(getApplicationContext());
                            String dir = getDeviceUniqueID();
                            if(!mUserName.isEmpty()){
                                editor.putBoolean(FKey,false);
                                editor.commit();
                                dir=mUserName;
                            }
                            File dataDir=new File(apppath+"/"+dir);
                            if(!dataDir.exists())
                                dataDir.mkdir();
                            String fullp=dataDir.getAbsolutePath();
                            Log.d(TAG,fullp);
                            editor.putString(Common.PKey, fullp);
                            editor.commit();
                            Common.CreateSensorDataFiles(fullp);
                        }
                    });

            alertDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            editor.putBoolean(FKey,false);
                            editor.commit();
                            String dir = getDeviceUniqueID();
                            File dataDir=new File(apppath+"/"+dir);
                            if(!dataDir.exists())
                                dataDir.mkdir();
                            String fullp=dataDir.getAbsolutePath();
                            Log.d(TAG,fullp);
                            editor.putString(Common.PKey, fullp);
                            editor.commit();
                            Common.CreateSensorDataFiles(fullp);
                            dialog.cancel();

                        }
                    });

            alertDialog.show();
        }

    }
