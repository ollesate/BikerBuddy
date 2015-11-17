package sjoholm.olof.gps_mc.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sjoholm.olof.gps_mc.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FooterInfoFragment extends Fragment {


    public FooterInfoFragment() {
        // Required empty public constructor
    }

    private TextView tvDestination;
    private TextView tvTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_footer_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        tvDestination = (TextView) view.findViewById(R.id.tvDistance);
        tvTime = (TextView) view.findViewById(R.id.tvTime);
    }

    public void updateValues(String distance, String time){
        tvDestination.setText(distance);
        tvTime.setText(time);
    }
}
