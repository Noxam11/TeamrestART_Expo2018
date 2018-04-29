package com.example.nutri_000.testinggauge;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class SensorTypeSelectionDialog extends Dialog {

    DetailsActivity callerDetailActivity;
    String deviceAddress;
    /**
     * Android Views
     **/
    TextView tvDevice;
    TextView tvHand;
    TextView tvLowerArm;
    TextView tvUpperArm;
    TextView tvBack;

    public SensorTypeSelectionDialog(@NonNull Context context, DetailsActivity detailsActivity, String deviceAddress) {
        super(context);
        callerDetailActivity = detailsActivity;
        this.deviceAddress = deviceAddress;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_sensor_type_selection_dialog);
        bindViews();
        setDeviceAddress();
        setClickListeners();
    }

    /**
     * this will assign clicks for all types
     */
    private void setClickListeners() {
        tvHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callerDetailActivity.onDeviceTypeClicked(TrustedDevice.TYPE_HAND, deviceAddress);
                dismiss();
            }
        });
        tvLowerArm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callerDetailActivity.onDeviceTypeClicked(TrustedDevice.TYPE_LOWER_ARM, deviceAddress);
                dismiss();
            }
        });
        tvUpperArm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callerDetailActivity.onDeviceTypeClicked(TrustedDevice.TYPE_UPPER_ARM, deviceAddress);
                dismiss();
            }
        });
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callerDetailActivity.onDeviceTypeClicked(TrustedDevice.TYPE_BACK, deviceAddress);
                dismiss();
            }
        });

    }

    /**
     * this function will add address in textview of the pop-up dialog
     */
    private void setDeviceAddress() {
        tvDevice.setText("For device " + deviceAddress);
    }

    /**
     * Binds XML views
     * Call this function after setContentView() in onCreate().
     **/
    private void bindViews() {
        tvDevice = (TextView) findViewById(R.id.tv_device);
        tvHand = (TextView) findViewById(R.id.tv_hand);
        tvLowerArm = (TextView) findViewById(R.id.tv_lower_arm);
        tvUpperArm = (TextView) findViewById(R.id.tv_upper_arm);
        tvBack = (TextView) findViewById(R.id.tv_back);
    }


}
