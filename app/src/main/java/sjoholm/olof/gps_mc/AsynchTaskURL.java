package sjoholm.olof.gps_mc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by w1 on 2015-09-23.
 */
public class AsynchTaskURL extends AsyncTask<String, String, String> {

//        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
    private InputStream inputStream = null;
    private String result = "";
    private String destination;
    private String origin;

    public AsynchTaskURL(OnResultListener listener){
        this.onResultListener = listener;
    }

    protected void onPreExecute() {
//            progressDialog.setMessage("Downloading your data...");
//            progressDialog.show();
//            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                public void onCancel(DialogInterface arg0) {
//                    AsynchTaskURL.this.cancel(true);
//                }
//            });
    }

    @Override
    protected String doInBackground(String... params) {

        String url_select = "";

        for(int i = 0; i < params.length; i++){
            url_select += params[i];
        }

        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

        try {
            // Set up HTTP post

            // HttpClient is more then less deprecated. Need to change to URLConnection
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url_select);
            httpPost.setEntity(new UrlEncodedFormEntity(param));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            // Read content & Log
            inputStream = httpEntity.getContent();
        } catch (UnsupportedEncodingException e1) {
            Log.e("UnsupportedEncodingion", e1.toString());
            e1.printStackTrace();
        } catch (ClientProtocolException e2) {
            Log.e("ClientProtocolException", e2.toString());
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            Log.e("IllegalStateException", e3.toString());
            e3.printStackTrace();
        } catch (IOException e4) {
            Log.e("IOException", e4.toString());
            e4.printStackTrace();
        }
        // Convert response to string using String Builder
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
            StringBuilder sBuilder = new StringBuilder();

            String line;
            while ((line = bReader.readLine()) != null) {
                sBuilder.append(line + "\n");
            }

            inputStream.close();
            result = sBuilder.toString();

        } catch (Exception e) {
            Log.e("StringBuildieredReader", "Error converting result " + e.toString());
        }
        return result;
    }


    protected void onPostExecute(String s) {
        Log.d("URL", result);
        //parse JSON data
        try {
            JSONObject rootObject = new JSONObject(result);
            JSONArray routesArray = rootObject.getJSONArray("routes");
            JSONArray legsArray = routesArray.getJSONObject(0).getJSONArray("legs");
            JSONArray stepsArray = legsArray.getJSONObject(0).getJSONArray("steps");
            ArrayList<Direction> arrayList = new ArrayList<Direction>();

            for(int i = 0; i < stepsArray.length(); i++){

                    //Log.d("URL", stepsArray.getJSONObject(i).getString("maneuver"));
                    arrayList.add(new Direction(stepsArray.getJSONObject(i)));

            }
            onResultListener.OnResult(arrayList);

            //this.progressDialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSONException", "Error: " + e.toString());
        } // catch (JSONException e)
    } // protected void onPostExecute(Void v)

    private OnResultListener onResultListener;

    public interface OnResultListener{
        void OnResult(ArrayList<Direction> dirs);
    }

}


