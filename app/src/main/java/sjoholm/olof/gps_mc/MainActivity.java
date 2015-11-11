package sjoholm.olof.gps_mc;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

//import com.google.android.gms.location.LocationListener;


public class MainActivity extends ActionBarActivity {

    private static final UUID SERVER_UUID = UUID.fromString("01001101-0000-1000-8000-00805f9b34fb");

    public final static int REQUEST_ENABLE_BT = 1;

    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controller = new Controller(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_ENABLE_BT){

            if (resultCode == Activity.RESULT_OK) {
                controller.Bluetooth_Find_HC06();
            } else {
                Toast.makeText(this, "App can not communicate without bluetooth", Toast.LENGTH_SHORT).show();
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //manager.removeUpdates(locationListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
