package sjoholm.olof.gps_mc;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class autocompleteFragment extends Fragment
        implements GoogleApiClient.OnConnectionFailedListener{


    private static String TAG = "autoComplFrag";
    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView mAutocompleteView;

    private TextView mPlaceDetailsText;

    private TextView mPlaceDetailsAttribution;

    private Button bSwitch;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    private static final LatLngBounds BOUNDS_GREATER_SWEDEN = new LatLngBounds(
            new LatLng(55.213356, 13.381348), new LatLng(68.389067, 20.939941));


    private static final float ZoomInLevel = 7.0f;

    private GPSTracker gps;

    public autocompleteFragment() {
        gps = MyGPSTracker.getInstance(getActivity());
        gps.setListener(new LocationSource.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(asyncDestString != ""){
                    startAsynch(asyncDestString);
                    Toast.makeText(getActivity(), "Updated position", Toast.LENGTH_SHORT).show();
                }
            }
        });

        initializeBluetooth();
    }

    private BluetoothDevice mBluetoothDevice;
    private BluetoothHandler bluetoothHandler;

    private void initializeBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {

                mBluetoothDevice = device;

                bluetoothHandler = new BluetoothHandler(mBluetoothDevice);

                Log.d("bluetooth", device.getName());

                break;

            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(googleMapFragment == null){
            googleMapFragment = new googleMapFragment();
            googleMapFragment.setMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    startDestination(latLng);
                }
            });
        }
        if(mapFragment == null){
            mapFragment = new mapFragment();
        }
        setLayoutToFragment(witchFragment);
        return inflater.inflate(R.layout.fragment_autocomplete, container, false);
    }

    private googleMapFragment googleMapFragment;
    private mapFragment mapFragment;

    private int witchFragment = 1;

    private int testLength = 0;

    private void toggleFragments(){

        testLength++;
        if(testLength == 4)
            testLength = 0;
        bluetoothHandler.send("L:"+testLength);
        Log.d("bluetooth", "L:"+testLength);

        if(witchFragment == 0){
            setLayoutToFragment(1);
            savedStateMapFragment = getActivity().getSupportFragmentManager().saveFragmentInstanceState(mapFragment);
            witchFragment = 1;
            //bluetoothHandler.send("0");
        }
        else{
            setLayoutToFragment(0);
            savedStateGoogleMap = getActivity().getSupportFragmentManager().saveFragmentInstanceState(googleMapFragment);
            witchFragment = 0;
            //bluetoothHandler.send("1");
        }
    }

    private void setDestinationMarker(LatLng latLng){
        googleMapFragment.setMarker(latLng, "Destination");
    }

    private SavedState savedStateGoogleMap = null, savedStateMapFragment = null;

    private void setLayoutToFragment(int layout){
        Fragment f = null;

        if(layout == 1){
            f = googleMapFragment;
            if(savedStateGoogleMap != null){
                googleMapFragment.setInitialSavedState(savedStateGoogleMap);
            }
        }else{
            f = mapFragment;
            if(savedStateMapFragment!= null){
                mapFragment.setInitialSavedState(savedStateMapFragment);
            }
        }

        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.midcontainer, f).commit();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        bSwitch = (Button) view.findViewById(R.id.bSwitch);
        bSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFragments();
            }
        });


        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .build();

        mGoogleApiClient.connect();

        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        mAutocompleteView = (AutoCompleteTextView)
                view.findViewById(R.id.autocomplete_places);

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        // Retrieve the TextViews that will display details and attributions of the selected place.
        mPlaceDetailsText = (TextView) view.findViewById(R.id.place_details);
        mPlaceDetailsAttribution = (TextView) view.findViewById(R.id.place_attribution);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(getActivity(), android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_GREATER_SWEDEN, null);
        mAutocompleteView.setAdapter(mAdapter);

    }


    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getActivity(), "Clicked: " + item.description,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);

            hideSoftKeyboard();
        }
    };

    private void hideSoftKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            // Format details of the place for display and show it in a TextView.
            mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
                    place.getId(), place.getAddress(), place.getPhoneNumber(),
                    place.getWebsiteUri()));

            // Display the third party attributions if set.
            final CharSequence thirdPartyAttribution = places.getAttributions();
            if (thirdPartyAttribution == null) {
                mPlaceDetailsAttribution.setVisibility(View.GONE);
            } else {
                mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
                mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
            }

            Log.i(TAG, "Place details received: " + place.getName());
            startNextFragment(place);
            places.release();
        }
    };

    private void startDestination(LatLng latLng){
        Log.d("Dir", "starting destination");
        setDestinationMarker(latLng);
        String latLngStr = latLng.latitude + "," + latLng.longitude;
        directionsBundle = new Bundle();
        directionsBundle.putString("destination", latLngStr);
        //bundle.putString("destination", "place_id:" + place.getId());
        mapFragment.setArguments(directionsBundle);

        googleMapFragment.setLocationMap(latLng);
        googleMapFragment.zoomMap(ZoomInLevel);

        startAsynch(latLngStr);
    }

    private String asyncDestString = "";
    private ArrayList<Direction> currentDirections;

    public void startAsynch(String latLngStringDirection ) {
        asyncDestString = latLngStringDirection;
        AsynchTaskURL asynchTaskURL = new AsynchTaskURL(new AsynchTaskURL.OnResultListener() {
            @Override
            public void OnResult(ArrayList<Direction> dirs) {
                mapFragment.setDirection(dirs);
                googleMapFragment.drawNavigationPath(dirs);
                currentDirections = dirs;
            }
        });

        GPSTracker gps =  MyGPSTracker.getInstance(getActivity());
        String orig = gps.getLatitude()+","+gps.getLongitude();
        String origin = orig;

        String asynchString = "https://maps.googleapis.com/maps/api/directions/json?origin="+origin+"&destination="+asyncDestString+"&key=AIzaSyAKaCJA-cFhcyLqi0oeF7Oag6BsOnNPD-s";
        String asynchStringWOServer = "https://maps.googleapis.com/maps/api/directions/json?origin="+origin+"&destination="+asyncDestString;
        Log.d("Direction", asynchStringWOServer);
        asynchTaskURL.execute(asynchStringWOServer);
    }


    private class SimulateTrip implements Runnable{

        private ArrayList<Direction> dirs;
        public SimulateTrip(ArrayList<Direction> dirs){
            this.dirs = dirs;
        }

        @Override
        public void run() {
            for(Direction dir: dirs){
                bluetoothHandler.send(dir.BlueToothCode + "");
                Log.d("Direction", dir.getManeuver());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    private Bundle directionsBundle;

    private void startNextFragment(Place place) {

        googleMapFragment.setLocationMap(place.getLatLng());
        googleMapFragment.zoomMap(ZoomInLevel);

        String latLng = place.getLatLng().latitude+","+place.getLatLng().longitude;
        setDestinationMarker(place.getLatLng());
        directionsBundle = new Bundle();
        directionsBundle.putString("destination", latLng);
        //bundle.putString("destination", "place_id:" + place.getId());
        directionsBundle.putString("destinationText", place.getAddress().toString());
        mapFragment = new mapFragment();
        mapFragment.setArguments(directionsBundle);
        startAsynch(latLng);
        //launchTurnbyTurnNavigation(place);
    }

    private void drawNavigationOnMap(ArrayList<Direction> list){




    }

    private void launchTurnbyTurnNavigation(Place destination) {
//        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination.getAddress().toString()));
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//        mapIntent.setPackage("com.google.android.apps.maps");
//        startActivity(mapIntent);

        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)", gps.getLatitude(), gps.getLongitude(), "Home Sweet Home", destination.getLatLng().latitude, destination.getLatLng().longitude, "Where the party is at");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(getActivity(),
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

}
