package sjoholm.olof.gps_mc;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by w1 on 2015-10-16.
 */
public class BluetoothHandler {

    public BluetoothHandler(BluetoothDevice device){
        Connect(device);
    }

    public BluetoothHandler(){

    }


    public void send(String message){
        if(connection != null) {
            byte[] b = message.getBytes();
            connection.write(b);
        }
    }


    public void Connect(BluetoothDevice bluetoothDevice) {
        new Thread(new ConnectThread(bluetoothDevice)).start();
    }

    public  void Disconnect(){
        if(connection != null)
            connection.cancel();
    }

    public boolean isConnected(){
        return false;
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
                Log.d("Bluetooth", "Before setting up RFcommSocket");
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(SERVER_UUID);

                Log.d("Bluetooth", "After setting up RFcommSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        private  final UUID SERVER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public void run() {
            // Cancel discovery because it will slow down the connection

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.d("Bluetooth", "Trying to connect");
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
            Log.d("Bluetooth", "Seems like we are connected!");
            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

    }

    private ConnectedThread connection;

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

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        connection = new ConnectedThread(mmSocket);
        connection.start();
    }

}
