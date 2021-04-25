package dev.div0.rgyandexmaps.map.yandex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import android.widget.TextView;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapWindow;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.SizeChangedListener;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import dev.div0.rgyandexmaps.R;
import dev.div0.rgyandexmaps.location.AppLocationListener;
import dev.div0.rgyandexmaps.location.ILocationChanged;
import dev.div0.rgyandexmaps.socket.AppSocket;
import dev.div0.rgyandexmaps.socket.ISocketSender;

public class MapActivity extends AppCompatActivity implements ILocationChanged, CameraListener {

    private MapView mapview;

    private String tag = "YandexMapActivity";

    private float zoom = 19.0f;
    private float azimuth = 0.0f;
    private float tilt = 0.0f;

    //private ISocketSender socketSender;
    //private UserLocationLayer userLocationLayer;

    private TextView coordinatesTextView;
    private LocationManager locationManager;
    private AppLocationListener locationListener;
    private Context mContext;
    private PlacemarkMapObject placemark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final MapActivity that = this;

        String key = getIntent().getExtras().getString("key");

        MapKitFactory.setApiKey(key);
        MapKitFactory.initialize(this);

        setContentView(R.layout.activity_map);

        coordinatesTextView = (TextView) findViewById(R.id.coordinatesTextView);
        log("onCreate");

        mapview = (MapView) findViewById(R.id.mapview);
        mContext=this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mapview.getMap().addCameraListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapview.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapview.onStart();
        locateDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, dev.div0.rgyandexmaps.Settings.locationUpdateInterval, dev.div0.rgyandexmaps.Settings.locationMinDistance, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, dev.div0.rgyandexmaps.Settings.locationUpdateInterval, dev.div0.rgyandexmaps.Settings.locationMinDistance, locationListener);
        }
    }

    private void locateDevice() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Log.d(tag, "gpsEnabled=" + gpsEnabled);

        SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensorAccelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //normal object
        Sensor sensorMagnetic = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); //null

        Log.d(tag, "sensorAccelerometer=" + sensorAccelerometer);
        Log.d(tag, "sensorMagnetic=" + sensorMagnetic);

        if (!gpsEnabled) {
            enableLocationSettings();
        } else {
            createLocationListener();
        }
    }


    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }
    private void createLocationListener(){
        locationListener = new AppLocationListener(this);
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        log("onLocationChanged location="+location);

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        //float currentZoom = mapview.getMap().getCameraPosition().getZoom();

        Point position = new Point(lat, lng);

        //log("lat:"+lat+" lng:"+lng+" zoom="+zoom);

        coordinatesTextView.setText("lat:"+lat+"   lng:"+lng+" zoom:"+zoom);

        mapview.getMap().move(
                new CameraPosition(position, zoom, azimuth, tilt),
                new Animation(Animation.Type.SMOOTH, 1),
                null);

        if(placemark == null){
            placemark = mapview.getMap().getMapObjects().addPlacemark(new Point(lat, lng), ImageProvider.fromAsset(this, "RGIcon.png"));
        }
        else{
            placemark.setGeometry(position);
        }
    }

    @Override
    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition, @NonNull CameraUpdateSource cameraUpdateSource, boolean b) {
        zoom = cameraPosition.getZoom();
        azimuth = cameraPosition.getAzimuth();
        tilt = cameraPosition.getTilt();
    }

    private void log(String data){
        Log.d(tag, data);

        if(AppSocket.getInstance().isConnected()){
            AppSocket.getInstance().sendLog(data);
        }
    }
}