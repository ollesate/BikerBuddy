package sjoholm.olof.gps_mc;

import android.content.Context;

/**
 * Created by w1 on 2015-10-05.
 */
public class MyGPSTracker {

    private static GPSTracker gpsTracker = null;

    private MyGPSTracker (Context context){
        gpsTracker = new GPSTracker(context);
    }

    public static GPSTracker getInstance(Context context) {

        if(gpsTracker == null){
            gpsTracker = new GPSTracker(context);
        }

        return gpsTracker;
    }

}
