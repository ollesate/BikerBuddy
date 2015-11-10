package sjoholm.olof.gps_mc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Set;

import sjoholm.olof.gps_mc.AsynchTaskURL;
import sjoholm.olof.gps_mc.BluetoothHandler;
import sjoholm.olof.gps_mc.Direction;
import sjoholm.olof.gps_mc.Fragments.AutocompleteFragment;
import sjoholm.olof.gps_mc.Fragments.MapFragment;
import sjoholm.olof.gps_mc.Fragments.googleMapFragment;
import sjoholm.olof.gps_mc.GPSTracker;
import sjoholm.olof.gps_mc.MainActivity;
import sjoholm.olof.gps_mc.R;

/**
 * Created by w1 on 2015-11-10.
 */
public class Controller {

    private MainActivity context;

    private BluetoothHandler bl_handler;
    private BluetoothDevice connectedDevice = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private GPSTracker gps;

    private AutocompleteFragment autocompleteFragment;
    private MapFragment mapFragment;
    private googleMapFragment googleMapFragment;

    public Controller(MainActivity context){
        this.context = context;
        initializeBluetooth();
        initializeGPS(context);
        intializeFragments();
    }

    private void intializeFragments() {
        googleMapFragment = new googleMapFragment();
        autocompleteFragment = new AutocompleteFragment();
        mapFragment = new MapFragment();
    }

    private void initializeGPS(Context context) {
        gps = GPSTracker.Singleton.getInstance(context);
        gps.setListener(new LocationSource.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                GPS_OnUpdate(location);
            }
        });
    }

    private void initializeBluetooth(){
        bl_handler = new BluetoothHandler();
    }

    private final String BLUETOOTH_MODULE_NAME = "HC-06";

    public void Bluetooth_TryConnect_HC06(){
        bl_handler = new BluetoothHandler();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        connectedDevice = null;

        if(bl_handler.isConnected())
            return;

        for(BluetoothDevice device : pairedDevices){
            if(device.getName().equals(BLUETOOTH_MODULE_NAME)){
                connectedDevice = device;
            }
        }

        if(connectedDevice == null){
            bluetooth_OpenPairWindow();
        }else{
            bluetooth_Connect(connectedDevice);
        }
    }

    private void bluetooth_OpenPairWindow(){
        //TODO
    }

    private void bluetooth_OpenTurnOnWindow(){
        //TODO
    }

    private void bluetooth_Connect(BluetoothDevice device){
        bl_handler.Connect(device);
    }

    public void Bluetooth_Disconnect(){
        bl_handler.Disconnect();
    }

    public void Bluetooth_Send(String message){
        bl_handler.send(message);
    }

    private void GPS_OnUpdate(Location location){

    }

    public void StartMainFragment(){
        replaceFragment(new AutocompleteFragment());
    }

    public void replaceFragment(Fragment fragment){
        context.getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    public void MapClick(){

    }

    public void AutocompleteClick(){

    }

    private void sendGoogleDirectionRequest(LatLng originCoord, LatLng destinationCoord){

        AsynchTaskURL asynchTaskURL = new AsynchTaskURL(new AsynchTaskURL.OnResultListener() {
            @Override
            public void OnResult(ArrayList<Direction> dirs) {
                onGoogleDirectionResult(dirs);
            }
        });

        String base = "https://maps.googleapis.com/maps/api/directions/json?";
        String origin = originCoord.latitude + "," + originCoord.longitude;
        String destination = destinationCoord.latitude + "," + destinationCoord.longitude;
        String query = base + "origin=" + origin + "&destination=" + destination;

        asynchTaskURL.execute(query);

    }

    private void onGoogleDirectionResult(ArrayList<Direction> dirs){

    }

}
