package com.myk.vance_gimbal_submission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

/**
 * Encapsulates location and geofencing logic and behaviors
 */
public class GeofenceHelper {

    private GeofencingClient geofencingClient;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private PendingIntent pendingIntent;
    private Context context;
    GeofenceHelperCallback callback;

    /**
     * Starts monitoring geofences and also starts requesting location updates. Geofences don't
     * seem to trigger if the device has not been publishing location results, so for the context
     * of this app we are requesting locations.
     *
     * @param context
     * @param geofencePendingIntent
     * @param geofences - geofences to monitor
     * @param callback - so the caller can respond to success/failure
     */
    @SuppressLint("MissingPermission")
    public void start(
            Context context,
            PendingIntent geofencePendingIntent,
            List<Geofence> geofences,
            GeofenceHelperCallback callback
    ) {
        geofencingClient = LocationServices.getGeofencingClient(context);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        pendingIntent = geofencePendingIntent;
        this.context = context;
        this.callback = callback;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d("MYK", location.toString());
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                createLocationRequest(),
                locationCallback,
                Looper.getMainLooper()
        );

        if (!geofences.isEmpty()) {
            addGeofences(geofences);
        }
    }

    @SuppressLint("MissingPermission")
    public void addGeofences(List<Geofence> geofences) {
        if (pendingIntent == null) {
            Log.e("MYK", "Pending intent is null. Make sure you call start before adding geofences.");
            return;
        }
        geofencingClient.addGeofences(getGeofencingRequest(geofences), pendingIntent)
                .addOnSuccessListener((Activity) context, aVoid -> {
                    Log.d("MYK", "Geofences added!");
                    callback.onSuccess();
                })
                .addOnFailureListener((Activity) context, e -> {
                    // Failed to add geofences
                    Log.e("MYK", "Failed to add geofences");
                    callback.onFailure();
                });
    }

    public void stop() {
        geofencingClient.removeGeofences(pendingIntent);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }
}

