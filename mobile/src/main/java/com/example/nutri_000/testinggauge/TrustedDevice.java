package com.example.nutri_000.testinggauge;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TrustedDevice {
    static final String sharedPrefFile = "sharedPrafFile";
    static final String DEVICE_LIST_KEY = "trustedDevices";
    static final String TYPE_HAND = "hand";
    static final String TYPE_LOWER_ARM = "lowerArm";
    static final String TYPE_UPPER_ARM = "upperArm";
    static final String TYPE_BACK = "back";
    static final String ADDRESS_UNASSIGNED = "Not assigned";
    String address;
    String assignedType;
    public TrustedDevice(String address, String assignedType) {
        this.address = address;
        this.assignedType = assignedType;
    }

    /**
     * this will return all the trusted devices stored in the local storage
     *
     * @param context
     * @return
     */
    static List<TrustedDevice> getTrustedDevices(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                sharedPrefFile, Context.MODE_PRIVATE);

        //shared pref can store text data for key.
        //read that text data
        String storedDeviceListJSON = prefs.getString(DEVICE_LIST_KEY, "[]");

        //after reading text data, convert it in to list.
        Gson gson = new Gson();
        List<TrustedDevice> trustedDevices = new ArrayList<>();
        Type listType = new TypeToken<List<TrustedDevice>>() {
        }.getType();
        trustedDevices = gson.fromJson(storedDeviceListJSON, listType);


        // sometimes only few devices were stored,
        // following code will fill missing types with address ADDRESS_UNASSIGNED.
        //fill missing devices
        String[] types = new String[]{TYPE_HAND, TYPE_LOWER_ARM, TYPE_UPPER_ARM, TYPE_BACK};
        for (int i = 0; i < types.length; i++) {
            boolean isTypeInList = false;
            for (TrustedDevice device : trustedDevices) {
                if (device.getAssignedType().equals(types[i])) {
                    //means there exist a device with the same type
                    isTypeInList = true;
                    break;
                }
            }

            //add unassigned with type if not already in list
            if (!isTypeInList) {
                TrustedDevice trustedDevice = new TrustedDevice(ADDRESS_UNASSIGNED, types[i]);
                trustedDevices.add(trustedDevice);
            }
        }

        //finally return this list
        return trustedDevices;
    }

    /**
     * this will add new device as trusted device
     * if the device already exist for the same type then it will replace it's address with the new device address
     * @param context
     * @param newDevice
     */
    public static void addAsTrustedDevice(Context context, TrustedDevice newDevice){
        List<TrustedDevice> trustedDevices = getTrustedDevices(context);
        try{

            for (TrustedDevice device : trustedDevices) {
                if(device.getAssignedType().equals(newDevice.getAssignedType())){
                    device.setAddress(newDevice.getAddress());
                    break;
                }
            }

            //once we replace the address, now store it in the local storage as text
            SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(DEVICE_LIST_KEY,new Gson().toJson(trustedDevices)).commit();
        }catch (Exception e){
            Toast.makeText(context, "Could not add device.",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * this will add new device as trusted device
     * if the device already exist for the same type then it will replace it's address with the new device address
     */
    public static void clearTrustedDeviceForSensorType(Context context, String sensorType){
        List<TrustedDevice> trustedDevices = getTrustedDevices(context);
        try{

            //let's set address of target type as ADDRESS_UNASSIGNED
            for (TrustedDevice device : trustedDevices) {
                if(device.getAssignedType().equals(sensorType)){
                    device.setAddress(ADDRESS_UNASSIGNED);
                    break;
                }
            }

            //once we replace the address, now store it in the local storage as text
            SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(DEVICE_LIST_KEY,new Gson().toJson(trustedDevices)).commit();
        }catch (Exception e){
            Toast.makeText(context, "Could not remove device.",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * this will add new device as trusted device
     * if the device already exist for the same type then it will replace it's address with the new device address
     */
    public static String getTrustedDeviceAddressForType(Context context, String sensorType){
        List<TrustedDevice> trustedDevices = getTrustedDevices(context);
        try{

            //let's set address of target type as ADDRESS_UNASSIGNED
            for (TrustedDevice device : trustedDevices) {
                if(device.getAssignedType().equals(sensorType)){
                    return device.getAddress();
                }
            }
        }catch (Exception e){
            Toast.makeText(context, "Could not find device.",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return ADDRESS_UNASSIGNED;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAssignedType() {
        return assignedType;
    }

    public void setAssignedType(String assignedType) {
        this.assignedType = assignedType;
    }
}
