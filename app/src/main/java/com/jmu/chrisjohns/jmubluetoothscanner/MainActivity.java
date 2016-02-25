package com.jmu.chrisjohns.jmubluetoothscanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;



public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;

    ListView listDevicesFound;
    ListView listWifi;
    Button btnScanWifi;
    Button btnScanDevice;
    Button btnEnableDiscoverability;
    TextView stateBluetooth;
    BluetoothAdapter bluetoothAdapter;
    WifiManager wifi;
    WifiScanReceiver wifiReciever;


    ArrayAdapter<String> btArrayAdapter;
    ArrayAdapter<String> wifiArrayAdapter;


    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScanDevice = (Button) findViewById(R.id.scandevice);

        btnEnableDiscoverability = (Button) findViewById(R.id.EnableDiscoverability);

        btnScanWifi = (Button) findViewById(R.id.scanwifi);

        stateBluetooth = (TextView) findViewById(R.id.bluetoothstate);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listDevicesFound = (ListView) findViewById(R.id.devicesfound);
        btArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
        listDevicesFound.setAdapter(btArrayAdapter);


        btnScanDevice.setOnClickListener(btnScanDeviceOnClickListener);
        btnEnableDiscoverability.setOnClickListener(btnEnableDiscoverabilityOnClickListener);
        btnScanWifi.setOnClickListener(btnScanWifiOnClickListener);


        listWifi = (ListView) findViewById(R.id.ListWifi);
        wifiArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
        listWifi.setAdapter(wifiArrayAdapter);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();


        //register broadcast receiver
        registerReceiver(ActionFoundReceiver,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));

        Log.d("MainActivity", "onCreate");
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(ActionFoundReceiver);
        unregisterReceiver(wifiReciever);

    }

    // Check if bluetooth is on and request to turn on if off
    private void CheckBlueToothState(){
        if (bluetoothAdapter == null){
            stateBluetooth.setText(R.string.no_bluetooth);
        }
        else{
            if (bluetoothAdapter.isEnabled()){
                if(bluetoothAdapter.isDiscovering()){
                    stateBluetooth.setText(R.string.discover_devices);
                }else{
                    stateBluetooth.setText(R.string.bluetooth_enabled);
                    btnScanDevice.setEnabled(true);
                }
            }else{
                stateBluetooth.setText(R.string.bluetooth_disabled);
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }


    }

    private Button.OnClickListener btnScanDeviceOnClickListener
            = new Button.OnClickListener(){

        //When button is clicked check BT state, clear the array, and begin discovery of devices
        @Override

        public void onClick(View v) {
            CheckBlueToothState();
            btArrayAdapter.clear();
            bluetoothAdapter.startDiscovery();
            Log.d("MainActivity", "onClick");



        }
    };

    private Button.OnClickListener btnEnableDiscoverabilityOnClickListener
            = new Button.OnClickListener(){

        @Override

        public void onClick(View v) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, 0);
            Log.d("MainActivity", "onClick");
        }
    };

    private Button.OnClickListener btnScanWifiOnClickListener
            = new Button.OnClickListener(){

        //When button is clicked check BT state, clear the array, and begin discovery of devices
        @Override

        public void onClick(View v) {

            wifi.startScan();
            List<ScanResult> wifiResults = wifi.getScanResults();
            System.out.println(getBaseContext().getFilesDir());
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            System.out.println(timeStamp);
            String wififilename="wifi.txt" + timeStamp;

            try {
                File myFile = new File("/sdcard/"+wififilename);
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

                for (int i = 0; i < wifiResults.size(); i++) {
                    String wifiscantext = (wifiResults.get(i).toString() + "\n");
                    myOutWriter.append(wifiscantext);
                    System.out.println(wifiResults.get(i).toString());

                }

                myOutWriter.close();
                fOut.close();

                Toast.makeText(getApplicationContext(),wififilename + " saved",Toast.LENGTH_LONG).show();


            } catch (IOException e) {e.printStackTrace();}




            File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), wififilename);
            Uri path = Uri.fromFile(filelocation);
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
// set the type to 'email'
            emailIntent .setType("vnd.android.cursor.dir/email");
            String to[] = {"johnsct@dukes.jmu.edu"};
            emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
// the attachment
            emailIntent .putExtra(Intent.EXTRA_STREAM, path);
// the mail subject
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            startActivity(Intent.createChooser(emailIntent , "Send email..."));




            Log.d("MainActivity", "onClick");

        }
    };



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT){
            CheckBlueToothState();
            Log.d("MainActivity", "onActivityResult");
        }
    }

    protected void onPause() {
        super.onPause();

        Log.d("MainActivity", "onPause");
    }

    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");

    }




    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

        // If bluetooth device is found then add it to our array
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String DeviceList = null;

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                btArrayAdapter.notifyDataSetChanged();
                for (int j = 0; j < btArrayAdapter.getCount(); j++) {
                    DeviceList +=  device;

                }
                System.out.println(DeviceList);

            }


            /*
            //This will get the SD Card directory and create a folder named MyFiles in it.
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File (sdCard.getAbsolutePath() + "/MyFiles");
            directory.mkdirs();

//Now create the file in the above directory and write the contents into it
            File file = new File(directory, "mysdfile.txt");
            try {

                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                osw.write(DeviceList);
                osw.flush();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */




            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"johnsct@dukes.jmu.edu"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject");
            emailIntent.putExtra(Intent.EXTRA_TEXT, DeviceList);
            //emailIntent.putExtra(Intent.EXTRA_STREAM, "btScan.txt");
            emailIntent.setType("message/rfc822");

            try {
                startActivity(emailIntent);
                finish();
                Log.i("Finished sending email", "");
            }
            catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
            }




        }};



    private class WifiScanReceiver extends BroadcastReceiver{
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();

            listWifi.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, wifiScanList));
        }
    }







}

// TIMER, CONNECT/SAVE DATA, WIFI