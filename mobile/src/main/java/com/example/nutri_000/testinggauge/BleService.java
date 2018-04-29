package com.example.nutri_000.testinggauge;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static com.example.nutri_000.testinggauge.MainActivity.getAppContext;

public class BleService extends Service {
    private BluetoothAdapter adapter;
    public BluetoothLeScanner scanner;
    public boolean searchingHand, searchingLowerArm, searchingUpperArm, searchingBack = false;
    public boolean searchingPCM = true;
    BluetoothGatt handGatt, lowerarmGatt, upperarmGatt, backGatt, fireflyGatt;
    private int connected = 2;
    private int connecting = 1;
    private int disconnected = 0;
    public boolean searchingFromDetails = false;
    public boolean scanning = true;
    String TAG = "bleService";
// TODO: Read me from HBB20: now we handle trusted devices in advanced way. Now we don't need this array
//    String[] approvedDevices = new String[4];
    private IBinder bleBinder = new BleBinder();
    Intent intent;
    public String[] deviceIDs = new String[30];
    public int[] deviceRSSIs = new int[30];
    public int shockclockCount = 0;
    //BluetoothDevice sensor;
    private BluetoothGattCharacteristic NRF_CHARACTERISTIC;
    public BluetoothGattCharacteristic FIREFLY_CHARACTERISTIC2;
    boolean fireflyFound = false;
    SharedPreferences sharedPreferences;
    //final Messenger mMessenger = new Messenger(new IncomingHandler());
    //Messenger pcmMessenger = null;
    boolean isBound;
    Context context;
    public class BleBinder extends Binder {
        BleService getService(){
            return BleService.this;
        }
    }
    public IBinder onBind(Intent intent){
        return bleBinder;
    }
    @Override
    public void onCreate(){
        intent = new Intent(TAG);
        context = this;
        // make a new intent to bind to a remote service
        Intent intentPCM = new Intent("com.txbdc.backgroundpcm.PCMService");
        intentPCM.setPackage("com.txbdc.backgroundpcm");
        intentPCM.putExtra("remote", "remote");
        bindService(intentPCM, myConnection, Context.BIND_AUTO_CREATE);

        //set up saved devices for future connections
        // TODO: READ ME from HBB20: now we handle trusted devices in advanced way. Now we don't need this
//        sharedPreferences = this.getSharedPreferences("savedDevices", Context.MODE_PRIVATE);
//        approvedDevices[0] = sharedPreferences.getString("device1","000000");
//        approvedDevices[1] = sharedPreferences.getString("device2","000000");
//        approvedDevices[2] = sharedPreferences.getString("device3","000000");
//        approvedDevices[3] = sharedPreferences.getString("device4","000000");

    }

    public int onStartCommand(Intent intent, int flags, int startId){
        return Service.START_NOT_STICKY;
    }
    /*class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String bleEvent = data.getString("bleEvent");
            if(bleEvent != null){
                if(bleEvent.equals("fireflyConnected")){
                    Log.v(TAG,"two way");
                    fireflyFound = true;
                    intent.putExtra("bleEvent", "fireflyConnected");
                    sendBroadcast(intent);
                }
            }
        }
    }*/

    // new service connection (the service connection for this service is in MainActivity)
    private ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            //send a message to the remote service when it connects
            //pcmMessenger = new Messenger(service);
            isBound = true;

