package com.myk.vance_gimbal_submission;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;
import java.util.Observable;

/**
 * Receives geofence updates
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        ObservableObject.getInstance().updateValue(intent);
    }
}

