package com.example.evan.bluetoothtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;

public class Transmissions extends AppCompatActivity {

    //Widgets
    Button btnGetValues, btnDisconnectBT;
    EditText txtValueDisplay;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmissions);

        //receive the address of the bluetooth device
        Intent newInt = getIntent();
        address = newInt.getStringExtra(MainActivity.EXTRA_ADDRESS);

        //view of the ledControl layout
        setContentView(R.layout.activity_transmissions);
        //call the widgets
        btnGetValues = (Button)findViewById(R.id.btnGetValues);
        btnDisconnectBT = (Button)findViewById(R.id.btnDisconnect);
        txtValueDisplay = (EditText)findViewById(R.id.txtValue1);

        new ConnectBT().execute(); //Call the class to connect

        btnGetValues.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                msg("button pushed");
                requestValues();      //method to send info to the Arduino and request values
            }
        });

        btnDisconnectBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                disconnectBT();     //method to disconnect the bluetooth device
            }
        });
    }

    private void requestValues()
    {
        //msg("in function");
        if (btSocket!=null)
        {
            try
            {
                //msg("Sending");
                btSocket.getOutputStream().write("TO".getBytes());
                while(btSocket.getInputStream().available()<1)
                {
                    //wait until the input stream has bytes
                }
                //msg("done waiting");
                byte[] buffer = {};
                int BytesRead = btSocket.getInputStream().read(buffer);
                msg(Integer.toString(BytesRead) + " bytes read.");
                String str = new String(buffer, "UTF-8"); //encoding type, probably wrong...
                txtValueDisplay.setText(str, TextView.BufferType.EDITABLE);
            }
            catch (IOException e)
            {
                msg("IO Error:" +e.getMessage());
            }
            catch (Exception e)
            {
                msg("Error:" +e.getMessage());
            }
        }
    }

    private void disconnectBT()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(Transmissions.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}


