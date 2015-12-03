package sjoholm.olof.gps_mc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import sjoholm.olof.gps_mc.Fragments.AutocompleteFragment;
import sjoholm.olof.gps_mc.Fragments.FooterInfoFragment;
import sjoholm.olof.gps_mc.Fragments.MainFragment;
import sjoholm.olof.gps_mc.Fragments.MapFragment;
import sjoholm.olof.gps_mc.Fragments.SimpleBluetoothConnectFragment;
import sjoholm.olof.gps_mc.Fragments.googleMapFragment;

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
    private SimpleBluetoothConnectFragment simpleBluetoothConnectFragment;
    private MainFragment mainFragment;
    private FooterInfoFragment footerInfoFragment;
    private LocationBuffer locations;

    public Controller(MainActivity context){
        this.context = context;
        initializeBluetooth();
        initializeFragments();
        initializeGPS(context);

        Bluetooth_TryFind_HC06();
    }

    private void initializeFragments() {

        footerInfoFragment = new FooterInfoFragment();

        mainFragment = new MainFragment();

        googleMapFragment = new googleMapFragment();

        googleMapFragment.setMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng mapLatLng) {
                sendGoogleDirectionRequest(gps.getLatLng(), mapLatLng);
            }
        });

        autocompleteFragment = new AutocompleteFragment();

        autocompleteFragment.setPlaceResultCallback(new AutocompleteFragment.PlaceResultCallback() {
            @Override
            public void onResult(LatLng place) {
                sendGoogleDirectionRequest(gps.getLatLng(), place);
            }
        });

        mapFragment = new MapFragment();

        simpleBluetoothConnectFragment = new SimpleBluetoothConnectFragment();
        simpleBluetoothConnectFragment.setController(this);

        simpleBluetoothConnectFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMainFragment();
                HC06_Connect();
            }
        });

    }

    private void initializeGPS(Context context) {
        locations = new LocationBuffer(5);

        gps = GPSTracker.Singleton.getInstance(context);
        gps.setListener(new LocationSource.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                GPS_OnUpdate(location);
            }
        });
        gps.getLastKnownLocation();
        gps.setUpdateRate(10);
    }

    private void initializeBluetooth(){
        bl_handler = new BluetoothHandler();

        connectionToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        bl_handler.setBluetoothStateListener(new BluetoothHandler.OnBluetoothStateListener() {
            @Override
            public void onStateChanged(int state) {

                switch (state) {
                    case BluetoothHandler.CONNECTING:
                        runToastOnGui(connectionToast, "Connecting...");
                        simpleBluetoothConnectFragment.lockButton(true);
                        break;
                    case BluetoothHandler.CONNECTED:
                        runToastOnGui(connectionToast, "Connection!");
                        simpleBluetoothConnectFragment.lockButton(false);
                        break;
                    case BluetoothHandler.FAILED_CONNECTING:
                        runToastOnGui(connectionToast, "Connection failed");
                        simpleBluetoothConnectFragment.lockButton(false);
                        HC06_Connect();
                        break;
                    case BluetoothHandler.DISCONNECTED:
                        runToastOnGui(connectionToast, "Connection disconnected");
                        simpleBluetoothConnectFragment.lockButton(false);
                        break;
                }
            }
        });
    }

    private void runToastOnGui(final Toast toast, final String text){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.setText(text);
                toast.show();
            }
        });
    }

    private void runToastOnGui(final Toast toast){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.show();
            }
        });
    }

    private final String BLUETOOTH_MODULE_NAME = "HC-06";

    private boolean checkBluetoothReady(){
        if (bluetoothAdapter == null){
            Toast.makeText(context, "No Bluetooth support", Toast.LENGTH_SHORT).show();
        }else{
            if (bluetoothAdapter.isEnabled()){
                if(bluetoothAdapter.isDiscovering()){
                    Toast.makeText(context, "Bluetooth is currently in device discovery process", Toast.LENGTH_SHORT).show();
                }else{
                    return true;
                }
            }
        }
        return false;
    }

    public void Bluetooth_Find_HC06(){

        if(!bluetoothAdapter.isEnabled() && bluetoothAdapter.isDiscovering()) {
            Toast.makeText(context, "Can't connect to Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

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
            Toast.makeText(context, "Not able to find HC-06", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Ready to connect HC-06", Toast.LENGTH_SHORT).show();
        }

    }


    private void Bluetooth_TryFind_HC06(){

        if(checkBluetoothReady()){
            Bluetooth_Find_HC06();

            StartSetupFragment();

        }else{
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, MainActivity.REQUEST_ENABLE_BT);
        }

    }

    private Toast connectionToast;

    public void HC06_Connect(){
        bl_handler.Connect(connectedDevice);
    }

    public void Bluetooth_Disconnect(){
        bl_handler.Disconnect();
    }

    public void Bluetooth_Send(String message){
        bl_handler.send(message);
    }

    private long prevTime = 0;
    private int numberOfTries;
    private boolean showedDestination;
    private String prevDestinationID = "";

    private void GPS_OnUpdate(final Location location){
        FileLog.d("GPS_UPDATE", "Location update with accuracy " + location.getAccuracy() + " m. " + "Latlng is " + location.getLatitude() + "," + location.getLongitude());
        locations.Add(location);

        if(directions.size() == 0)
            return;


//            LatLng currentPoint = new LatLng(location.getLatitude(), location.getLongitude());
//            final float dist= getDistance(currentPoint, getNextPoint());
//            final float estimatedTimeToDest = dist / location.getSpeed();

        final LatLng destination = directions.get(0).getEndLatLng();
        final float estimatedTimeToDest = locations.TimeToDestination(destination);
        final float dist = locations.DistanceToDestination(destination);
        final float avarageSpeed = locations.getAverageSpeed();


        if(estimatedTimeToDest < 10){

            if(prevDestinationID.equals(directions.get(0).getHtmlInstructions()))
                return;

            prevDestinationID = directions.get(0).getHtmlInstructions();

            if(directions.size() > 1) {
                signalNextManeuver();
                runToastOnGui(Toast.makeText(context, directions.get(1).getManeuver(), Toast.LENGTH_SHORT));
            }
            else runToastOnGui(Toast.makeText(context, "You have reached your destination.", Toast.LENGTH_SHORT));
        }

//            runToastOnGui(Toast.makeText(context,
//                    "Location update with accuracy " + location.getAccuracy() + " m. " +
//                            "Speed is " + location.getSpeed() + " m/s. " +
//                            "Distance to target is  " + dist + " m. " +
//                            "Estimated time to destination " + estimatedTimeToDest + "s.", Toast.LENGTH_LONG));

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                footerInfoFragment.updateValues(Math.round(dist) + " m.", estimatedTimeToDest + " s.", avarageSpeed + " m/s.");
            }
        });

        if(dist < 10){
            sendGoogleDirectionRequest(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    directions.get(directions.size() - 1).getEndLatLng()
            );
            prevTime = location.getTime();
        }

        if (prevTime == 0) {
            prevTime = location.getTime();
        } else {
            if (Calendar.getInstance().getTime().getTime() - prevTime > 15000) {
                //Update map
                sendGoogleDirectionRequest(
                        new LatLng(location.getLatitude(), location.getLongitude()),
                        directions.get(directions.size() - 1).getEndLatLng()
                );
                prevTime = location.getTime();
            }
        }

        if(location.getAccuracy() > 10 && numberOfTries < 4){
            numberOfTries++;
            gps.setUpdateRate(0);
        }else {
            numberOfTries = 0;

            if (dist < 50) {
                gps.setUpdateRate(0);
            } else if (dist < 100) {
                gps.setUpdateRate(2);
            } else if (dist < 200) {
                gps.setUpdateRate(5);
            } else {
                gps.setUpdateRate(10);
            }

        }



    }

    private void signalNextManeuver(){
        bl_handler.send(directions.get(1).BlueToothCode + "");

    }

    private float getDistance(LatLng p1, LatLng p2){
        return (float) (Math.sqrt(Math.pow(p1.latitude-p2.latitude, 2) + Math.pow(p1.longitude-p2.longitude, 2)) * 111.32 * 1000);
    }

    private LatLng getNextPoint(){
        return (directions == null || directions.size() < 2) ? null : directions.get(1).getStartLatLng();
    }

    public void StartSetupFragment(){
        replaceFragment(simpleBluetoothConnectFragment);
    }

    public void StartMainFragment(){
        replaceFragment(mainFragment);
        replaceFragment(autocompleteFragment, R.id.header_container);
        replaceFragment(googleMapFragment, R.id.content_container);
        replaceFragment(footerInfoFragment, R.id.footer_container);
    }

    public void replaceFragment(Fragment fragment){
        context.getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    public void replaceFragment(Fragment fragment, int id){
        context.getSupportFragmentManager().beginTransaction().replace(id, fragment).commit();
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

    ArrayList<Direction> directions = new ArrayList<>();

    private void onGoogleDirectionResult(ArrayList<Direction> dirs){
        directions = dirs;
        for(Direction d : dirs)
            Log.d("Controller", d.toString());

        googleMapFragment.drawNavigationPath(dirs);
    }

}
