package com.servizi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.internet.InviaPosizione;




/*
public class BackgroundService extends Service {
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "BackgroundService";
    private final String notificationIdChannel = "channel_backgroundservice";
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private NotificationManager notificationManager = null;

    private final int LOCATION_INTERVAL = 2*60*1000; //1000 = 1 sec
    private final double LOCATION_DISTANCE = 0.01;
    private static final String TAG_BOOT_EXECUTE_SERVICE = "BOOT_BROADCAST_BACKGROUND_SERVICE";

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class LocationListener implements android.location.LocationListener {
        private Location lastLocation = null;
        private final String TAG = "LocationListener";
        private Location mLastLocation;

        LocationListener(String provider) {
            mLastLocation = new Location(provider);
            Log.i(TAG, "Creato!");
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            Log.i(TAG, "LocationChanged: "+location);
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            new InviaPosizione().execute( (Object)getApplicationContext(), (Object) lat, (Object)lng);
        }

        @Override
        public void onProviderDisabled(String provider) { Log.e(TAG, "onProviderDisabled: " + provider); }

        @Override
        public void onProviderEnabled(String provider) { Log.e(TAG, "onProviderEnabled: " + provider); }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { Log.e(TAG, "onStatusChanged: " + status); }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //return START_NOT_STICKY;
        return START_STICKY ;

    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        startTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy - inizio");
        if (mLocationManager != null) {
            try { mLocationManager.removeUpdates(mLocationListener); }
            catch (Exception ex) { Log.i(TAG, "fail to remove location listners, ignore", ex); }
        }

        Log.d(TAG, "notificationManager>"+notificationManager);

        if(notificationManager!=null) {
            Log.d(TAG, "cancellazione notifica");
            notificationManager.deleteNotificationChannel(notificationIdChannel);
        }
        Log.d(TAG, "onDestroy - fine");
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) { mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE); }
    }

    public void startTracking() {
        startForeground(12345678, getNotification());
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.NETWORK_PROVIDER);

        try { mLocationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, (float)LOCATION_DISTANCE, mLocationListener ); }
        catch (java.lang.SecurityException ex) { Log.e(TAG, "fail to request location update, ignore", ex); }
        catch (IllegalArgumentException ex) { Log.e(TAG, "gps provider does not exist " + ex.getMessage()); }
        Log.d(TAG, "startTracking");
    }

    public void stopTracking() {
        Log.d(TAG, "stopTracking");
        this.onDestroy();
    }

    private Notification getNotification() {
        NotificationChannel channel = new NotificationChannel(notificationIdChannel, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), notificationIdChannel).setAutoCancel(true);
        return builder.build();
    }

    public class LocationServiceBinder extends Binder { public BackgroundService getService() { return BackgroundService.this; } }
}
*/