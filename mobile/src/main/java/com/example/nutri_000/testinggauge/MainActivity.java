package com.example.nutri_000.testinggauge;


//Code adapted from provided TxBDC code entitled TestingGauge provided 09/2017
//Code developed by Senior Design Team 550: restART
//Includes recording functionality for aid in upper limb VNS therapy



import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;


import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    BleService bleService;
    boolean isBound = false;
    boolean fileCreated = false;
    boolean writeDebounce = false;
    String fileName = "trialData_";
    String randomID = "0001_";
    String path = "/storage/emulated/0/";
    String string = "Hello World!";
    String dateTime = DateFormat.getDateTimeInstance().format(new Date());
    String fileType = ".txt";
    /*ArrayList<String> hipData = new ArrayList<String>();
    ArrayList<String>  kneeData= new ArrayList<String>();
    ArrayList<String>  ankleData= new ArrayList<String>();*/
    ArrayList<String> handData = new ArrayList<String>();
    ArrayList<String>  lowerarmData= new ArrayList<String>();
    ArrayList<String>  upperarmData= new ArrayList<String>();
    ArrayList<String>  backData= new ArrayList<String>();
    /*int hipCount = 0;
    int kneeCount = 0;
    int ankleCount = 0;*/
    int handCount = 0;
    int lowerarmCount = 0;
    int upperarmCount = 0;
    int backCount = 0;
    String fullPath = path+fileName+randomID+dateTime+fileType;
    int scanCount = 20;
    /*int footClickCount = 0;
    int kneeClickCount = 0;
    int hipClickCount = 0;*/
    int handClickCount = 0;
    int lowerarmClickCount = 0;
    int upperarmClickCount = 0;
    int backClickCount = 0;
    boolean fireflyFound = false;


    //Arraylist of movements, changes size dynamically as movement is added or removed
    //See "Movement" class
    ArrayList<Movement> movements;

    //int threshold;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleService.BleBinder binder = (BleService.BleBinder) service;
            bleService = binder.getService();
            isBound = true;
            bleService.initializeBle();
            //bleService.scanner.startScan(bleService.mScanCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
            isBound = false;
        }
    };



    private static final String TAG = "Cole";


    Handler timerHandler = new Handler();
    Status statusVariables = new Status();
    FireflyCommands fireflyCommands = new FireflyCommands();

    SensorUI handUI;
    SensorUI lowerarmUI;
    SensorUI upperarmUI;
    SensorUI backUI;

    private static Context context;

    //BLE connections for the firefly
    private FloatingActionButton stimButton;

    //ble connections for the sensor
    private TextView sensorStatus;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!= null){

        }
        else{
            // all UI components for main activity
            setContentView(R.layout.activity_main);

            //retrieve stored movements from sharepreferences
            movements = Helper.getStoredMovementsList(this);

            //Helper.getThreshold would retrieve from sharedpreferences the slider's value
            //For a specific movement, use movements.get(index).error = Helper.getThreshold(this)
            //threshold = Helper.getThreshold(this);

            handUI = new SensorUI(R.id.handButton, R.id.relativeHand, this );
            //handUI.leftPB.setRotation(180);

            handUI.green = R.drawable.handgreen;
            handUI.yellow = R.drawable.handyellow;
            handUI.white = R.drawable.handwhite;


            lowerarmUI = new SensorUI(R.id.lowerarmButton, R.id.relativeLowerArm, this);
            //lowerarmUI.leftPB.setRotation(180);

            lowerarmUI.green = R.drawable.lowerarmgreen;
            lowerarmUI.yellow = R.drawable.lowerarmyellow;
            lowerarmUI.white = R.drawable.lowerarmwhite;


            upperarmUI = new SensorUI(R.id.upperarmButton, R.id.relativeUpperArm, this);
            //upperarmUI.leftPB.setRotation(180);

            upperarmUI.green = R.drawable.upperarmgreen;
            upperarmUI.yellow = R.drawable.upperarmyellow;
            upperarmUI.white = R.drawable.upperarmwhite;


            backUI = new SensorUI(R.id.backButton, R.id.relativeBack, this);
            //backUI.leftPB.setRotation(180);

            backUI.green = R.drawable.backgreen;
            backUI.yellow = R.drawable.backyellow;
            backUI.white = R.drawable.backwhite;

            stimButton = (FloatingActionButton) findViewById(R.id.stim_buton);
            sensorStatus = (TextView) findViewById(R.id.SensorStatus);

            stimButton.bringToFront();
            stimButton.setOnLongClickListener(new View.OnLongClickListener(){
                  @Override
                  public boolean onLongClick(View v){
                      if(bleService.fireflyFound){
                          //bleService.sendMessageForPCM("disconnect pcm");
                          bleService.fireflyGatt.disconnect();
                          bleService.fireflyGatt.close();
                          bleService.fireflyGatt = null;
                          setSensorStatus("PCM Disconnected");
                          stimButton.setImageResource(R.drawable.ic_flash_off_black_24dp);
                          bleService.fireflyFound = false;
                      }
                      return true;
                  }
            });

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_COARSE_LOCATION);
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                Log.v(TAG, "permission granted");
//                writeFile();
            }
            registerReceiver(broadcastReceiver, new IntentFilter("bleService"));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bleService.fireflyGatt != null) {
            bleService.fireflyGatt.disconnect();
            bleService.fireflyGatt.close();
            bleService.fireflyGatt = null;
        }
        if(bleService.handGatt != null) {
            bleService.handGatt.disconnect();
            bleService.handGatt.close();
            bleService.handGatt = null;
        }
        if(bleService.lowerarmGatt != null) {
            bleService.lowerarmGatt.disconnect();
            bleService.lowerarmGatt.close();
            bleService.lowerarmGatt = null;
        }
        if(bleService.upperarmGatt != null) {
            bleService.upperarmGatt.disconnect();
            bleService.upperarmGatt.close();
            bleService.upperarmGatt = null;
        }
        if(bleService.backGatt != null) {
            bleService.backGatt.disconnect();
            bleService.backGatt.close();
            bleService.backGatt = null;
        }
        Log.v("onDestroy", "DESTROYED");
    }
    @Override
    protected void onStop() {

        super.onStop();
//        writeFileAtStop("hand = ", handUI);
//        writeFileAtStop("lowerarm = ", lowerarmUI);
//        writeFileAtStop("upperarm = ", upperarmUI);
//        writeFileAtStop("back = ", backUI);
        Log.v("onStop", "STOPPED");
    }
    @Override
    protected void onPause(){
        super.onPause();

    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        int restore = 42;
        savedInstanceState.putInt("SOMETHING", restore);
    }

    //stim button clicked
    public void stimClicked(View v)
    {
        if(bleService.fireflyGatt != null){
            if(!statusVariables.stimming) {
                statusVariables.stimming = true;
                triggerFirefly(fireflyCommands.startStim);
                timerHandler.postDelayed(fireflyStop, 1000);
                timerHandler.postDelayed(fireflyDebounce,5000);
                //bleService.sendMessageForPCM("stimulate");
//                writeFile();
            }
        }
        else{
           // bleService.sendMessageForPCM("button clicked");
            bleService.searchingHand = false;
            bleService.searchingLowerArm = false;
            bleService.searchingUpperArm = false;
            bleService.searchingBack = false;
            bleService.searchingPCM = true;
            setSensorStatus("Searching for PCM");
            bleService.scanner.startScan(bleService.mScanCallback);
            scanCount = 20;
            bleService.scanning = true;
            timerHandler.postDelayed(scanStop, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION:
            {
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "coarse location permission granted");
                    Intent bleIntent = new Intent(this, BleService.class);
                    startService(bleIntent);
                    bindService(bleIntent, mServiceConnection, this.BIND_AUTO_CREATE);
                }
                else
                {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                    {

                        @Override
                        public void onDismiss(DialogInterface dialog)
                        {
                            //add code to handle dismiss
                        }

                    });
                    builder.show();

                }
                return;
            }
        }
    }

    public static Context getAppContext(){
        return MainActivity.context;
    }
    public void checkValue(final int value, final SensorUI sensor){
        //if (value > sensor.rightSB.getProgress() | (value*-1) > sensor.leftSB.getProgress()){
            if(!statusVariables.stimming) {
                statusVariables.stimming = true;
                Log.v(TAG, "Start command");
                triggerFirefly(fireflyCommands.startStim);
                timerHandler.postDelayed(fireflyStop, 1000);
                timerHandler.postDelayed(fireflyDebounce,5000);
            }
        //}
    }

    //15 total movements are able to be recorded, so initialize arrays of 15
    boolean [] start_movement = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
    int [] count = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    boolean good_movement = false;
    String good_movement_name;

    long time;  //keeps track of realtime
    long lighttime; //keeps track of how long screen has been greenlighted

    float handx, handy, handz;
    long handtime;

    float lowerx, lowery, lowerz;
    long lowertime;

    float upperx, uppery, upperz;
    long uppertime;

    float backx, backy, backz;
    long backtime;

    float handx2;
    float handy2;
    float handz2;
    float lowerx2;
    float lowery2;
    float lowerz2;
    float upperx2;
    float uppery2;
    float upperz2;
    float backx2;
    float backy2;
    float backz2;


    //thresholds have been hardcoded in. Use movements.get(index) to set them specific to each recorded movement
    float handthres = 25;
    float lowerarmthres = 14;
    float upperarmthres = 10;
    float backthres = 5;
    //GAUGE
    public void setGaugeValue(final int valueX, final int valueY, final int valueZ, final SensorUI sensor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {




                android.support.design.widget.CoordinatorLayout backgroundFeedback = (android.support.design.widget.CoordinatorLayout) findViewById(R.id.container);

                RelativeLayout nameFeedback = (RelativeLayout) findViewById(R.id.relativeRightFeedback);

                TextView correctMovement = (TextView) findViewById(R.id.correctMovement);


                //retrieve the values outputting from all connected sensors
                if(recording) {
                    if (sensor == handUI) {
                        /*handData.add(String.valueOf(valueX) + " ");
                        handData.add(String.valueOf(valueY) + " ");
                        handData.add(String.valueOf(valueZ) + " ");
                        //handCount++;
                        handData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //handCount++;*/
                        handx = valueX;
                        handy = valueY;
                        handz = valueZ;
                        handtime = System.currentTimeMillis();
                    }
                    if (sensor == lowerarmUI) {
                        /*lowerarmData.add(String.valueOf(valueX) + " ");
                        lowerarmData.add(String.valueOf(valueY) + " ");
                        lowerarmData.add(String.valueOf(valueZ) + " ");
                        //lowerarmCount++;
                        lowerarmData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //lowerarmCount++;*/
                        lowerx = valueX;
                        lowery = valueY;
                        lowerz = valueZ;
                        lowertime = System.currentTimeMillis();
                    }
                    if (sensor == upperarmUI) {
                        /*upperarmData.add(String.valueOf(valueX) + " ");
                        upperarmData.add(String.valueOf(valueY) + " ");
                        upperarmData.add(String.valueOf(valueZ) + " ");
                        //upperarmCount++;
                        upperarmData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //upperarmCount++;*/
                        upperx = valueX;
                        uppery = valueY;
                        upperz = valueZ;
                        uppertime = System.currentTimeMillis();
                    }
                    if (sensor == backUI) {
                        /*backData.add(String.valueOf(valueX) + " ");
                        backData.add(String.valueOf(valueY) + " ");
                        backData.add(String.valueOf(valueZ) + " ");
                        //backCount++;
                        backData.add(Long.toString(System.currentTimeMillis()) + "\n");
                        //backCount++;*/
                        backx = valueX;
                        backy = valueY;
                        backz = valueZ;
                        backtime = System.currentTimeMillis();
                    }

                    //adds values to arrays
                    //only adds the values outputted if the interval has been 50 milliseconds
                    if (!(System.currentTimeMillis() < (time + 50))) {
                        TempHandx.add(handx);
                        TempHandy.add(handy);
                        TempHandz.add(handz);
                        TempHandTime.add(handtime);

                        TempLowerArmx.add(lowerx);
                        TempLowerArmy.add(lowery);
                        TempLowerArmz.add(lowerz);
                        TempLowerArmTime.add(lowertime);

                        TempUpperArmx.add(upperx);
                        TempUpperArmy.add(uppery);
                        TempUpperArmz.add(upperz);
                        TempUpperArmTime.add(uppertime);

                        TempBackx.add(backx);
                        TempBacky.add(backy);
                        TempBackz.add(backz);
                        TempBackTime.add(backtime);

                        time = System.currentTimeMillis();
                    }
                }
                else {
                    if (sensor == handUI) {
                        handx2 = valueX;
                        handy2 = valueY;
                        handz2 = valueZ;
                    }
                    if (sensor == lowerarmUI) {
                        lowerx2 = valueX;
                        lowery2 = valueY;
                        lowerz2 = valueZ;
                    }
                    if (sensor == upperarmUI) {
                        upperx2 = valueX;
                        uppery2 = valueY;
                        upperz2 = valueZ;
                    }
                    if (sensor == backUI) {
                        backx2 = valueX;
                        backy2 = valueY;
                        backz2 = valueZ;
                    }


                    //begin checking if it is a correct movement
                    //checks all 15 at the same time due to initializing array of 15 items
                    if (!(movements == null)) {
                        for (int i = 0; i < 15; i++) {
                            if (i < movements.size()) {
                                if (!start_movement[i]) {
                                    if (       (((handx2 > (movements.get(i).Handx.get(0) - handthres*2) && handx2 < (movements.get(i).Handx.get(0) + handthres*2))) || ((handx2 > (movements.get(i).Handx.get(0) + 360 - handthres*2) && handx2 < (movements.get(i).Handx.get(0) + 360 + handthres*2))) || ((handx2 > (movements.get(i).Handx.get(0) - 360 - handthres*2) && handx2 < (movements.get(i).Handx.get(0) - 360 + handthres*2))))
                                            && (((handy2 > (movements.get(i).Handy.get(0) - handthres*2) && handy2 < (movements.get(i).Handy.get(0) + handthres*2))) || ((handy2 > (movements.get(i).Handy.get(0) + 360 - handthres*2) && handy2 < (movements.get(i).Handy.get(0) + 360 + handthres*2))) || ((handy2 > (movements.get(i).Handy.get(0) - 360 - handthres*2) && handy2 < (movements.get(i).Handy.get(0) - 360 + handthres*2))))
                                            && (((handz2 > (movements.get(i).Handz.get(0) - handthres*2) && handz2 < (movements.get(i).Handz.get(0) + handthres*2))) || ((handz2 > (movements.get(i).Handz.get(0) + 360 - handthres*2) && handz2 < (movements.get(i).Handz.get(0) + 360 + handthres*2))) || ((handz2 > (movements.get(i).Handz.get(0) - 360 - handthres*2) && handz2 < (movements.get(i).Handz.get(0) - 360 + handthres*2))))
                                            && (((lowerx2 > (movements.get(i).LowerArmx.get(0) - lowerarmthres*2) && lowerx2 < (movements.get(i).LowerArmx.get(0) + lowerarmthres*2))) || ((lowerx2 > (movements.get(i).LowerArmx.get(0) + 360 - lowerarmthres*2) && lowerx2 < (movements.get(i).LowerArmx.get(0) + 360 + lowerarmthres*2))) || ((lowerx2 > (movements.get(i).LowerArmx.get(0) - 360 - lowerarmthres*2) && lowerx2 < (movements.get(i).LowerArmx.get(0) - 360 + lowerarmthres*2))))
                                            && (((lowery2 > (movements.get(i).LowerArmy.get(0) - lowerarmthres*2) && lowery2 < (movements.get(i).LowerArmy.get(0) + lowerarmthres*2))) || ((lowery2 > (movements.get(i).LowerArmy.get(0) + 360 - lowerarmthres*2) && lowery2 < (movements.get(i).LowerArmy.get(0) + 360 + lowerarmthres*2))) || ((lowery2 > (movements.get(i).LowerArmy.get(0) - 360 - lowerarmthres*2) && lowery2 < (movements.get(i).LowerArmy.get(0) - 360 + lowerarmthres*2))))
                                            && (((lowerz2 > (movements.get(i).LowerArmz.get(0) - lowerarmthres*2) && lowerz2 < (movements.get(i).LowerArmz.get(0) + lowerarmthres*2))) || ((lowerz2 > (movements.get(i).LowerArmz.get(0) + 360 - lowerarmthres*2) && lowerz2 < (movements.get(i).LowerArmz.get(0) + 360 + lowerarmthres*2))) || ((lowerz2 > (movements.get(i).LowerArmz.get(0) - 360 - lowerarmthres*2) && lowerz2 < (movements.get(i).LowerArmz.get(0) - 360 + lowerarmthres*2))))
                                            && (((upperx2 > (movements.get(i).UpperArmx.get(0) - upperarmthres*2) && upperx2 < (movements.get(i).UpperArmx.get(0) + upperarmthres*2))) || ((upperx2 > (movements.get(i).UpperArmx.get(0) + 360 - upperarmthres*2) && upperx2 < (movements.get(i).UpperArmx.get(0) + 360 + upperarmthres*2))) || ((upperx2 > (movements.get(i).UpperArmx.get(0) - 360 - upperarmthres*2) && upperx2 < (movements.get(i).UpperArmx.get(0) - 360 + upperarmthres*2))))
                                            && (((uppery2 > (movements.get(i).UpperArmy.get(0) - upperarmthres*2) && uppery2 < (movements.get(i).UpperArmy.get(0) + upperarmthres*2))) || ((uppery2 > (movements.get(i).UpperArmy.get(0) + 360 - upperarmthres*2) && uppery2 < (movements.get(i).UpperArmy.get(0) + 360 + upperarmthres*2))) || ((uppery2 > (movements.get(i).UpperArmy.get(0) - 360 - upperarmthres*2) && uppery2 < (movements.get(i).UpperArmy.get(0) - 360 + upperarmthres*2))))
                                            && (((upperz2 > (movements.get(i).UpperArmz.get(0) - upperarmthres*2) && upperz2 < (movements.get(i).UpperArmz.get(0) + upperarmthres*2))) || ((upperz2 > (movements.get(i).UpperArmz.get(0) + 360 - upperarmthres*2) && upperz2 < (movements.get(i).UpperArmz.get(0) + 360 + upperarmthres*2))) || ((upperz2 > (movements.get(i).UpperArmz.get(0) - 360 - upperarmthres*2) && upperz2 < (movements.get(i).UpperArmz.get(0) - 360 + upperarmthres*2))))
                                            && (((backx2 > (movements.get(i).Backx.get(0) - backthres*2) && backx2 < (movements.get(i).Backx.get(0) + backthres*2))) || ((backx2 > (movements.get(i).Backx.get(0) + 360 - backthres*2) && backx2 < (movements.get(i).Backx.get(0) + 360 + backthres*2))) || ((backx2 > (movements.get(i).Backx.get(0) - 360 - backthres*2) && backx2 < (movements.get(i).Backx.get(0) - 360 + backthres*2))))
                                            && (((backy2 > (movements.get(i).Backy.get(0) - backthres*2) && backy2 < (movements.get(i).Backy.get(0) + backthres*2))) || ((backy2 > (movements.get(i).Backy.get(0) + 360 - backthres*2) && backy2 < (movements.get(i).Backy.get(0) + 360 + backthres*2))) || ((backy2 > (movements.get(i).Backy.get(0) - 360 - backthres*2) && backy2 < (movements.get(i).Backy.get(0) - 360 + backthres*2))))
                                            && (((backz2 > (movements.get(i).Backz.get(0) - backthres*2) && backz2 < (movements.get(i).Backz.get(0) + backthres*2))) || ((backz2 > (movements.get(i).Backz.get(0) + 360 - backthres*2) && backz2 < (movements.get(i).Backz.get(0) + 360 + backthres*2))) || ((backz2 > (movements.get(i).Backz.get(0) - 360 - backthres*2) && backz2 < (movements.get(i).Backz.get(0) - 360 + backthres*2))))) {
                                        time = System.currentTimeMillis();
                                        start_movement[i] = true;
                                    }
                                } // close start movement
                                //moves through the length of the array as long as start_movement is true
                                if (start_movement[i]) {
                                    if ((System.currentTimeMillis() - time) < 600) {
                                        if (count[i] < movements.get(i).Handx.size()) {
                                            if (       (((handx2 > (movements.get(i).Handx.get(count[i]+2) - handthres*2) && handx2 < (movements.get(i).Handx.get(count[i]+2) + handthres*2))) )
                                                    && (((handy2 > (movements.get(i).Handy.get(count[i]+2) - handthres*2) && handy2 < (movements.get(i).Handy.get(count[i]+2) + handthres*2))))
                                                    && (((handz2 > (movements.get(i).Handz.get(count[i]+2) - handthres*2) && handz2 < (movements.get(i).Handz.get(count[i]+2) + handthres*2))))
                                                    && (((lowerx2 > (movements.get(i).LowerArmx.get(count[i]+2) - lowerarmthres*2) && lowerx2 < (movements.get(i).LowerArmx.get(count[i]+2) + lowerarmthres*2))))
                                                    && (((lowery2 > (movements.get(i).LowerArmy.get(count[i]+2) - lowerarmthres*2) && lowery2 < (movements.get(i).LowerArmy.get(count[i]+2) + lowerarmthres*2))))
                                                    && (((lowerz2 > (movements.get(i).LowerArmz.get(count[i]+2) - lowerarmthres*2) && lowerz2 < (movements.get(i).LowerArmz.get(count[i]+2) + lowerarmthres*2))))
                                                    && (((upperx2 > (movements.get(i).UpperArmx.get(count[i]+2) - upperarmthres*2) && upperx2 < (movements.get(i).UpperArmx.get(count[i]+2) + upperarmthres*2))))
                                                    && (((uppery2 > (movements.get(i).UpperArmy.get(count[i]+2) - upperarmthres*2) && uppery2 < (movements.get(i).UpperArmy.get(count[i]+2) + upperarmthres*2))))
                                                    && (((upperz2 > (movements.get(i).UpperArmz.get(count[i]+2) - upperarmthres*2) && upperz2 < (movements.get(i).UpperArmz.get(count[i]+2) + upperarmthres*2))))
                                                    && (((backx2 > (movements.get(i).Backx.get(count[i]+2) - backthres*2) && backx2 < (movements.get(i).Backx.get(count[i]+2) + backthres*2))))
                                                    && (((backy2 > (movements.get(i).Backy.get(count[i]+2) - backthres*2) && backy2 < (movements.get(i).Backy.get(count[i]+2) + backthres*2))))
                                                    && (((backz2 > (movements.get(i).Backz.get(count[i]+2) - backthres*2) && backz2 < (movements.get(i).Backz.get(count[i]+2) + backthres*2))))) {
                                                count[i]++;
                                                time = System.currentTimeMillis();
                                            }
                                        }
                                    } else {
                                        count[i] = 0;
                                        start_movement[i] = false;
                                    }
                                }

                                //if you've reached the end of the arrays, give a greenlight
                                if ((count[i] + 4)*10 > ((movements.get(i).Handx.size()*(10)))) {
                                    //imageFeedback.setBackgroundResource(R.drawable.goodmovement);
                                    good_movement = true;
                                    good_movement_name = movements.get(i).name;
                                    movements.get(i).counter++;


                                    Helper.writeMovements(MainActivity.this,movements);
                                    count[i] = 0;
                                    start_movement[i] = false;

                                    lighttime = System.currentTimeMillis();


                                    backgroundFeedback.setBackgroundResource(R.drawable.backglow);

                                    nameFeedback.setBackgroundResource(R.drawable.movefeedback);

                                    correctMovement.setText(good_movement_name);




                                }




                            }
                        }





                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


                        //would insert VNS stimulation here
                        if (good_movement) {


                            v.vibrate(400);


                            good_movement = false;

                        }





                        //controls how long screen has been lit up
                        if (System.currentTimeMillis() > (lighttime + 2000)) {





                            backgroundFeedback.setBackgroundResource(R.drawable.darkgradient);


                            nameFeedback.setBackgroundResource(R.drawable.nomovementback);


                            correctMovement.setText("");
                        }

                    }
                }

            }
        });
    }


    //SENSOR STATUS TEXT
    public void setSensorStatus(final String message)
    {
        //final String msg = "Sensor " + message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorStatus.setText(message);

            }
        });
    }
    public void triggerFirefly(byte[] onOff)
    {
        if(bleService.fireflyFound){
            bleService.FIREFLY_CHARACTERISTIC2.setValue(onOff);
            boolean b = bleService.fireflyGatt.writeCharacteristic(bleService.FIREFLY_CHARACTERISTIC2);
            Log.i(TAG, "firefly write status = " + b);
        }

    }
    Runnable fireflyStop = new Runnable() {
        @Override
        public void run() {
            if(bleService.fireflyFound){
                Log.v(TAG, "Stop command");
                triggerFirefly(fireflyCommands.stopStim);
            }
        }
    };

    Runnable scanStop = new Runnable() {
        @Override
        public void run() {
            if(bleService.scanning){
                if(scanCount > 0){
                    scanCount--;
                    timerHandler.postDelayed(scanStop, 1000);
                }
                else if(scanCount == 0){
                    bleService.scanner.stopScan(bleService.mScanCallback);
                    bleService.scanning = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSensorStatus("Scan Timeout");
                            if(bleService.handGatt == null){handUI.connect.setBackgroundResource(R.drawable.handwhite);}
                            if(bleService.lowerarmGatt ==  null){lowerarmUI.connect.setBackgroundResource(R.drawable.lowerarmwhite);}
                            if(bleService.upperarmGatt ==  null){upperarmUI.connect.setBackgroundResource(R.drawable.upperarmwhite);}
                            if(bleService.backGatt ==  null){backUI.connect.setBackgroundResource(R.drawable.backwhite);}
                        }
                    });
                }
            }
        }
    };

    Runnable fireflyDebounce = new Runnable(){
        @Override
        public void run(){
            statusVariables.stimming = false;
        }
    };
    Runnable debounceWrite = new Runnable(){
        @Override
        public void run(){
            writeDebounce = false;
        }
    };
    Runnable doubleClick = new Runnable(){
        @Override
        public void run(){
            backClickCount = 0;
            upperarmClickCount = 0;
            lowerarmClickCount = 0;
            handClickCount = 0;
        }
    };

    public void connectHand(View v){
        if(bleService.handGatt == null){
            if(TrustedDevice.getTrustedDeviceAddressForType(this,TrustedDevice.TYPE_HAND).equals(TrustedDevice.ADDRESS_UNASSIGNED)){
                //when no device is registered for this type, ask to add device before connecting
                askToAddTrustedSensor(TrustedDevice.TYPE_HAND);
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSensorStatus("Searching");
                        handUI.connect.setBackgroundResource(R.drawable.handyellow);
                        if (bleService.lowerarmGatt == null) {
                            lowerarmUI.connect.setBackgroundResource(R.drawable.lowerarmwhite);
                        }
                        if (bleService.upperarmGatt == null) {
                            upperarmUI.connect.setBackgroundResource(R.drawable.upperarmwhite);
                        }
                        if (bleService.backGatt == null) {
                            backUI.connect.setBackgroundResource(R.drawable.backwhite);
                        }
                    }
                });
                bleService.searchingHand = true;
                bleService.searchingLowerArm = false;
                bleService.searchingUpperArm = false;
                bleService.searchingBack = false;
                bleService.searchingPCM = false;
                bleService.scanner.startScan(bleService.mScanCallback);
                scanCount = scanCount + 20;
                bleService.scanning = true;
                timerHandler.postDelayed(scanStop, 1000);
            }
        }
        else {
            handClickCount++;
            timerHandler.postDelayed(doubleClick, 500);
            if (handClickCount == 2) {
                bleService.handGatt.disconnect();
                bleService.handGatt.close();
                bleService.handGatt = null;
                setSensorStatus("Sensor disconnected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handUI.connect.setBackgroundResource(R.drawable.handwhite);

                    }
                });
                handClickCount = 0;
            }
        }
    }
    public void connectLowerArm(View v){
        if(bleService.lowerarmGatt ==  null) {
            if(TrustedDevice.getTrustedDeviceAddressForType(this,TrustedDevice.TYPE_LOWER_ARM).equals(TrustedDevice.ADDRESS_UNASSIGNED)){
                //when no device is registered for this type, ask to add device before connecting
                askToAddTrustedSensor(TrustedDevice.TYPE_LOWER_ARM);
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSensorStatus("Searching");
                        lowerarmUI.connect.setBackgroundResource(R.drawable.lowerarmyellow);
                        if (bleService.upperarmGatt == null) {
                            upperarmUI.connect.setBackgroundResource(R.drawable.upperarmwhite);
                        }
                        if (bleService.backGatt == null) {
                            backUI.connect.setBackgroundResource(R.drawable.backwhite);
                        }
                        if (bleService.handGatt == null) {
                            handUI.connect.setBackgroundResource(R.drawable.handwhite);
                        }
                    }
                });
                bleService.searchingLowerArm = true;
                bleService.searchingHand = false;
                bleService.searchingUpperArm = false;
                bleService.searchingBack = false;
                bleService.searchingPCM = false;
                bleService.scanner.startScan(bleService.mScanCallback);
                scanCount = scanCount + 20;
                bleService.scanning = true;
                timerHandler.postDelayed(scanStop, 1000);
            }
        }
        else{
            lowerarmClickCount++;
            timerHandler.postDelayed(doubleClick,500);
            if(lowerarmClickCount == 2){
                bleService.lowerarmGatt.disconnect();
                bleService.lowerarmGatt.close();
                bleService.lowerarmGatt = null;
                setSensorStatus("Sensor disconnected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lowerarmUI.connect.setBackgroundResource(R.drawable.lowerarmwhite);

                    }
                });
                lowerarmClickCount = 0;
            }
        }

    }

    public void connectUpperArm(View v){
        if(bleService.upperarmGatt == null) {
            if(TrustedDevice.getTrustedDeviceAddressForType(this,TrustedDevice.TYPE_UPPER_ARM).equals(TrustedDevice.ADDRESS_UNASSIGNED)){
                //when no device is registered for this type, ask to add device before connecting
                askToAddTrustedSensor(TrustedDevice.TYPE_UPPER_ARM);
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSensorStatus("Searching");
                        upperarmUI.connect.setBackgroundResource(R.drawable.upperarmyellow);
                        if (bleService.backGatt == null) {
                            backUI.connect.setBackgroundResource(R.drawable.backwhite);
                        }
                        if (bleService.handGatt == null) {
                            handUI.connect.setBackgroundResource(R.drawable.handwhite);
                        }
                        if (bleService.lowerarmGatt == null) {
                            lowerarmUI.connect.setBackgroundResource(R.drawable.lowerarmwhite);
                        }
                    }
                });
                bleService.searchingHand = false;
                bleService.searchingLowerArm = false;
                bleService.searchingUpperArm = true;
                bleService.searchingBack = false;
                bleService.searchingPCM = false;
                bleService.scanner.startScan(bleService.mScanCallback);
                scanCount = scanCount + 20;
                bleService.scanning = true;
                timerHandler.postDelayed(scanStop, 1000);
            }
        }
        else{
            upperarmClickCount++;
            timerHandler.postDelayed(doubleClick,500);
            if(upperarmClickCount == 2){
                bleService.upperarmGatt.disconnect();
                bleService.upperarmGatt.close();
                bleService.upperarmGatt = null;
                setSensorStatus("Sensor disconnected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        upperarmUI.connect.setBackgroundResource(R.drawable.upperarmwhite);

                    }
                });
                upperarmClickCount = 0;
            }
        }
    }

    /**
     * this will be called when user click on button to connect the device but no device is registered for that sensor type.
     * @param sensorType
     */
    private void askToAddTrustedSensor(String sensorType) {
        new AlertDialog.Builder(this)
                .setTitle("No device registered for "+sensorType)
                .setMessage("In order to connect the device, it must be added as trusted device.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Add Now", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        startDetails(findViewById(R.id.detailsButton));
                    }})
                .setNegativeButton("Dismiss", null).show();
    }

    public void connectBack(View v){
        if(bleService.backGatt == null) {
            if(TrustedDevice.getTrustedDeviceAddressForType(this,TrustedDevice.TYPE_BACK).equals(TrustedDevice.ADDRESS_UNASSIGNED)){
                //when no device is registered for this type, ask to add device before connecting
                askToAddTrustedSensor(TrustedDevice.TYPE_BACK);
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSensorStatus("Searching");
                        backUI.connect.setBackgroundResource(R.drawable.backyellow);
                        if (bleService.handGatt == null) {
                            handUI.connect.setBackgroundResource(R.drawable.handwhite);
                        }
                        if (bleService.lowerarmGatt == null) {
                            lowerarmUI.connect.setBackgroundResource(R.drawable.lowerarmwhite);
                        }
                        if (bleService.upperarmGatt == null) {
                            upperarmUI.connect.setBackgroundResource(R.drawable.upperarmwhite);
                        }
                    }
                });
                bleService.searchingHand = false;
                bleService.searchingLowerArm = false;
                bleService.searchingUpperArm = false;
                bleService.searchingBack = true;
                bleService.searchingPCM = false;
                bleService.scanner.startScan(bleService.mScanCallback);
                scanCount = scanCount + 20;
                bleService.scanning = true;
                timerHandler.postDelayed(scanStop, 1000);
            }
        }
        else{
            backClickCount++;
            timerHandler.postDelayed(doubleClick,500);
            if(backClickCount == 2){
                bleService.backGatt.disconnect();
                bleService.backGatt.close();
                bleService.backGatt = null;
                setSensorStatus("Sensor disconnected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        backUI.connect.setBackgroundResource(R.drawable.backwhite);

                    }
                });
                backClickCount = 0;
            }
        }
    }

    private BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent","none");
            Log.d(TAG, "onReceive: "+eventType);
            if(eventType.equals("sensorConnected")){
                if(extras.getString("gatt").equals("hand")){
                    connectSensor(handUI);
                    /*handUI.connect.setOnLongClickListener(new View.OnLongClickListener(){
                        @Override
                        public boolean onLongClick(View v){
                            handUI.calibrateSensor(handUI);
                            return true;
                        }
                    });*/
                }
                if(extras.getString("gatt").equals("lowerarm")){
                    connectSensor(lowerarmUI);
                    /*lowerarmUI.connect.setOnLongClickListener(new View.OnLongClickListener(){
                        @Override
                        public boolean onLongClick(View v){
                            lowerarmUI.calibrateSensor(lowerarmUI);
                            return true;
                        }
                    });*/
                }
                if(extras.getString("gatt").equals("upperarm")){
                    connectSensor(upperarmUI);
                    /*upperarmUI.connect.setOnLongClickListener(new View.OnLongClickListener(){
                        @Override
                        public boolean onLongClick(View v){
                            upperarmUI.calibrateSensor(upperarmUI);
                            return true;
                        }
                    });*/
                }
                if(extras.getString("gatt").equals("back")){
                    connectSensor(backUI);
                    /*backUI.connect.setOnLongClickListener(new View.OnLongClickListener(){
                        @Override
                        public boolean onLongClick(View v){
                            backUI.calibrateSensor(backUI);
                            return true;
                        }
                    });*/
                }
                if(extras.getString("gatt").equals("firefly")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stimButton.setVisibility(View.VISIBLE);
                            setSensorStatus("PCM Connected");
                            stimButton.setImageResource(R.drawable.ic_flash_on_24dp);
                        }
                    });
                }
                if (extras.getString("gatt").equals("unknown")) {
                    Log.v(TAG, "unknown gatt");
                }
                Log.v("bleService", "connected message sent");
            }
            if(eventType.equals("sensorDisconnected")){
                if(extras.getString("gatt").equals("hand")){
                    onSensorDisconnected(handUI);
                    if(bleService.handGatt != null){
                        bleService.handGatt.close();
                        bleService.handGatt = null;
                    }
                }
                if(extras.getString("gatt").equals("lowerarm")){
                    onSensorDisconnected(lowerarmUI);
                    if(bleService.lowerarmGatt != null){
                        bleService.lowerarmGatt.close();
                        bleService.lowerarmGatt = null;
                    }
                }
                if(extras.getString("gatt").equals("upperarm")){
                    onSensorDisconnected(upperarmUI);
                    if(bleService.upperarmGatt != null){
                        bleService.upperarmGatt.close();
                        bleService.upperarmGatt = null;
                    }
                }
                if(extras.getString("gatt").equals("back")){
                    onSensorDisconnected(backUI);
                    if(bleService.backGatt != null){
                        bleService.backGatt.close();
                        bleService.backGatt = null;
                    }
                }
                if(extras.getString("gatt").equals("firefly")){
                    setSensorStatus("PCM Disconnected");
                    if(bleService.fireflyGatt != null){
                        bleService.fireflyGatt.close();
                        bleService.fireflyGatt = null;
                        stimButton.setImageResource(R.drawable.ic_flash_off_black_24dp);
                    }
                }
            }
            if(eventType.equals("notification")){
                BleNotification notification = intent.getParcelableExtra("notifyObject");
                Log.d(TAG, "onReceive: notification:"+notification.gatt+", x,y,z::"+notification.valueX+","+notification.valueY+","+notification.valueZ);
                if(notification.gatt.equals("hand")){
                    findGaugeValue(handUI,notification.valueX,notification.valueY,notification.valueZ);
                }
                else if(notification.gatt.equals("lowerarm")){
                    findGaugeValue(lowerarmUI,notification.valueX,notification.valueY,notification.valueZ);
                }
                else if(notification.gatt.equals("upperarm")){
                    findGaugeValue(upperarmUI,notification.valueX,notification.valueY,notification.valueZ);
                }
                else if(notification.gatt.equals("back")){
                    findGaugeValue(backUI,notification.valueX,notification.valueY,notification.valueZ);
                }
            }
            if(eventType.equals("fireflyConnected")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stimButton.setVisibility(View.VISIBLE);
                        setSensorStatus("PCM Connected");
                        stimButton.setImageResource(R.drawable.ic_flash_on_24dp);
                    }
                });
            }
        }
    };
    private void connectSensor(final SensorUI sensor){




        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                setSensorStatus("Sensor Connected");
                sensor.connect.setBackgroundResource(sensor.green);
                v.vibrate(100);
            }
        });
    }
    private void onSensorDisconnected(final SensorUI sensor){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensor.connect.setBackgroundResource(sensor.white);

            }
        });
        setSensorStatus("Sensor Disconnected");
        Log.v("BLUETOOTH", "DISCONNECTED");
    }

    public void startDetails(View v){
        Intent intent = new Intent(this, DetailsActivity.class);
        if (bleService.backGatt != null){
            String backDeviceAddress = bleService.backGatt.getDevice().getAddress().toString();
            intent.putExtra("backDeviceAddress", backDeviceAddress);
        }
        else{
            String string = "not connected";
            intent.putExtra("backDeviceAddress", string);
        }
        if (bleService.upperarmGatt != null){
            String upperarmDeviceAddress = bleService.upperarmGatt.getDevice().getAddress().toString();
            intent.putExtra("upperarmDeviceAddress", upperarmDeviceAddress);
        }
        else{
            String string = "not connected";
            intent.putExtra("upperarmDeviceAddress", string);
        }
        if(bleService.lowerarmGatt != null){
            String lowerarmDeviceAddress = bleService.lowerarmGatt.getDevice().getAddress().toString();
            intent.putExtra("lowerarmDeviceAddress", lowerarmDeviceAddress);
        }
        else{
            String string = "not connected";
            intent.putExtra("lowerarmDeviceAddress", string);
        }
        if(bleService.handGatt != null){
            String handDeviceAddress = bleService.handGatt.getDevice().getAddress().toString();
            intent.putExtra("handDeviceAddress", handDeviceAddress);
        }
        else{
            String string = "not connected";
            intent.putExtra("handDeviceAddress", string);
        }
        startActivity(intent);
    }
    public void startListOfMovements(View v){
        Intent intent = new Intent(this, RecordingActivity.class);

        startActivity(intent);
    }

    public void calibrateDevice(View v) {
        handUI.calibrateSensor(handUI);
        lowerarmUI.calibrateSensor(lowerarmUI);
        upperarmUI.calibrateSensor(upperarmUI);
        backUI.calibrateSensor(backUI);
    }

    //temporary arrays to hold values before they are stored into sharedpreferences
    ArrayList<Float> TempHandx = new ArrayList();
    ArrayList<Float> TempHandy = new ArrayList();
    ArrayList<Float> TempHandz = new ArrayList();
    ArrayList<Long> TempHandTime = new ArrayList();
    ArrayList<Float> TempLowerArmx = new ArrayList<>();
    ArrayList<Float> TempLowerArmy = new ArrayList<>();
    ArrayList<Float> TempLowerArmz = new ArrayList<>();
    ArrayList<Long> TempLowerArmTime = new ArrayList();
    ArrayList<Float> TempUpperArmx = new ArrayList<>();
    ArrayList<Float> TempUpperArmy = new ArrayList<>();
    ArrayList<Float> TempUpperArmz = new ArrayList<>();
    ArrayList<Long> TempUpperArmTime = new ArrayList();
    ArrayList<Float> TempBackx = new ArrayList<>();
    ArrayList<Float> TempBacky = new ArrayList<>();
    ArrayList<Float> TempBackz = new ArrayList<>();
    ArrayList<Long> TempBackTime = new ArrayList();
    boolean recording = false;
    boolean recordingData = false;
    public FloatingActionButton addMovementButton;
    public EditText movementName;
    public EditText notes;

    //when record button is clicked
    public void clickRecording(View v) {
        if (recording) {
            recording = false;
            recordingData = false;
            stopRecording();
        } else {
            recording = true;
            recordingData = true;
            time = System.currentTimeMillis();
            TempHandx = new ArrayList<>();
            TempHandy = new ArrayList<>();
            TempHandz = new ArrayList<>();
            TempHandTime = new ArrayList();
            TempLowerArmx = new ArrayList<>();
            TempLowerArmy = new ArrayList<>();
            TempLowerArmz = new ArrayList<>();
            TempLowerArmTime = new ArrayList();
            TempUpperArmx = new ArrayList<>();
            TempUpperArmy = new ArrayList<>();
            TempUpperArmz = new ArrayList<>();
            TempUpperArmTime = new ArrayList();
            TempBackx = new ArrayList<>();
            TempBacky = new ArrayList<>();
            TempBackz = new ArrayList<>();
            TempBackTime = new ArrayList();
            addMovementButton = (FloatingActionButton) findViewById(R.id.recordButton);
            addMovementButton.setImageResource(R.drawable.stop);
        }
    }

    //enables user to type a custom name and stores all data recorded into Movement class
    public void stopRecording() {
        addMovementButton = (FloatingActionButton) findViewById(R.id.recordButton);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater saveRecordingInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View saveRecordingLayout = saveRecordingInflater.inflate(R.layout.save_recording, null);
        alertDialogBuilder.setView(saveRecordingLayout);
        movementName = (EditText) saveRecordingLayout.findViewById(R.id.filenameET);
        notes = (EditText) saveRecordingLayout.findViewById(R.id.notesET);

        alertDialogBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String path = "/storage/emulated/0/";
                        addMovementButton.setImageResource(R.drawable.record);
                        Movement newMovement = new Movement();
                        newMovement.counter = 0;
                        newMovement.diff = 0;
                        newMovement.name = movementName.getText().toString();
                        newMovement.filename = "TXBDC_" + newMovement.name;
                        newMovement.Handx = TempHandx;
                        newMovement.Handy = TempHandy;
                        newMovement.Handz = TempHandz;
                        newMovement.HandTime = TempHandTime;
                        newMovement.LowerArmx = TempLowerArmx;
                        newMovement.LowerArmy = TempLowerArmy;
                        newMovement.LowerArmz = TempLowerArmz;
                        newMovement.LowerArmTime = TempLowerArmTime;
                        newMovement.UpperArmx = TempUpperArmx;
                        newMovement.UpperArmy = TempUpperArmy;
                        newMovement.UpperArmz = TempUpperArmz;
                        newMovement.UpperArmTime = TempUpperArmTime;
                        newMovement.Backx = TempBackx;
                        newMovement.Backy = TempBacky;
                        newMovement.Backz = TempBackz;
                        newMovement.BackTime = TempBackTime;
                        if (movements == null) {
                            movements = new ArrayList<>();
                        }
                        movements.add(newMovement);

                        //comment this out if you no longer want files to be written
                        writeFile(newMovement, "TXBDC_" + newMovement.name, movementName.getText().toString());
                        //stores new movement in sharedpreferences
                        Helper.writeMovements(MainActivity.this, movements);
                    }
                });
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", null);

        addMovementButton.setImageResource(R.drawable.record);

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }

    public void findGaugeValue(final SensorUI sensor, float gyroX, float gyroY, float gyroZ){


        Log.d(TAG, "findGaugeValue: MainActivity: x"+gyroX+", y:"+gyroY+", z:"+gyroZ);


            setGaugeValue((int) gyroX,(int) gyroY,(int)gyroZ,sensor);


    }
    public void writeFile(Movement newMovement, String fileName, String userEntered) {
        recording = false;
        try {
            fullPath = path+fileName+fileType;
            //fullPathData = path + fileName + "_data" + fileType;
            //FileOutputStream outputStream = new FileOutputStream(fullPath);
            File f = new File(fullPath);
            FileOutputStream outputStream = new FileOutputStream(f); // create a file output stream around f

            newMovement.path = f;
            string = userEntered + "\n";
            outputStream.write(string.getBytes());
            string = "file created on: " + dateTime + "\n" + notes + "\n";
            outputStream.write(string.getBytes());

            string = "Hand \n";
            outputStream.write(string.getBytes());
            for(int i=0; i < newMovement.Handx.size(); i++) {
                string = Float.toString(newMovement.Handx.get(i)) + " " + Float.toString(newMovement.Handy.get(i)) + " " + Float.toString(newMovement.Handz.get(i))+ " " + Long.toString(newMovement.HandTime.get(i))+"\n";
                outputStream.write(string.getBytes());
            }
            string = "LowerArm \n";
            outputStream.write(string.getBytes());
            for(int i=0; i < newMovement.LowerArmx.size(); i++) {
                string = Float.toString(newMovement.LowerArmx.get(i)) + " " + Float.toString(newMovement.LowerArmy.get(i)) + " " + Float.toString(newMovement.LowerArmz.get(i))+ " " + Long.toString(newMovement.LowerArmTime.get(i))+ "\n";
                outputStream.write(string.getBytes());
            }
            string = "UpperArm \n";
            outputStream.write(string.getBytes());
            for(int i=0; i < newMovement.UpperArmx.size(); i++) {
                string = Float.toString(newMovement.UpperArmx.get(i)) + " " + Float.toString(newMovement.UpperArmy.get(i)) + " " + Float.toString(newMovement.UpperArmz.get(i))+ " " + Long.toString(newMovement.UpperArmTime.get(i))+ "\n";
                outputStream.write(string.getBytes());
            }
            string = "Back \n";
            outputStream.write(string.getBytes());
            for(int i=0; i < newMovement.Backx.size(); i++) {
                string = Float.toString(newMovement.Backx.get(i)) + " " + Float.toString(newMovement.Backy.get(i)) + " " + Float.toString(newMovement.Backz.get(i))+ " " + Long.toString(newMovement.BackTime.get(i))+ "\n";
                outputStream.write(string.getBytes());
            }
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        /**
         * this will be called everytime after returning from the Recording activity. This make sure that it movements are always updated.
         */
        movements = Helper.getStoredMovementsList(this);
    }

    /********************************************************************************************************/


}