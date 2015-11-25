package sjoholm.olof.gps_mc;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by olof on 2015-11-24.
 */
public class LocationBuffer {

    private int size;
    private final int MAX_SIZE = 50;
    private Location [] locations = new Location[MAX_SIZE];
    private int head;
    private final long RESET_PERIOD = 30*1000; //30 seconds
    private long timeLastUpdate;

    public LocationBuffer(int size){
        this.size = size;

        if(size > MAX_SIZE){
            this.size = MAX_SIZE;
        }
    }

    private int mod(int i, int size){
        return (i+size)%size;
    }

    public void Add(Location location){
        if(location.getTime() - timeLastUpdate > RESET_PERIOD)
            reset();

        locations[head++%size] = location;

        timeLastUpdate = location.getTime();
    }

    private Location lastLocation(){
        return locations[mod(head-1, size)];
    }

    public int lastIndex(){
        return mod(head-1, size);
    }

    public float TimeToDestination(LatLng destination){
        float length = getDistance(new LatLng(lastLocation().getLatitude(), lastLocation().getLongitude()), destination);
        return length / getAverageSpeed();
    }

    private float getDistance(LatLng p1, LatLng p2){
        float actual = (float) (Math.sqrt(Math.pow(p1.latitude-p2.latitude, 2) + Math.pow(p1.longitude-p2.longitude, 2)) * 111.32 * 1000);
        return (actual - 20 > 0) ? actual - 20 : 0 ;
    }

    public float DistanceToDestination(LatLng destination){
        return getDistance(new LatLng(lastLocation().getLatitude(), lastLocation().getLongitude()), destination);
    }

    public float getAverageSpeed(){
        float sum = 0.0f;
        int n = 0;
        for (int i = 0; i < size; i++) {
            if(locations[i] == null)
                continue;
            n++;
            sum += locations[i].getSpeed();
        }
        return sum/n;
    }

    private void reset(){
        for (int i = 0; i < size; i++) {
            locations[i] = null;
        }
        head = 0;
    }

}

class LocationHandler{

    private LocationBuffer buffer;
    private ACCURACY currentAccuracy;
    private float VERY_LOW_ACCURACY = 100;
    private float LOW_ACCURACY = 50;
    private float AVERAGE_ACCURACY = 30;
    private float GOOOD_ACCURACY = 20;
    private float EXCELLENT_ACCURACY = 10;
    private int connectTries;
    private int MAX_CONNECT_TRIES;

    enum ACCURACY{
        VERY_BAD,
        BAD,
        AVERAGE,
        GOOD,
        EXCELLENT
    }

    public ACCURACY getAccuracy(){
        return currentAccuracy;
    }

    public LocationHandler(){
        buffer = new LocationBuffer(5);
    }

    public boolean Add(Location location){

        if(location.getAccuracy() < EXCELLENT_ACCURACY){
            currentAccuracy = ACCURACY.EXCELLENT;
        }
        else if(location.getAccuracy() < GOOOD_ACCURACY){
            currentAccuracy = ACCURACY.GOOD;
        }
        else if(location.getAccuracy() < AVERAGE_ACCURACY){
            currentAccuracy = ACCURACY.AVERAGE;
        }
        else if(location.getAccuracy() < LOW_ACCURACY){
            currentAccuracy = ACCURACY.BAD;
        }
        else if(location.getAccuracy() < VERY_LOW_ACCURACY){
            currentAccuracy = ACCURACY.VERY_BAD;
        }

        if(location.getAccuracy() > LOW_ACCURACY && connectTries < MAX_CONNECT_TRIES) {
            connectTries++;
            return false;
        }

        connectTries = 0;
        buffer.Add(location);
        return true;
    }

    public float getAverageSpeed(){
        return buffer.getAverageSpeed();
    }

    public float TimeToDestination(LatLng destination){
        return buffer.TimeToDestination(destination);
    }

    public float DistanceToDestination(LatLng destination){
        return buffer.DistanceToDestination(destination);
    }
}