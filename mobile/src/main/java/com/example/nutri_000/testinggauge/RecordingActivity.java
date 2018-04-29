package com.example.nutri_000.testinggauge;


import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;



import android.app.AlertDialog;
import android.content.BroadcastReceiver;

import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.view.WindowManager;

import android.widget.EditText;
import android.widget.SeekBar;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

public class RecordingActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "Cole";
    public TextView thresholdTV;
    public SeekBar thresholdSB;
    //record button
    public Button addMovementButton;
    public EditText movementName;
    public EditText notes;
    public TextView movementTV;
    public TextView counterTV;
    BleService bleService;
    int threshold;
    boolean recording = false;
    boolean recordingData = false;
    String name = "trialData_";
    String path = "/storage/emulated/0/";
    String dateTime = DateFormat.getDateTimeInstance().format(new Date());
    String fullPath = path + name + dateTime;
    String fileType = ".txt";
    String string = "Hello World!";

    ArrayList<Movement> movements;
    int error;
    int percent;

    long time;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleService.BleBinder binder = (BleService.BleBinder) service;
            bleService = binder.getService();
//            isBound = true;
            bleService.initializeBle();
            //bleService.scanner.startScan(bleService.mScanCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
//            isBound = false;
        }
    };
    //public void storeData() {
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent");
            if (eventType.equals("notification")) {
                BleNotification notification = intent.getParcelableExtra("notifyObject");
                Log.d(TAG, "onReceive: Notification OnRecordingActivity  gatt:" + notification.gatt + "x:y:z::" + notification.valueX + ":" + notification.valueY + ":" + notification.valueZ);
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {

        } else {
            setContentView(R.layout.activity_recording);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            //retrieves movements arraylist from sharedpreferences
            movements = Helper.getStoredMovementsList(this);

            //sets up the chart displaying all recorded movements
            setupChart();


        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                    Intent bleIntent = new Intent(this, BleService.class);
                    startService(bleIntent);
                    bindService(bleIntent, mServiceConnection, this.BIND_AUTO_CREATE);
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //add code to handle dismiss
                        }

                    });
                    builder.show();

                }
                return;
            }
        }
    }


    //deletes movements and automatically saves reduced movements arraylist into sharedpreferences
    public void deleteMovement(View v) {
        int i = 0;
        switch (v.getId()) {
            case R.id.deletebutton0:
                i = 0;
                break;
            case R.id.deletebutton1:
                i = 1;
                break;
            case R.id.deletebutton2:
                i = 2;
                break;
            case R.id.deletebutton3:
                i = 3;
                break;
            case R.id.deletebutton4:
                i = 4;
                break;
            case R.id.deletebutton5:
                i = 5;
                break;
            case R.id.deletebutton6:
                i = 6;
                break;
            case R.id.deletebutton7:
                i = 7;
                break;
            case R.id.deletebutton8:
                i = 8;
                break;
            case R.id.deletebutton9:
                i = 9;
                break;
            case R.id.deletebutton10:
                i = 10;
                break;
            case R.id.deletebutton11:
                i = 11;
                break;
            case R.id.deletebutton12:
                i = 12;
                break;
            case R.id.deletebutton13:
                i = 13;
                break;
            case R.id.deletebutton14:
                i = 14;
                break;
        }

        String movementID = "movement" + i;
        String movementcounterID = "movementcounter" + i;
        int resID = getResources().getIdentifier(movementID, "id", getPackageName());
        movementTV = ((TextView) findViewById(resID));
        resID = getResources().getIdentifier(movementcounterID, "id", getPackageName());
        counterTV = ((TextView) findViewById(resID));
        movementTV.setText("");
        counterTV.setText("");


        if(!(movements == null)) {
            if (i < movements.size()) {
                final int j = i;
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater delete_movement_dialogInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View delete_movement_dialogLayout = delete_movement_dialogInflater.inflate(R.layout.delete_movement_dialog, null);
                alertDialogBuilder.setView(delete_movement_dialogLayout);
                TextView deleteName = (TextView) delete_movement_dialogLayout.findViewById(R.id.deleteName);
                deleteName.setText(movements.get(i).name);

                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //deleteFile(movements, j);
                                movements.remove(j);
                                Helper.writeMovements(RecordingActivity.this, movements);
                                //can also use Helper.getStoredMovements
                                setupArrayList();
                                //Movement temp = movements.get(i);
                            }
                        });
                    }
                });
                alertDialogBuilder.setNegativeButton("No", null);

                AlertDialog dialog = alertDialogBuilder.create();
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
            setupChart();
        }
    }


    //resets the counter for all recorded movements
    public void resetCounter(View V){
        if (movements.size() != 0) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater delete_movement_dialogInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View delete_movement_dialogLayout = delete_movement_dialogInflater.inflate(R.layout.delete_movement_dialog, null);
            alertDialogBuilder.setView(delete_movement_dialogLayout);
            TextView message = (TextView) delete_movement_dialogLayout.findViewById(R.id.inflaterText);
            message.setText("Are you sure you want to reset the counter for all movements?");

            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < 15; i++) {
                                String movementcounterID = "movementcounter" + i;
                                int resID = getResources().getIdentifier(movementcounterID, "id", getPackageName());
                                counterTV = ((TextView) findViewById(resID));
                                if (movements != null && i < movements.size()) {
                                    movements.get(i).counter = 0;
                                    movements.get(i).diff = 0;
                                    movements.get(i).error = error;
                                    Helper.writeMovements(RecordingActivity.this, movements);
                                    counterTV.setText(Integer.toString(movements.get(i).counter));
                                }
                            }
                        }
                    });
                }
            });
            alertDialogBuilder.setNegativeButton("No", null);

            AlertDialog dialog = alertDialogBuilder.create();
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.show();


        }

    }


    //loads the arraylist of objects from shared preferences
    public void setupArrayList() {
        setContentView(R.layout.activity_recording);


        SharedPreferences share = getSharedPreferences("share preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = share.getString("movements", null);
        Type type = new TypeToken<ArrayList<Movement>>() {
        }.getType();
        movements = gson.fromJson(json, type);
        if (movements == null) {
            movements = new ArrayList<>();
        }
        //movements.clear();

        Helper.writeMovements(this,movements);
        //sets up chart a new everytime a movement is added or removed
        setupChart();
    }

    //sets up the chart a new everytime a movement is added or removed
    public void setupChart() {
        for (int i = 0; i < 15; i++) {
            String movementID = "movement" + i;
            String movementcounterID = "movementcounter" + i;
            int resID = getResources().getIdentifier(movementID, "id", getPackageName());
            movementTV = ((TextView) findViewById(resID));
            resID = getResources().getIdentifier(movementcounterID, "id", getPackageName());
            counterTV = ((TextView) findViewById(resID));
            //HBB20: we need to handle the case when the first time no activity is recorded.
            if (movements == null || i >= movements.size()) {
                movementTV.setText("");
                counterTV.setText("");
            } else {
                Movement temp = movements.get(i);
                movementTV.setText(temp.name);
                counterTV.setText(Integer.toString(temp.counter));
            }
        }
    }

}
