package dev.div0.rgyandexmaps.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class AppLocationListener implements LocationListener {

    private ILocationChanged changedCallbackProvider;

    public AppLocationListener(ILocationChanged _changedCallbackProvider){
        Log.d("AppLocationListener", "create");
        changedCallbackProvider = _changedCallbackProvider;
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d("AppLocationListener", "onLocationChanged location="+location);
        changedCallbackProvider.onLocationChanged(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("AppLocationListener", "onProviderDisabled");
        //checkEnabled();
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Log.d("AppLocationListener", "onProviderEnabled");
        //checkEnabled();
        //showLocation(locationManager.getLastKnownLocation(provider));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("AppLocationListener", "onStatusChanged. provider="+provider);
        /*
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            tvStatusGPS.setText("Status: " + String.valueOf(status));
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            tvStatusNet.setText("Status: " + String.valueOf(status));
        }
        */
    }
}
