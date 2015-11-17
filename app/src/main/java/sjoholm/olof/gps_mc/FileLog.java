package sjoholm.olof.gps_mc;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

/**
 * Created by olof on 2015-11-16.
 */
public class FileLog {

    private static final String DEBUG_LOG = "userdebuglog.txt";

    private static String LOG;

    public static void d(String tag, String text){
        Calendar c = Calendar.getInstance();

        Log.d(tag, text);
        LOG += "<- " + c.getTime().toString() + " -> " + tag + ": " + text + "\n";
    }

    public static void NewSession(Context context){
        LOG += "NEW SESSION \n";
    }

    public static void WriteToFile(Context context){
        LOG += "\n\n"; //Radbryten
        writeToFile(LOG, context);
    }

    public static void LogFromFile(Context context){
        Log.i("USER_DEBUG_LOG", readFromFile(context));
    }

    private static void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(DEBUG_LOG, Context.MODE_PRIVATE));
            outputStreamWriter.append(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    private static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(DEBUG_LOG);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString + "\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
