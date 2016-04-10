package com.example.konra.bluetoothwithpython;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private  SensorManager mSensorManager;
    private  Sensor mAccelerometer;
    private  Sensor rotationVector;
    private  Sensor orientation;

    private ArrayAdapter<String> mArrayAdapter;
    private OutputStream outputStream;
    private InputStream inStream;
    private BluetoothSocket mSocket = null;
    private ConnectedThread connectedThread = null;
    private int counter = 0;

    private float startalfa=0;
    private float startbeta=0;
    private float startgamma = 0;
    private float alfa=0;
    private float beta=0;
    private float gama=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",

        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        ListView listView = (ListView) findViewById(R.id.listview);

        mArrayAdapter = new ArrayAdapter<String>(
                this,
                R.layout.list_element,
                R.id.list_item,
                weekForecast);

        ListView list = (ListView) findViewById(R.id.listview);
        list.setAdapter(mArrayAdapter);






    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buttonAction (View view){
        Context context = getApplicationContext();
        CharSequence text = "Hello toast!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        setBluetothConection();


        toast.show();
    }

    protected void setBluetothConection(){
        /** zostaje sprawdzone czy tu w ogole jest bt*/
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        /** jest proszone o blututh */
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int  REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        /** wyswietlane sa polaczone urzadzenia */
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress() + "\n" + device.getUuids());
                ConnectThread connectThread = new ConnectThread(device);
                connectThread.run();
                mSocket = connectThread.getSocket();
                connectedThread = new ConnectedThread(mSocket);
//                String str = "dupa";
//                byte[] bytetab = str.getBytes();
//                connectedThread.write(bytetab);
                //sendMessage("nadawanie start");
                mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
//              mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//              rotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
               // mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
                //mSensorManager.registerListener(this,rotationVector,SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(this,orientation,SensorManager.SENSOR_DELAY_NORMAL);








        }
    }}

    public void sendMessage(View view){
        String str = "";
        str += "dupa"+Integer.toString(counter);
        counter++;
        byte[] bytetab = str.getBytes();
        connectedThread.write(bytetab);
    }

    public void sendMessage(String message){

        String str = message+Integer.toString(counter);
        counter++;
        byte[] bytetab = str.getBytes();
        connectedThread.write(bytetab);
    }

    public void calibrate(View viev){
        startalfa=alfa;
        startgamma=gama;
        startbeta=beta;
    }

    public void cancel(View view){
        connectedThread.cancel();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                String accelerationString = Float.toString(x)
                        + " " + Float.toString(y)
                        + " " + Float.toString(z);
                sendMessage(accelerationString);
            case Sensor.TYPE_ROTATION_VECTOR:
                float xr = event.values[0];
                float yr = event.values[1];
                float zr = event.values[2];
                String rotationString = Float.toString(xr)
                        + " " + Float.toString(yr)
                        + " " + Float.toString(zr);
                sendMessage(rotationString);
            case  Sensor.TYPE_ORIENTATION:
                alfa = event.values[0];
                beta = event.values[1];
                gama = event.values[2];
                String orientationString = Float.toString(alfa-startalfa)
                        + " " + Float.toString(beta-startbeta)
                        + " " + Float.toString(gama-startgamma) + " " ;
                sendMessage(orientationString);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String k= "";

    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
//            // Cancel discovery because it will slow down the connection
//            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

        public BluetoothSocket getSocket(){
            return mmSocket;
        }
    }



    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }



}

