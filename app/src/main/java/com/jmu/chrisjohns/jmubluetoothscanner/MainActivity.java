package com.jmu.chrisjohns.jmubluetoothscanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.util.Date;
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
    String[] wifis;
    WifiScanReceiver wifiReciever;
    FileOutputStream OutputStream;


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

        CheckBlueToothState();

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
    }

    // Check if bluetooth is on and request to turn on if off
    private void CheckBlueToothState() {
        if (bluetoothAdapter == null) {
            stateBluetooth.setText(R.string.no_bluetooth);
        } else {
            if (bluetoothAdapter.isEnabled()) {
                if (bluetoothAdapter.isDiscovering()) {
                    stateBluetooth.setText(R.string.discover_devices);
                } else {
                    stateBluetooth.setText(R.string.bluetooth_enabled);
                    btnScanDevice.setEnabled(true);
                }
            } else {
                stateBluetooth.setText(R.string.bluetooth_disabled);
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }


    }

    private Button.OnClickListener btnScanDeviceOnClickListener
            = new Button.OnClickListener() {

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
            = new Button.OnClickListener() {

        @Override

        public void onClick(View v) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, 0);
            Log.d("MainActivity", "onClick");
        }
    };

    private Button.OnClickListener btnScanWifiOnClickListener
            = new Button.OnClickListener() {

        //When button is clicked check BT state, clear the array, and begin discovery of devices
        @Override

        public void onClick(View v) {

            registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifi.startScan();
            Date d = new Date();
            List<ScanResult> wifiResults = wifi.getScanResults();
            System.out.println(wifiResults.toString().length());
            try {
                OutputStream = openFileOutput("wifiscan.txt", Context.MODE_PRIVATE);
                for (int i = 0; i < wifiResults.toString().length(); i++) {
                    OutputStream.write(wifiResults.toString().getBytes());
                }
                OutputStream.flush();
                OutputStream.close();


            } catch (Exception e) {
                System.out.println("error error");

            }

            Log.d("MainActivity", "onClick");


        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            CheckBlueToothState();
            Log.d("MainActivity", "onActivityResult");
        }
    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
        Log.d("MainActivity", "onPause");
    }

    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");

    }


    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver() {

        // If bluetooth device is found then add it to our array
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                btArrayAdapter.notifyDataSetChanged();
            }
        }
    };


    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();

            listWifi.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, wifiScanList));
        }
    }


}

// TIMER, CONNECT/SAVE DATA, WIFI