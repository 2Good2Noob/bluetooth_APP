package com.example.robot;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {

    private static final String DEVICE_ADDRESS = "98:D3:91:FE:C0:25";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private OutputStream outputStream = null;

    private Button btnConnect, btnForward, btnBackward, btnLeft, btnRight, btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prośba o uprawnienie Bluetooth (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }
        }

        btnConnect = findViewById(R.id.btnConnect);
        btnForward = findViewById(R.id.btnForward);
        btnBackward = findViewById(R.id.btnBackward);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnStop = findViewById(R.id.btnStop);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectBluetooth();
            }
        });

        btnForward.setOnClickListener(v -> sendCommand("U"));
        btnBackward.setOnClickListener(v -> sendCommand("D"));
        btnLeft.setOnClickListener(v -> sendCommand("L"));
        btnRight.setOnClickListener(v -> sendCommand("R"));
        btnStop.setOnClickListener(v -> sendCommand("S"));
    }

    private void connectBluetooth() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void sendCommand(String command) {
        if (outputStream != null) {
            try {
                outputStream.write(command.getBytes());
            } catch (IOException e) {
                Toast.makeText(this, "Error sending command", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
