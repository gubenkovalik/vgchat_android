package ml.fastest.vgchat.gps.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import ml.fastest.vgchat.gps.GPS;
import ml.fastest.vgchat.gps.listeners.GPSResponseListener;

public class LocationService extends Service {

    private LocationManager locationManager;

    private LocationListener locationListener;

    private Timer timer;


    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("LOCSERV", "StartCommand");

        return START_REDELIVER_INTENT;
    }

    public void onStart(Intent intent, int startId) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener();
        timer = new Timer();
        timer.scheduleAtFixedRate(updater, 0, 30000);
    }

    private TimerTask updater = new TimerTask() {

        @Override
        public void run() {
            try {
                String provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
                locationManager.requestSingleUpdate(provider, locationListener, getMainLooper());
            } catch (SecurityException ignored) {

            }
        }
    };

    private class LocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(final Location location) {
            GPS.init(new GPSResponseListener() {
                @Override
                public void onSuccess(String response) {
                    Log.e("LocationServ", "Location sent! " + location.toString());
                }

                @Override
                public void onFailure(String response, int responseCode) {
                    Log.e("LocationServ", "Location cant be sent! ");
                }
            }).sendLocation(location, LocationService.this);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
