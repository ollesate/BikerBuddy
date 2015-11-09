package sjoholm.olof.gps_mc;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class mapFragment extends Fragment {


    public mapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    private ListView lvDirections;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        lvDirections = (ListView) view.findViewById(R.id.lvDIrections);
        if (directions != null){
            arrayAdapter = new ArrayAdapter<Direction>(getActivity(), android.R.layout.simple_list_item_1, directions);
            lvDirections.setAdapter(arrayAdapter);
        }
    }


    private ArrayAdapter<Direction> arrayAdapter;
    private ArrayList<Direction> directions;

    public void setDirection(ArrayList<Direction> directions){
        this.directions = directions;
        if(lvDirections != null){
            arrayAdapter.clear();
            arrayAdapter.addAll(directions);
            lvDirections.setAdapter(arrayAdapter);
        }

    }

}
