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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.List;

import static com.example.nutri_000.testinggauge.TrustedDevice.TYPE_BACK;
import static com.example.nutri_000.testinggauge.TrustedDevice.TYPE_HAND;
import static com.example.nutri_000.testinggauge.TrustedDevice.TYPE_LOWER_ARM;
import static com.example.nutri_000.testinggauge.TrustedDevice.TYPE_UPPER_ARM;
import static com.example.nutri_000.testinggauge.TrustedDevice.getTrustedDevices;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "DetailsActivity";
    // TODO: READ ME: you can change this setting. This decides if one device can be used for two types.
    static boolean allowOneDeviceForTwoTypes = false;
    BleService bleService;
    android.os.Handler timerHandler = new android.os.Handler();
    boolean isBound = false;
    Button scanButton;
    TextView approvedDevice1, approvedDevice2, approvedDevice3, approvedDevice4;
    String newApprovedDevice;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    Runnable scanStop = new Runnable() {
        @Override
        public void run() {
            if (bleService.scanning) {
                bleService.scanner.stopScan(bleService.mScanCallback);
                bleService.scanning = false;
            }
            bleService.searchingFromDetails = false;
            List<String> input = new ArrayList<>();
            for (int i = 0; i < bleService.shockclockCount; i++) {
                input.add(bleService.deviceIDs[i]);
            }// define an adapter
            mAdapter = new RecyclerAdapter(input, DetailsActivity.this);
            recyclerView.setAdapter(mAdapter);
        }
    };

    //a demo change
    private RecyclerView.LayoutManager mLayoutManager;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleService.BleBinder binder = (BleService.BleBinder) service;
            bleService = binder.getService();
            isBound = true;
//            approvedDevice1.setText(bleService.approvedDevices[0]);
//            approvedDevice2.setText(bleService.approvedDevices[1]);
//            approvedDevice3.setText(bleService.approvedDevices[2]);
//            approvedDevice4.setText(bleService.approvedDevices[3]);
            //bleService.initializeBle();
            /*bleService.searchingFromDetails = true;
            bleService.scanner.startScan(bleService.mScanCallback);
            bleService.scanning = true;
            timerHandler.postDelayed(scanStop, 5000);*/

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        scanButton = (Button) findViewById(R.id.scanButton);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        Intent bleIntent = new Intent(this, BleService.class);
        bindService(bleIntent, mServiceConnection, this.BIND_AUTO_CREATE);
        approvedDevice1 = (TextView) findViewById(R.id.device1);
        approvedDevice2 = (TextView) findViewById(R.id.device2);
        approvedDevice3 = (TextView) findViewById(R.id.device3);
        approvedDevice4 = (TextView) findViewById(R.id.device4);
        // use this setting to
        // improve performance if you know that changes
        // in content do not change the layout size
        // of the RecyclerView
        //recyclerView.setHasFixedSize(false);
        // use a linear layout manager


        Intent intent = getIntent();
        Bundle deviceAddresses = intent.getExtras();
        String handDevice = deviceAddresses.getString("handDeviceAddress");
        TextView handAddress = (TextView) findViewById(R.id.handAddress);
        String lowerarmDevice = deviceAddresses.getString("lowerarmDeviceAddress");
        TextView lowerarmAddress = (TextView) findViewById(R.id.lowerarmAddress);
        String upperarmDevice = deviceAddresses.getString("upperarmDeviceAddress");
        TextView upperarmAddress = (TextView) findViewById(R.id.upperarmAddress);
        String backDevice = deviceAddresses.getString("backDeviceAddress");
        TextView backAddress = (TextView) findViewById(R.id.backAddress);
        handDevice = "hand: " + handDevice;
        lowerarmDevice = "lowerarm: " + lowerarmDevice;
        upperarmDevice = "upperarm: " + upperarmDevice;
        backDevice = "back: " + backDevice;
        handAddress.setText(handDevice);
        lowerarmAddress.setText(lowerarmDevice);
        upperarmAddress.setText(upperarmDevice);
        backAddress.setText(backDevice);
