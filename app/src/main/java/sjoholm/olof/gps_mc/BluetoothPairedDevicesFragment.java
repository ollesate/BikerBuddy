package sjoholm.olof.gps_mc;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothPairedDevicesFragment extends Fragment {


    public BluetoothPairedDevicesFragment() {
        // Required empty public constructor
    }

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter mArrayAdapter;

    private void startBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

    }

    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        startBluetooth();
        mArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
        View view = (View) inflater.inflate(R.layout.fragment_bluetooth_paired_devices, container, false);
        Button button =(Button) view.findViewById(R.id.bScanPairedDevices);
        button.setOnClickListener(mClickListener);
        mListView = (ListView) view.findViewById(R.id.lvPairedDevices);
        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = mArrayList.get(i);
                ConnectedToDeviceFragment fragment = new ConnectedToDeviceFragment ();
                fragment.setBluetoothDevice(device);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
            }
        });
        return view;
    }

    private ArrayList<BluetoothDevice> mArrayList = new ArrayList<BluetoothDevice>();

    private View.OnClickListener mClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Set<BluetoothDevice>pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            BluetoothDevice correctDevice = null;
            mArrayAdapter.clear();
            mArrayList.clear();
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapter.add("Name: " + device.getName() +  ", Adress: " + device.getAddress());
                    mArrayList.add(device);
                    Log.d("bluetooth", device.getName());
                }
            }
            mListView.setAdapter(mArrayAdapter);
        }
    };



}
