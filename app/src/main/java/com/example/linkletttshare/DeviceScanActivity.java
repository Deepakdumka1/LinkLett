package com.example.linkletttshare;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceScanActivity extends AppCompatActivity {

    private static final String TAG = "DeviceScanActivity";
    private static final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final String NAME_SECURE = "LinkLettSecure";
    
    // Message types and key names are defined in BluetoothConnectionService
    
    private BluetoothAdapter bluetoothAdapter;
    private ProgressBar progressBar;
    private ListView deviceListView;
    private DeviceAdapter deviceAdapter;
    private List<BluetoothDevice> deviceList;
    private Contact contactToShare;
    
    private BluetoothConnectionService connectionService;
    private String connectedDeviceName = null;
    
    // BroadcastReceiver for Bluetooth device discovery
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                } else {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                }
                
                if (device != null) {
                    // Get device name safely
                    String deviceName = "Unknown Device";
                    if (ActivityCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        deviceName = device.getName() != null ? device.getName() : "Unknown Device";
                    }
                    
                    Log.d(TAG, "Device found: " + deviceName + " - " + device.getAddress());
                    
                    boolean deviceExists = false;
                    for (BluetoothDevice d : deviceList) {
                        if (d.getAddress().equals(device.getAddress())) {
                            deviceExists = true;
                            break;
                        }
                    }
                    if (!deviceExists) {
                        deviceList.add(device);
                        deviceAdapter.notifyDataSetChanged();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                if (deviceList.isEmpty()) {
                    Toast.makeText(DeviceScanActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    // Handler for Bluetooth connection messages
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothConnectionService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnectionService.STATE_CONNECTED:
                            setTitle(getString(R.string.title_connected_to, connectedDeviceName));
                            Toast.makeText(DeviceScanActivity.this, "Connected to " + connectedDeviceName, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothConnectionService.STATE_CONNECTING:
                            setTitle(R.string.title_connecting);
                            break;
                        case BluetoothConnectionService.STATE_LISTEN:
                        case BluetoothConnectionService.STATE_NONE:
                            setTitle(R.string.title_not_connected);
                            break;
                    }
                    break;
                case BluetoothConnectionService.MESSAGE_WRITE:
                    Toast.makeText(DeviceScanActivity.this, "Contact sent successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to main activity after sending
                    break;
                case BluetoothConnectionService.MESSAGE_READ:
                    // We don't handle incoming data in this app
                    break;
                case BluetoothConnectionService.MESSAGE_DEVICE_NAME:
                    connectedDeviceName = msg.getData().getString(BluetoothConnectionService.DEVICE_NAME);
                    Toast.makeText(DeviceScanActivity.this, "Connected to " + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothConnectionService.MESSAGE_TOAST:
                    Toast.makeText(DeviceScanActivity.this, msg.getData().getString(BluetoothConnectionService.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    });

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                } else {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                }
                
                if (device != null) {
                    // Get device name safely
                    String deviceName = "Unknown Device";
                    if (ActivityCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        deviceName = device.getName() != null ? device.getName() : "Unknown Device";
                    }
                    
                    Log.d(TAG, "Device found: " + deviceName + " - " + device.getAddress());
                    
                    boolean deviceExists = false;
                    for (BluetoothDevice d : deviceList) {
                        if (d.getAddress().equals(device.getAddress())) {
                            deviceExists = true;
                            break;
                        }
                    }
                    if (!deviceExists) {
                        deviceList.add(device);
                        deviceAdapter.notifyDataSetChanged();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                if (deviceList.isEmpty()) {
                    Toast.makeText(DeviceScanActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        // Get the contact object to share
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            contactToShare = getIntent().getParcelableExtra("contact", Contact.class);
        } else {
            contactToShare = getIntent().getParcelableExtra("contact");
        }
        
        if (contactToShare == null) {
            Toast.makeText(this, "No contact selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up UI components
        progressBar = findViewById(R.id.progressBar);
        deviceListView = findViewById(R.id.deviceListView);
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        deviceListView.setAdapter(deviceAdapter);

        // Set up Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        
        // Initialize the BluetoothConnectionService
        connectionService = new BluetoothConnectionService(this, handler, APP_UUID);

        // Set item click listener for device selection
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            // Cancel discovery because it's resource intensive
            if (bluetoothAdapter.isDiscovering()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.cancelDiscovery();
                }
            }

            // Get the device MAC address
            BluetoothDevice device = deviceList.get(position);
            String deviceName = "Unknown Device";
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                deviceName = device.getName() != null ? device.getName() : device.getAddress();
            }
            
            Toast.makeText(DeviceScanActivity.this, 
                    "Connecting to " + deviceName, 
                    Toast.LENGTH_SHORT).show();
            
            // Attempt to connect to the device
            connectionService.connect(device);
            
            // Show progress for connection
            progressBar.setVisibility(View.VISIBLE);
        });

        // Register for broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
        Log.d(TAG, "Registered Bluetooth receiver");

        // Start discovery
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
                progressBar.setVisibility(View.VISIBLE);
                Log.d(TAG, "Starting Bluetooth discovery");
            } else {
                // Request permission if not granted
                ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 
                        123);
            }
        } else {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If Bluetooth is not enabled, request to enable it
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        2);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up the Bluetooth service if it was stopped
        if (connectionService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (connectionService.getState() == BluetoothConnectionService.STATE_NONE) {
                // Start the Bluetooth services
                connectionService.start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // If we're connected to a device, and connection service exists, tell the other device we're disconnecting
        if (connectionService != null && connectionService.getState() == BluetoothConnectionService.STATE_CONNECTED) {
            // Send a message to the connected device to let them know we're disconnecting
            String message = "Disconnecting...";
            byte[] send = message.getBytes();
            connectionService.write(send);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the broadcast receiver
        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
        
        // Cancel discovery
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
        
        // Stop the Bluetooth service
        if (connectionService != null) {
            connectionService.stop();
        }
    }
    
    // Method to send contact data once connected
    private void sendContactData() {
        if (connectionService != null && connectionService.getState() == BluetoothConnectionService.STATE_CONNECTED) {
            if (contactToShare != null) {
                // Send the contact as a string in the format: id|name|phoneNumber
                String contactString = contactToShare.getId() + "|" + contactToShare.getName() + "|" + contactToShare.getPhoneNumber();
                byte[] contactBytes = contactString.getBytes();
                connectionService.write(contactBytes);
                
                // Show confirmation toast
                Toast.makeText(this, "Sending contact: " + contactToShare.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {
        private final LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            super(context, 0, devices);
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
                holder = new ViewHolder();
                holder.text1 = convertView.findViewById(android.R.id.text1);
                holder.text2 = convertView.findViewById(android.R.id.text2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BluetoothDevice device = getItem(position);
            if (device != null) {
                holder.text1.setText(device.getName() != null ? device.getName() : "Unknown Device");
                holder.text2.setText(device.getAddress());
            }

            return convertView;
        }

        private static class ViewHolder {
            TextView text1;
            TextView text2;
        }
    }
}
