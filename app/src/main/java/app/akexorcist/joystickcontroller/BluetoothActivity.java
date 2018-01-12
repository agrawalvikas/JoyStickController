package app.akexorcist.joystickcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class BluetoothActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    ProgressBar mProgressBar;
    Toolbar toolbar;
    ArrayAdapter<String> listAdapter;
    TextView tvSearch;
    ListView listView;
    Button search, disconnect;
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;

    BluetoothDevice temp;
    IntentFilter filter;
    BluetoothSocket tempSocket;
    String tag = "debugging";
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    protected static final int CHK_BLUE = 2;
    BroadcastReceiver receiver;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.i(tag, "in message handler");

            switch (msg.what) {
                case CHK_BLUE:
                    String chk = (String) msg.obj;
                    Toast.makeText(getApplicationContext(), chk, Toast.LENGTH_SHORT).show();
                    break;

                case SUCCESS_CONNECT:
                    // DO something
                    Log.i(tag, "connected");
                    //Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_SHORT).show();
                    tempSocket = (BluetoothSocket) msg.obj;
                    ConnectedThread connectedThread = new ConnectedThread(tempSocket,mHandler,getApplicationContext());
                    connectedThread.start();
                    String write = "Worked!";
                    byte[] writebuf = write.getBytes();
                    connectedThread.write(writebuf);
                    //Intent joystickIntent = new Intent(getApplicationContext(),Main.class);
                    //joystickIntent.putExtra("device",temp);
                    //startActivity(joystickIntent);

                    //String s = "successfully connected";
                    //connectedThread.write(s.getBytes());
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String string = new String(readBuf);
                    tvSearch.append(string);
                    Log.i(tag, "in message handler received msg from HC-05");
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        init();
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!btAdapter.isEnabled()) {
                turnOnBT();
            }
        }
    }

    public void init() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        tvSearch = (TextView) findViewById(R.id.tvSearch);
        disconnect = (Button) findViewById(R.id.bDisconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (tempSocket != null) {
                    ConnectedThread connectedThread = new ConnectedThread(tempSocket,mHandler,getApplicationContext());
                    connectedThread.cancel();
                    Toast.makeText(getApplication(), "DISCONNECTED", Toast.LENGTH_SHORT).show();
                    tempSocket = null;
                } else {
                    Toast.makeText(getApplication(), "No connected device found!..", Toast.LENGTH_SHORT).show();
                }

            }
        });
        search = (Button) findViewById(R.id.button);
        search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                getPairedDevices();
                startDiscovery();
            }
        });
        pairedDevices = new ArrayList<>();
        devices = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, 0);
        listView.setAdapter(listAdapter);

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    String s = "";
                    for (int a = 0; a < pairedDevices.size(); a++) {
                        if (device.getName().equals(pairedDevices.get(a))) {
                            //append
                            s = "(Paired)";
                            break;
                        }
                    }
                    listAdapter.add(device.getName() + " " + s + " " + "\n" + device.getAddress());
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    // run some code
                    tvSearch.setText("Searching...Please wait");
                    mProgressBar.setVisibility(View.VISIBLE);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    // run some code
                    tvSearch.setText("Available Devices");
                    mProgressBar.setVisibility(View.INVISIBLE);
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if (btAdapter.getState() == btAdapter.STATE_OFF) {
                        turnOnBT();
                    }
                }

            }
        };

        registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }


    //to startDiscovery of bluetooth devices
    private void startDiscovery() {
        // TODO Auto-generated method stub
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();

    }

    //Turn the bluetooth on
    private void turnOnBT() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    //get the list of paired devices in ArrayList
    private void getPairedDevices() {
        // TODO Auto-generated method stub
        devicesArray = btAdapter.getBondedDevices();
        if (devicesArray.size() > 0) {
            for (BluetoothDevice device : devicesArray) {
                pairedDevices.add(device.getName());
            }
        }
        return;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                            long arg3) {
        // TODO Auto-generated method stub

        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        //if(listAdapter.getItem(arg2).contains("Paired")){

        BluetoothDevice selectedDevice = devices.get(arg2);
        ConnectThread connect = new ConnectThread(selectedDevice,btAdapter,mHandler);
        connect.start();
        Log.i(tag, "in click listener");
        //}
            /*else{
                Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_LONG).show();
			}*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
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
}
