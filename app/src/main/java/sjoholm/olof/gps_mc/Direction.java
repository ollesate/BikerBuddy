package sjoholm.olof.gps_mc;

import android.content.ContentValues;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by w1 on 2015-09-23.
 */
public class Direction{

    private String distanceText;
    private int distanceValue;
    private String maneuver;
    private String htmlInstructions;
    private String polyLineEncoded;

    public LatLng getStartLatLng() {
        return startLatLng;
    }

    public void setStartLatLng(LatLng latLng) {
        this.startLatLng = latLng;
    }

    private LatLng startLatLng;

    public LatLng getEndLatLng() {
        return endLatLng;
    }

    public void setEndLatLng(LatLng endLatLng) {
        this.endLatLng = endLatLng;
    }

    private LatLng endLatLng;

    //private ContentValues val = new ContentValues().

    public int M_TURN_LEFT = 0;
    public int M_TURN_RIGHT = 1;
    public int M_Roundabout_Right = 2;
    public int M_Roundabout_Left = 3;
    public int M_Merge = 4;
    public int M_Straight = 5;
    public int M_Ramp_Right = 6;
    public int M_Ramp_Left = 7;
    public int M_Fork_Right = 8;
    public int M_FORK_LEFT = 9;
    public int M_KEEP_LEFT = 10;
    public int M_KEEP_RIGHT = 11;

    public int BlueToothCode;
    //"html_instructions" : "I rondellen tar du avfart \u003cb\u003e3:e\u003c/b\u003e in på infart \u003cb\u003eInre Ringvägen\u003c/b\u003e mot \u003cb\u003eGöteborg/Kalmar/Hamnen\u003c/b\u003e",

    public String getManeuver() {
        return maneuver;
    }

    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }

    public int getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(int distanceValue) {
        this.distanceValue = distanceValue;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }

    @Override
    public String toString() {
        if(maneuver.equals("drive-straight"))
            return "drive straight for" + distanceText + ".";
        else
            return "drive " + distanceText + ", then " +maneuver;
    }

    public Direction(JSONObject jsonObject){
        try {

            htmlInstructions = jsonObject.getString("html_instructions");

            //Hämta coords
            double lat = jsonObject.getJSONObject("start_location").getDouble("lat");
            double lng = jsonObject.getJSONObject("start_location").getDouble("lng");
            startLatLng = new LatLng(lat, lng);

            lat = jsonObject.getJSONObject("end_location").getDouble("lat");
            lng = jsonObject.getJSONObject("end_location").getDouble("lng");
            endLatLng = new LatLng(lat, lng);

            if(jsonObject.has("maneuver")) {

                maneuver = jsonObject.getString("maneuver");

            }

            else{

                maneuver = "drive-straight";

            }
            
            switch (maneuver){
                case "turn-left":
                    BlueToothCode = 0;
                    break;
                case "turn-right":
                    BlueToothCode = 1;
                    break;
                case "roundabout-right":
                    BlueToothCode = 2;
                    break;
                case "roundabout-left":
                    BlueToothCode = 3;
                    break;
                case "merge":
                    BlueToothCode = 4;
                    break;
                case "straight":
                    BlueToothCode = 5;
                    break;
                case "drive-straight":
                    BlueToothCode = 5;
                    break;
                case "ramp-right":
                    BlueToothCode = 6;
                    break;
                case "ramp-left":
                    BlueToothCode = 7;
                    break;
                case "fork-right":
                    BlueToothCode = 8;
                    break;
                case "fork-left":
                    BlueToothCode = 9;
                    break;
                case "keep-left":
                    BlueToothCode = 10;
                    break;
                case "keep-right":
                    BlueToothCode = 11;
                    break;
                default:
                    Log.d("Direction", "Manuever not catched: " + maneuver);
                    break;
            }

//            if(maneuver.equals("roundabout-right") || maneuver.equals("roundabout-left")){
//                Log.d("Dir", ">>>roundabout<<<");
//                Log.d("Dir", htmlInstructions);
//                String imp = htmlInstructions.substring(
//                        htmlInstructions.indexOf("the roundabout"),
//                        htmlInstructions.indexOf("onto"));
//
//
//
//                String number = ""+imp.charAt(imp.indexOf(">")+1);
//                maneuver += "-" + number;
//            }
            Log.d("Dir", maneuver);

            polyLineEncoded = jsonObject.getJSONObject("polyline").getString("points");

            distanceValue = jsonObject.getJSONObject("distance").getInt("value");

            distanceText = jsonObject.getJSONObject("distance").getString("text");



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getHtmlInstructions() {
        return htmlInstructions;
    }

    public void setHtmlInstructions(String htmlInstructions) {
        this.htmlInstructions = htmlInstructions;
    }

    public String getPolyLineEncoded() {
        return polyLineEncoded;
    }

    public void setPolyLineEncoded(String polyLineEncoded) {
        this.polyLineEncoded = polyLineEncoded;
    }
}