            //sendMessageForPCM("service connected");
        }

        public void onServiceDisconnected(ComponentName className) {
            //pcmMessenger = null;
            isBound = false;
        }
    };

    public ScanCallback mScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            Log.d(TAG, "onScanResult");

            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            Log.d(TAG, "onBatchScanResults: " + results.size() + " results");
            for (ScanResult result : results)
            {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            Log.d(TAG, "LE Scan Failed: " + errorCode);
        }

        private void processResult(ScanResult device)
        {
            Log.i(TAG, "New LE Device: " + device.getDevice().getName() + " @ " + device.getRssi() + " Address " + device.getDevice().getAddress());
            String deviceName;
            deviceName = device.getDevice().getName();
            if(searchingFromDetails){
                if(deviceName != null){
                    Log.d(TAG, "processResult: Here to check the name.");
                    if(deviceName.equals("JohnCougarMellenc")){
                        boolean newDevice = true;
                        for(int i = 0; i<shockclockCount; i++){
                            if(device.getDevice().getAddress().equals(deviceIDs[i])){
                                newDevice = false;
                            }
                        }
                        if(newDevice){
                            Log.d(TAG, "processResult: Adding new Device");
                            deviceIDs[shockclockCount] = device.getDevice().getAddress();
                            deviceRSSIs[shockclockCount] = device.getRssi();
                            shockclockCount++;
                        }else{
                            Log.d(TAG, "processResult: Device already added");
                        }

                    }
                }
            }
            else{
                if(deviceName != null){
                    if(deviceName.equals("JohnCougarMellenc")){
                        List<TrustedDevice> trustedDevices = TrustedDevice.getTrustedDevices(context);
                        for(TrustedDevice trustedDevice:trustedDevices){
                            if(device.getDevice().getAddress().toString().equals(trustedDevice.getAddress())){
                                //TODO: READ ME: THIS IS JUST FOR YOUR CHECKING TO VERIFY THAT DEVICE WILL BE CONNECTED TO THE SPECIFIC TYPE ONLY. REMOVE TOAST before delivery.
                                Toast.makeText(context, "Found trusted device registered with "+trustedDevice.getAssignedType()+". This will attach only if you are looking for sensor of "+ trustedDevice.getAssignedType(), Toast.LENGTH_LONG).show();
                                String bleEvent = "scan";
                                intent.putExtra("bleEvent", bleEvent);
                                sendBroadcast(intent);
                                if(searchingHand && trustedDevice.getAssignedType().equals(TrustedDevice.TYPE_HAND)){
                                    BluetoothDevice sensor = device.getDevice();
                                    scanner.stopScan(mScanCallback);
                                    scanning = false;
                                    handGatt = sensor.connectGatt(getAppContext(),false,bleGattCallback);
                                }
                                else if(searchingLowerArm && trustedDevice.getAssignedType().equals(TrustedDevice.TYPE_LOWER_ARM)){
                                    BluetoothDevice sensor = device.getDevice();
                                    scanner.stopScan(mScanCallback);
                                    scanning = false;
                                    lowerarmGatt = sensor.connectGatt(getAppContext(),false,bleGattCallback);
                                }
                                else if(searchingUpperArm  && trustedDevice.getAssignedType().equals(TrustedDevice.TYPE_UPPER_ARM)){
                                    BluetoothDevice sensor = device.getDevice();
                                    scanner.stopScan(mScanCallback);
                                    scanning = false;
                                    upperarmGatt = sensor.connectGatt(getAppContext(),false,bleGattCallback);
                                }
                                else if(searchingBack  && trustedDevice.getAssignedType().equals(TrustedDevice.TYPE_BACK)){
                                    BluetoothDevice sensor = device.getDevice();
                                    scanner.stopScan(mScanCallback);
                                    scanning = false;
                                    backGatt = sensor.connectGatt(getAppContext(),false,bleGattCallback);
                                }
                            }
                        }

                    }
                    //if(device.getDevice().getAddress().equals("A0:E6:F8:BF:E6:04")){
                    if(deviceName.equals("FireflyPCM")){
                        if(searchingPCM){
                            BluetoothDevice sensor = device.getDevice();
                            scanner.stopScan(mScanCallback);
                            scanning = false;
                            fireflyGatt = sensor.connectGatt(getAppContext(),false,bleGattCallback);
                        }
                    }
                }

            }
        }
    };

    public void initializeBle(){
        adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();
    }

    public final BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if(gatt == handGatt | gatt == lowerarmGatt | gatt == upperarmGatt | gatt == backGatt) {
                byte[] temp = characteristic.getValue();

                /*int MSB = temp[1] << 8;
                int LSB = temp[0] & 0x000000FF;
                int val = MSB | LSB;
                float gyroZ = val * 0.0625f;
                MSB = temp[3] << 8;
                LSB = temp[2] & 0x000000FF;
                val = MSB | LSB;
                float gyroY = val * 0.0625f;
                MSB = temp[5] << 8;
                LSB = temp[4] & 0x000000FF;
                val = MSB | LSB;
                float gyroX = val * 0.0625f;*/

                //retrieving and calculated needed data from the sensors

                int MSB = temp[1] << 8;
                int LSB = temp[0] & 0x000000FF;
                int val = MSB | LSB;
                double quatw = val * 0.000061035f;
                MSB = temp[3] << 8;
                LSB = temp[2] & 0x000000FF;
                val = MSB | LSB;
                double quatx = val * 0.000061035f;
                MSB = temp[5] << 8;
                LSB = temp[4] & 0x000000FF;
                val = MSB | LSB;
                double quaty = val * 0.000061035f;
                MSB = temp[7] << 8;
                LSB = temp[6] & 0x000000FF;
                val = MSB | LSB;
                double  quatz = val * 0.000061035f;

                double  sqw = quatw*quatw;
                double sqx = quatx*quatx;
                double sqy = quaty*quaty;
                double sqz = quatz*quatz;


                // invs (inverse square length) is only required if quaternion is not already normalised
                double invs = 1 / (sqx + sqy + sqz + sqw);
                double gyroZ = (-sqx - sqy + sqz + sqw)*invs*90 ;//gyroz

                double tmp1 = quatx*quatz;
                double tmp2 = quaty*quatw;

                double gyroX= 2.0 * (tmp1 - tmp2)*invs*90 ; //gyrox

                tmp1 = quaty*quatz;
                tmp2 = quatx*quatw;
                double gyroY = 2.0 * (tmp1 + tmp2)*invs*90; //gyroy


                String bleEvent = "notification";


                intent.putExtra("bleEvent", bleEvent);
                if(gatt == handGatt){
                    BleNotification notification = new BleNotification((float) gyroX, (float)gyroY, (float)gyroZ, "hand");
                    intent.putExtra("notifyObject", notification);
                    intent.putExtra("gatt","hand");
                    intent.putExtra("valueX", gyroX);
                    intent.putExtra("valueY", gyroY);
                    intent.putExtra("valueZ", gyroZ);
                }
                else if(gatt == lowerarmGatt){
                    BleNotification notification = new BleNotification((float) gyroX, (float)gyroY, (float)gyroZ, "lowerarm");
                    intent.putExtra("notifyObject", notification);
                    intent.putExtra("gatt","lowerarm");
                    intent.putExtra("valueX", gyroX);
                    intent.putExtra("valueY", gyroY);
                    intent.putExtra("valueZ", gyroZ);
                }
                else if(gatt == upperarmGatt){
                    BleNotification notification = new BleNotification((float) gyroX, (float)gyroY, (float)gyroZ, "upperarm");
                    intent.putExtra("notifyObject", notification);
                    intent.putExtra("gatt","upperarm");
                    intent.putExtra("valueX", gyroX);
                    intent.putExtra("valueY", gyroY);
                    intent.putExtra("valueZ", gyroZ);
                }
                else if(gatt == backGatt) {
                    BleNotification notification = new BleNotification((float) gyroX, (float)gyroY, (float)gyroZ,"back");
                    intent.putExtra("notifyObject", notification);
                    intent.putExtra("gatt", "back");
                    intent.putExtra("valueX", gyroX);
                    intent.putExtra("valueY", gyroY);
                    intent.putExtra("valueZ", gyroZ);
                }
                    
                sendBroadcast(intent);

            }
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            if(newState == disconnected) {
                String bleEvent = "sensorDisconnected";
                intent.putExtra("bleEvent", bleEvent);
                if(gatt.equals(handGatt)){
                    intent.putExtra("gatt","hand");
                }
                else if(gatt.equals(lowerarmGatt)){
                    intent.putExtra("gatt","lowerarm");
                }
                else if(gatt.equals(upperarmGatt)){
                    intent.putExtra("gatt","upperarm");
                }
                else if(gatt.equals(backGatt)){
                    intent.putExtra("gatt","back");
                }
                else if(gatt.equals(fireflyGatt)){
                    intent.putExtra("gatt","firefly");
                    fireflyFound = false;
                }
                else{
                    intent.putExtra("gatt", "unknown");
                }
                sendBroadcast(intent);
            }
            else if( newState == connecting) {
            }
            else if( newState == connected) {
                Log.v(TAG, "device connected");
                gatt.discoverServices();
            }
        }
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {

        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic,int status) {
            Log.v(TAG, "charRead");
        }
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            Log.v(TAG, "services discovered");
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for(int i = 0; i < characteristics.size(); i++){
                    if(characteristics.get(i).getUuid().toString().equals("0000beef-1212-efde-1523-785fef13d123")){
                        NRF_CHARACTERISTIC = service.getCharacteristic(UUID.fromString("0000beef-1212-efde-1523-785fef13d123"));
                        gatt.setCharacteristicNotification(NRF_CHARACTERISTIC,true);
                        UUID dUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                        BluetoothGattDescriptor notifyDescriptor = NRF_CHARACTERISTIC.getDescriptor(dUUID);
                        notifyDescriptor.setValue(ENABLE_NOTIFICATION_VALUE);
                        boolean b = gatt.writeDescriptor(notifyDescriptor);
                        scanner.stopScan(mScanCallback);
                        scanning = false;
                        String bleEvent = "sensorConnected";
                        intent.putExtra("bleEvent", bleEvent);
                        intent.putExtra("gatt", "undetermined");
                        if(gatt == handGatt){
                            intent.putExtra("gatt", "hand");
                        }
                        if(gatt == lowerarmGatt){
                            intent.putExtra("gatt", "lowerarm");
                        }
                        if(gatt == upperarmGatt){
                            intent.putExtra("gatt","upperarm");
                        }
                        if(gatt == backGatt){
                            intent.putExtra("gatt","back");
                        }
                        sendBroadcast(intent);
                        Log.v(TAG, String.valueOf(b));
                    }
                    if(characteristics.get(i).getUuid().toString().equals("0000fff2-0000-1000-8000-00805f9b34fb")) {
                        FIREFLY_CHARACTERISTIC2 = characteristics.get(i);
                        FIREFLY_CHARACTERISTIC2.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        fireflyFound = true;
                        Log.v(TAG, "pcm connected");
                        String bleEvent = "sensorConnected";
                        intent.putExtra("bleEvent", bleEvent);
                        intent.putExtra("gatt","firefly");
                        sendBroadcast(intent);
                    }
                }
            }
        }
    };

   /* public void sendMessageForPCM(String event){

        Message msg = Message.obtain();

        Bundle bundle = new Bundle();
        bundle.putString("pcmEvent", event);

        msg.setData(bundle);
        msg.replyTo = new Messenger(new IncomingHandler());

        try {
            msg.replyTo = mMessenger;
            pcmMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/
}
