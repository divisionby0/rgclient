package dev.div0.rgyandexmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;

import dev.div0.rgyandexmaps.location.AppLocationListener;
import dev.div0.rgyandexmaps.location.ILocationChanged;
import dev.div0.rgyandexmaps.map.yandex.MapActivity;
import dev.div0.rgyandexmaps.socket.AppSocket;
import dev.div0.rgyandexmaps.socket.ISocketListenerCallback;
import dev.div0.rgyandexmaps.socket.ISocketSender;

public class MainActivity extends AppCompatActivity implements ISocketListenerCallback{

    private String userId = "AndroidUser_0";
    private String tag = "startPoint";

    //private LocationManager locationManager;

    //private Parcelable socketSender;
    private String yandexMapsAPIKey;
    private Double lat;
    private Double lng;

    //private AppLocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView infoTextView = (TextView)findViewById(R.id.infoTextView);
        infoTextView.setText("Connecting...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        createSocket();
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            //return;
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
        }
    }
     */

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    private void createSocket() {
        Log.d(tag, "createSocket");
        final MainActivity that = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppSocket.getInstance().setUserId(userId);
                AppSocket.getInstance().setSocketListenerCallback(that);
                AppSocket.getInstance().init();
                //socketSender = new AppSocket(userId, that);
            }
        });
    }

    private void startMap(){
        if(!yandexMapsAPIKey.isEmpty() && yandexMapsAPIKey != null ){
            Log.d(tag, "start map");
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("key", yandexMapsAPIKey);
            startActivity(intent);
        }
    }

    @Override
    public void onSocketConnected() {
        Log.d(tag, "socket connected");
    }

    @Override
    public void onSocketDisconnected() {
        Log.d(tag, "socket disconnected");
    }

    @Override
    public void onSocketConnectError(String error) {
        Log.d(tag, "socket error: "+error);
    }

    @Override
    public void onYandexAPIKey(String key) {
        Log.d(tag, "onYandexAPIKey : "+key);
        yandexMapsAPIKey = key;
        startMap();
    }
}