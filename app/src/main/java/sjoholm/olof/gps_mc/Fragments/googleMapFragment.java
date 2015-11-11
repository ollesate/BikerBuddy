package sjoholm.olof.gps_mc.Fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import sjoholm.olof.gps_mc.Direction;
import sjoholm.olof.gps_mc.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class googleMapFragment extends Fragment {


    public googleMapFragment() {
        // Required empty public constructor
    }

    private MapView mapView;
    private GoogleMap map;

    private static final float DefaultZoomLevel = 4.0f;

    private static final LatLng DefaultLocation = new LatLng(55.213356, 13.381348);

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    private static final LatLngBounds BOUNDS_GREATER_SWEDEN = new LatLngBounds(
            new LatLng(55.213356, 13.381348), new LatLng(68.389067, 20.939941));

    private GoogleMap.OnMapClickListener mapClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_google_map, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        MapsInitializer.initialize(this.getActivity());

        return v;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        if(mapClickListener != null)
            map.setOnMapClickListener(mapClickListener);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void setMarker(LatLng latLng, String title) {
        map.clear();
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title));
    }

    public void setMapClickListener(GoogleMap.OnMapClickListener mapClickListener) {
        this.mapClickListener = mapClickListener;
        if(map != null)
            map.setOnMapClickListener(mapClickListener);
    }

    public void zoomMap(float value){
        CameraUpdate upd = CameraUpdateFactory.zoomTo(value);
        map.animateCamera(upd);
    }

    public void setLocationMap(LatLng latLng){
        CameraUpdate upd = CameraUpdateFactory.newLatLng(latLng);
        map.moveCamera(upd);
    }

    public void drawNavigationPath(ArrayList<Direction> list){
        PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);

        for(Direction dir : list){
            List<LatLng> lineList = PolyUtil.decode(dir.getPolyLineEncoded());
            rectLine.addAll(lineList);
        }

        map.addPolyline(rectLine);
    }
}
