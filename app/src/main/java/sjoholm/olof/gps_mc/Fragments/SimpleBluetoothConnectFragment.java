package sjoholm.olof.gps_mc.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import sjoholm.olof.gps_mc.Controller;
import sjoholm.olof.gps_mc.R;

public class SimpleBluetoothConnectFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private Button connectButton;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    private Controller controller;


    public SimpleBluetoothConnectFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_simple_bluetooth_connect, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        connectButton = (Button) view.findViewById(R.id.bnConnect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(controller != null){
                    controller.HC06_Connect();
                }
            }
        });

        if(onClickListener != null){
            connectButton.setOnClickListener(onClickListener);
        }

    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;

        if(connectButton != null){
            connectButton.setOnClickListener(onClickListener);
        }
    }

    private View.OnClickListener onClickListener;

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void lockButton(boolean lock){
        connectButton.setClickable(!lock);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
