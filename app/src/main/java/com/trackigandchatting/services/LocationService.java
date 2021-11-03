package com.trackigandchatting.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.trackigandchatting.App;
import com.trackigandchatting.R;
import com.trackigandchatting.models.UserLocation;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private final static long UPDATE_INTERVAL = 4 * 1000; //updating time 4s
    private final static long FASTEST_INTERVAL = 2000; // if a location is available sooner you can get it (i.e. another app is using the location services).

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        startForeground(1, getNotification()); // start notification
        //Give android.permission.FOREGROUND_SERVICE in manifest

    }

    private Notification getNotification() {

        NotificationCompat.Builder notificationBuilder = null;

        notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_phone_android_24) // small icon on top
                        .setContentTitle("Location Notification")
                        .setContentText("Location Service is running in the background")
                        .setAutoCancel(true);

        return notificationBuilder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getLocation();
        return START_NOT_STICKY; // service stop when app is closed.
    }

    private void getLocation() {

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL); // location update time
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL); // location update time

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            stopSelf(); //stop service if permission is not granted
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                saveDataInFirestore(locationResult);


            }
        }, Looper.myLooper()); //loop continuously run, until thread get destroy
    }

    private void saveDataInFirestore(LocationResult locationResult) {


        try {
            if (FirebaseAuth.getInstance().getUid() != null) { // when user close the app, uId() gets null. if its null we stop this service

                Map<String, Object> userLocation = new HashMap<>();
                userLocation.put("geo_point", (new GeoPoint(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude())));
                FirebaseFirestore.getInstance().collection("UserCurrentLocation").document(FirebaseAuth.getInstance().getUid()).update(userLocation).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
            } else {
                stopSelf();
            }
        }catch (NullPointerException e){ // when user close the app, uId() gets null. if its null we stop this service
            stopSelf();
        }
    }
}
