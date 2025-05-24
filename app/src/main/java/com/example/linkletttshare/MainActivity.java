package com.example.linkletttshare;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    private static final int REQUEST_ENABLE_BLUETOOTH = 3;
    private static final int REQUEST_CONTACTS_PERMISSION = 4;

    private TextView selectedContactTextView;
    private TextView nfcStatusTextView;
    private TextView bluetoothStatusTextView;
    private Button selectContactButton;
    private Button scanDevicesButton;
    private View bluetoothStatusDot;
    private View nfcStatusDot;

    private NfcAdapter nfcAdapter;
    private BluetoothAdapter bluetoothAdapter;
    
    private Contact selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        selectedContactTextView = findViewById(R.id.selectedContactTextView);
        nfcStatusTextView = findViewById(R.id.nfcStatusTextView);
        bluetoothStatusTextView = findViewById(R.id.bluetoothStatusTextView);
        selectContactButton = findViewById(R.id.selectContactButton);
        scanDevicesButton = findViewById(R.id.scanDevicesButton);
        bluetoothStatusDot = findViewById(R.id.bluetoothStatusDot);
        nfcStatusDot = findViewById(R.id.nfcStatusDot);

        // Set up NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            nfcStatusTextView.setText(R.string.nfc_not_supported);
            nfcStatusTextView.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        } else if (!nfcAdapter.isEnabled()) {
            nfcStatusTextView.setText(R.string.nfc_disabled);
            nfcStatusTextView.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        } else {
            nfcStatusTextView.setText(R.string.nfc_available);
            nfcStatusTextView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }

        // Set up Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        
        if (bluetoothAdapter == null) {
            bluetoothStatusTextView.setText(R.string.bluetooth_not_supported);
            bluetoothStatusTextView.setTextColor(ContextCompat.getColor(this, R.color.error_red));
            bluetoothStatusDot.setBackground(ContextCompat.getDrawable(this, R.drawable.status_dot_red));
        } else if (!bluetoothAdapter.isEnabled()) {
            bluetoothStatusTextView.setText(R.string.bluetooth_disabled);
            bluetoothStatusTextView.setTextColor(ContextCompat.getColor(this, R.color.error_red));
            bluetoothStatusDot.setBackground(ContextCompat.getDrawable(this, R.drawable.status_dot_red));
        } else {
            bluetoothStatusTextView.setText(R.string.bluetooth_available);
            bluetoothStatusTextView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            bluetoothStatusDot.setBackground(ContextCompat.getDrawable(this, R.drawable.status_dot_green));
        }

        // Set up click listeners
        selectContactButton.setOnClickListener(v -> selectContact());
        scanDevicesButton.setOnClickListener(v -> scanForDevices());

        // Request permissions
        requestContactsPermission();
    }

    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_CONTACTS_PERMISSION);
        }
    }

    private void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    private void scanForDevices() {
        if (selectedContact == null) {
            Toast.makeText(this, "Please select a contact first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if Bluetooth is enabled
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            return;
        }

        // Check Bluetooth permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
            return;
        }

        // Start Bluetooth device discovery
        if (bluetoothAdapter != null) {
            bluetoothAdapter.startDiscovery();
            Toast.makeText(this, "Scanning for Bluetooth devices...", Toast.LENGTH_SHORT).show();
            
            // In a real app, you would implement a DeviceScanActivity
            Intent intent = new Intent(this, DeviceScanActivity.class);
            intent.putExtra("contact", selectedContact);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CONTACT && resultCode == RESULT_OK) {
            if (data != null) {
                Uri contactUri = data.getData();
                if (contactUri != null) {
                    retrieveContactInfo(contactUri);
                }
            }
        } else if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == RESULT_OK) {
            // Bluetooth has been enabled, try scanning again
            scanForDevices();
        }
    }

    private void retrieveContactInfo(Uri contactUri) {
        String[] projection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
        };

        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                boolean hasPhone = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0;
                
                String phoneNumber = "";
                if (hasPhone) {
                    try (Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId}, null)) {
                        
                        if (phoneCursor != null && phoneCursor.moveToFirst()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                    }
                }

                selectedContact = new Contact(contactId, displayName, phoneNumber);
                selectedContactTextView.setText(displayName);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error retrieving contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
