package ml.fastest.vgchat;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import ml.fastest.vgchat.gps.GPS;
import ml.fastest.vgchat.gps.listeners.GPSResponseListener;
import ml.fastest.vgchat.gps.services.LocationService;

/**
 * Created by Джен Кот on 28.09.2016.
 */

public class JenCat extends Application {
    public void onCreate() {
        super.onCreate();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        if(!sp.contains("device_id")) {
            GPS.init(new GPSResponseListener() {
                @Override
                public void onSuccess(String response) {
                    startService(new Intent(JenCat.this, LocationService.class));
                }

                @Override
                public void onFailure(String response, int responseCode) {
                    Toast.makeText(JenCat.this, "Cant register device", Toast.LENGTH_LONG).show();
                }
            }).registerDevice(this);
        }

    }
}