//        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("newDevice"));
        String defaultValue = "000000";
        refreshTrustedDeviceList();
    }

    /**
     * this function will refresh trusted devices on screen
     */
    private void refreshTrustedDeviceList() {
        List<TrustedDevice> trustedDevices = getTrustedDevices(this);
        for (TrustedDevice trustedDevice : trustedDevices) {
            switch (trustedDevice.getAssignedType()) {
                case TYPE_HAND:
                    approvedDevice1.setText("Hand - " + trustedDevice.getAddress());
                    break;
                case TYPE_LOWER_ARM:
                    approvedDevice2.setText("Lower Arm - " + trustedDevice.getAddress());
                    break;
                case TYPE_UPPER_ARM:
                    approvedDevice3.setText("Upper Arm - " + trustedDevice.getAddress());
                    break;
                case TYPE_BACK:
                    approvedDevice4.setText("Back - " + trustedDevice.getAddress());
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        TODO: READ ME from HBB20: this function was saving the new setting at the end of the activity. But now we are saving new setting after every change.
//        bleService.detailsStopped();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.v("recycler click", String.valueOf(item));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void scanClicked(View v) {
        if (isBound) {
            Log.v("details", "service bound");
            if (bleService.scanning) {
                bleService.scanner.stopScan(bleService.mScanCallback);
                bleService.scanning = false;
                bleService.searchingFromDetails = false;
            }
            if (bleService.scanning != true) {
                Log.v("details", "starting scan");
                bleService.searchingFromDetails = true;
                bleService.scanner.startScan(bleService.mScanCallback);
                bleService.scanning = true;
                timerHandler.postDelayed(scanStop, 5000);
                for (int i = 0; i > bleService.shockclockCount; i++) {
                    bleService.deviceIDs[i] = null;
                }
                bleService.shockclockCount = 0;
            }
        }
    }
//    TODO: READ ME  by HBB20: we are handling click and device assignment with different and efficient way. So we do not need this anymore.

//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Bundle extras = intent.getExtras();
//            newApprovedDevice = extras.getString("deviceAddress");
//            Log.v("new device", "Address: " + newApprovedDevice);
//            //setNewApprovedDevice(newApprovedDevice);
//        }
//    };

//    public void setNewApprovedDevice(final String newDevice) {
//        Log.d(TAG, "setNewApprovedDevice: Did you click?");
//
//        if (approvedDevice1.getText().toString().equals("1")) {
//            approvedDevice1.setText(newDevice);
//        } else if (approvedDevice2.getText().toString().equals("2")) {
//            approvedDevice2.setText(newDevice);
//        } else if (approvedDevice3.getText().toString().equals("3")) {
//            approvedDevice3.setText(newDevice);
//        } else if (approvedDevice4.getText().toString().equals("4")) {
//            approvedDevice4.setText(newDevice);
//        } else {
//            String dev2 = approvedDevice1.getText().toString();
//            String dev3 = approvedDevice2.getText().toString();
//            String dev4 = approvedDevice3.getText().toString();
//            approvedDevice2.setText(dev2);
//            approvedDevice3.setText(dev3);
//            approvedDevice4.setText(dev4);
//            approvedDevice1.setText(newDevice);
//        }
//        bleService.approvedDevices[0] = approvedDevice1.getText().toString();
//        bleService.approvedDevices[1] = approvedDevice2.getText().toString();
//        bleService.approvedDevices[2] = approvedDevice3.getText().toString();
//        bleService.approvedDevices[3] = approvedDevice4.getText().toString();
//    }

    public void onClick(View v) {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater PCMInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View PCMLayout = PCMInflater.inflate(R.layout.pcmlayout, null);
        builder.setView(PCMLayout);
        //
        DiscreteSeekBar ds = (DiscreteSeekBar) PCMLayout.findViewById(R.id.PCMDurationSeekbar);
        ds.setMin(0);
        ds.setMax(30);
        ds.setProgress(12);
        final TextView CurrentPCMValue = (TextView) PCMLayout.findViewById(R.id.PCMDurationDisplay);
        CurrentPCMValue.setTypeface(CurrentPCMValue.getTypeface(), Typeface.BOLD);
        CurrentPCMValue.setText(ds.getProgress() + " Seconds");
        ds.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 1;
            }
        });
        ds.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                CurrentPCMValue.setTypeface(CurrentPCMValue.getTypeface(), Typeface.BOLD);
                CurrentPCMValue.setText(value + " Seconds");
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
        //
        DiscreteSeekBar pulseWidthSeekbar = (DiscreteSeekBar) PCMLayout.findViewById(R.id.PCMPulseWidthSeekbar);
        pulseWidthSeekbar.setMin(1);
        pulseWidthSeekbar.setMax(10);
        pulseWidthSeekbar.setProgress(4);
        final TextView currentPCMPulseWidth = (TextView) PCMLayout.findViewById(R.id.PCMPulseWidthDisplay);
        currentPCMPulseWidth.setTypeface(currentPCMPulseWidth.getTypeface(), Typeface.BOLD);
        currentPCMPulseWidth.setText((pulseWidthSeekbar.getProgress() * 25) + " μs");
        pulseWidthSeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 25;
            }
        });
        pulseWidthSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                currentPCMPulseWidth.setTypeface(currentPCMPulseWidth.getTypeface(), Typeface.BOLD);
                currentPCMPulseWidth.setText((value * 25) + " μs");
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
        //
        DiscreteSeekBar amplitudeSeekbar = (DiscreteSeekBar) PCMLayout.findViewById(R.id.PCMAmplitudeSeekbar);
        amplitudeSeekbar.setMin(0);
        amplitudeSeekbar.setMax(16);
        amplitudeSeekbar.setProgress(5);
        final TextView amplitudeDisplay = (TextView) PCMLayout.findViewById(R.id.PCMAmplitudeDisplay);
        amplitudeDisplay.setTypeface(amplitudeDisplay.getTypeface(), Typeface.BOLD);
        amplitudeDisplay.setText((amplitudeSeekbar.getProgress() / 10.0) + " mA");
        amplitudeSeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 10;
            }
        });

        amplitudeSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                String format = String.valueOf((double) seekBar.getProgress() / 10);
                seekBar.setIndicatorFormatter(format);
                amplitudeDisplay.setText(format + " mA");
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                String format = String.valueOf((double) seekBar.getProgress() / 10);
                seekBar.setIndicatorFormatter(format);
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                String format = String.valueOf((double) seekBar.getProgress() / 10);
                seekBar.setIndicatorFormatter(format);
            }
        });
        //
        DiscreteSeekBar PCMFrequencySeekbar = (DiscreteSeekBar) PCMLayout.findViewById(R.id.PCMFrequencySeekbar);
        PCMFrequencySeekbar.setMin(0);
        PCMFrequencySeekbar.setMax(20);
        PCMFrequencySeekbar.setProgress(11);
        final TextView PCMFreqDisplay = (TextView) PCMLayout.findViewById(R.id.PCMFrequencyDisplay);
        PCMFreqDisplay.setTypeface(PCMFreqDisplay.getTypeface(), Typeface.BOLD);
        PCMFreqDisplay.setText((PCMFrequencySeekbar.getProgress() * 5) + " Hz");
        PCMFrequencySeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 5;
            }
        });
        PCMFrequencySeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                PCMFreqDisplay.setText((value * 5) + " Hz");
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });


        builder.setTitle("PCM Settings: ");

        // add OK and Cancel buttons
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        android.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * this function will be called when ever the device from "Available Devices" will be clicked.
     * we need to prompt type selection box for this device.
     *
     * @param deviceAddress
     */
    public void onDeviceClicked(String deviceAddress) {
        Log.d(TAG, "onDeviceClicked: " + deviceAddress);
        SensorTypeSelectionDialog sensorTypeSelectionDialog = new SensorTypeSelectionDialog(this, this, deviceAddress);
        sensorTypeSelectionDialog.show();
    }

    /**
     * this will be called when the type is selected from the pop-up dialog.
     *
     * @param selectedType
     * @param newDeviceAddress
     */
    public void onDeviceTypeClicked(String selectedType, String newDeviceAddress) {
        Toast.makeText(this, "This should set " + newDeviceAddress + " as " + selectedType + ".", Toast.LENGTH_SHORT).show();

        //if one device is not allowed for assignment of 2 types, we need to check if device address already exist for any other type.
        // if any, it must be removed
        if (!allowOneDeviceForTwoTypes) {
            List<TrustedDevice> trustedDevices = TrustedDevice.getTrustedDevices(this);
            for (TrustedDevice trustedDevice : trustedDevices) {
                if (trustedDevice.getAddress().equals(newDeviceAddress)) {
                    TrustedDevice.clearTrustedDeviceForSensorType(this, trustedDevice.getAssignedType());
                }
            }
        }

        //then add this new device
        TrustedDevice.addAsTrustedDevice(this, new TrustedDevice(newDeviceAddress, selectedType));

        //finally refresh the list on screen to display the latest list
        refreshTrustedDeviceList();

    }


}
