package ml.fastest.vgchat.gps;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import jp.satorufujiwara.http.LightHttpClient;
import jp.satorufujiwara.http.Request;
import jp.satorufujiwara.http.Response;
import jp.satorufujiwara.http.gson.GsonConverterProvider;
import ml.fastest.vgchat.gps.listeners.GPSResponseListener;
import ml.fastest.vgchat.gps.models.Device;

/**
 * Created by Джен Кот on 28.09.2016.
 */

public class GPS {

    private GPSResponseListener gpsResponseListener;

    private GPS(GPSResponseListener gpsResponseListener) {
        this.gpsResponseListener = gpsResponseListener;
    }

    public static GPS init(GPSResponseListener gpsResponseListener) {

        if(gpsResponseListener == null) {
            throw new RuntimeException("GPSResponseListener must not be null");
        }

        return new GPS(gpsResponseListener);
    }

    public void registerDevice(final Context context) {

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String imei = tm.getDeviceId();

        final Device device = new Device(imei, imei);

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                LightHttpClient httpClient = new LightHttpClient();
                httpClient.setConverterProvider(new GsonConverterProvider());
                Request request = httpClient.newRequest()
                        .url("http://gps.jencat.ml/api/devices")
                        .post(device, Device.class)
                        .build();
                try {
                    Response<String> response = httpClient.newCall(request).execute();

                    String body = response.getBody();

                    switch(response.getCode()) {
                        case 200:
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            prefs.edit().putString("device_id", body).apply();

                            gpsResponseListener.onSuccess(body);
                            break;
                        default:
                            gpsResponseListener.onFailure(body, response.getCode());
                    }

                } catch (Exception e){

                    Log.e("ex", e.getLocalizedMessage());
                }
                return null;
            }
        }.execute();
    }

    private String getDeviceId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("device_id", "");
    }



    public void sendLocation(final Location location, final Context context) {

        final ml.fastest.vgchat.gps.models.Location loc = new ml.fastest.vgchat.gps.models.Location(
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy(),
                location.getAltitude()
        );

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                LightHttpClient httpClient = new LightHttpClient();
                httpClient.setConverterProvider(new GsonConverterProvider());
                Request request = httpClient.newRequest()
                        .url("http://gps.jencat.ml/api/devices/"+getDeviceId(context)+"/points")
                        .post(loc, ml.fastest.vgchat.gps.models.Location.class)
                        .build();
                try {
                    Response<String> response = httpClient.newCall(request).execute();

                    String body = response.getBody();

                    switch(response.getCode()) {
                        case 200:
                            gpsResponseListener.onSuccess(body);
                            break;
                        case 404:
                            GPS.init(new GPSResponseListener() {
                                @Override
                                public void onSuccess(String response) {
                                    sendLocation(location, context);
                                }

                                @Override
                                public void onFailure(String response, int responseCode) {

                                }
                            }).registerDevice(context);
                        default:
                            gpsResponseListener.onFailure(body, response.getCode());
                    }

                } catch (Exception e){

                    Log.e("ex", e.getLocalizedMessage());
                }
                return null;
            }
        }.execute();


    }
}
