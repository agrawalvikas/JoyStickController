package app.akexorcist.joystickcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Vikas on 26-06-2015.
 */
public class ConnectThread extends Thread {

    private final BluetoothSocket mmSocket;
    Handler mHandler;
    String tag = "debugging";
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    protected static final int CHK_BLUE = 2;
    BluetoothAdapter btAdapter;
    String chk_blue = "Check bluetooth status on robot";


    public ConnectThread(BluetoothDevice device,BluetoothAdapter bAdapter,Handler handler) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        this.mHandler = handler;
        this.btAdapter = bAdapter;
        Log.i(tag, "In connectThread");
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            Log.i(tag, "Got Socket");
        } catch (IOException e) {
            Log.i(tag, "get socket failed");

        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        btAdapter.cancelDiscovery();
        Log.i(tag, "connect - run");
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
            Log.i(tag, "connect - succeeded");

        } catch (IOException connectException) {
            Log.i(tag, "connect failed");
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
                mHandler.obtainMessage(CHK_BLUE, chk_blue).sendToTarget();
            } catch (IOException closeException) {
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)

        mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
    }


    /**
     * Will cancel an in-progress connection, and close the socket
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (Exception e) {
            Log.i(tag, "Can not close :" + e.getMessage());
        }
    }
}

