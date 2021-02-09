package com.myk.vance_gimbal_submission.presentation;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.myk.vance_gimbal_submission.App;
import com.myk.vance_gimbal_submission.GeofenceBroadcastReceiver;
import com.myk.vance_gimbal_submission.GeofenceHelper;
import com.myk.vance_gimbal_submission.GeofenceHelperCallback;
import com.myk.vance_gimbal_submission.ObservableObject;
import com.myk.vance_gimbal_submission.R;
import com.myk.vance_gimbal_submission.di.ViewModelFactory;
import com.myk.vance_gimbal_submission.domain.GeofenceDomainModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.inject.Inject;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;


public class MainActivity extends AppCompatActivity implements Observer {

    @Inject
    ViewModelFactory viewModelFactory;
    private MainViewModel viewModel;

    List<Geofence> geofences = new ArrayList<>();
    List<String> geofenceStrings = new ArrayList<>();
    SimpleAdapter adapter;

    private TextView output;

    PendingIntent geofencePendingIntent = null;
    GeofenceHelper geofenceHelper = new GeofenceHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // allow dagger to inject this class with dependencies
        ((App) getApplicationContext())
                .appComponent
                .inject(this);

        output = findViewById(R.id.output);
        Button addButton = findViewById(R.id.add_button);
        Button clearButton = findViewById(R.id.clear_button);
        RecyclerView recycler = findViewById(R.id.recycler);
        adapter = new SimpleAdapter(geofenceStrings.toArray(new String[2]));

        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        ObservableObject.getInstance().addObserver(this);

        // display output to user
        viewModel.getOutput().observe(this, text -> {
            if (text == null || text.isEmpty()) {
                output.setText(R.string.default_text);
            } else {
                output.setText(text);
            }
        });

        // add geofences and update recyclerview
        viewModel.getGeofenceList().observe(this, geofences -> {
            if (geofences.isEmpty()) {
                geofenceHelper.stop();
                this.geofences = new ArrayList<>();
                adapter.updateAdapter(new String[0]);
            } else {
                String[] list = new String[geofences.size()];
                for (int i = 0; i < geofences.size(); i++) {
                    list[i] = geofences.get(i).stringify();
                }
                adapter.updateAdapter(list);
                populateGeofences(geofences);
                geofenceHelper.addGeofences(this.geofences);
            }
        });

        // start monitoring geofences if permissions granted, otherwise ask for permissions
        if (checkPermissions()) {
            startGeofenceMonitoring();
        } else if (shouldShowRequestPermissionRationale("Gimme permissions pls")) {
            Toast.makeText(this, "Yoggsly", Toast.LENGTH_LONG).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, 101
                );
            }
        }

        recycler.setAdapter(adapter);

        // create and display dialog for taking input
        addButton.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.add_geofence_dialog);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);
            Button done = dialog.findViewById(R.id.done_button);

            EditText latitude = dialog.findViewById(R.id.latitude_edit_text);
            EditText longitude = dialog.findViewById(R.id.longitude_edit_text);
            EditText radius = dialog.findViewById(R.id.radius_edit_text);
            dialog.show();

            done.setOnClickListener(v -> {
                Log.d("MYK", latitude.getText().toString());
                Log.d("MYK", longitude.getText().toString());
                Log.d("MYK", radius.getText().toString());
                dialog.dismiss();
                viewModel.onAddButtonClicked(
                        latitude.getText().toString(),
                        longitude.getText().toString(),
                        radius.getText().toString()
                );
            });
        });

        clearButton.setOnClickListener(view -> {
            viewModel.onClearButtonClicked();
        });
    }

    /**
     * Overridden from [Observer]. Called every time GeofenceBroadcastReceiver receives an update
     */
    @Override
    public void update(Observable observable, Object o) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent((Intent) o);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e("MYK", errorMessage);
            return;
        }

        // get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // forward to viewmodel for handling
        viewModel.onGeofenceEventTriggered(geofenceTransition);
    }

    /**
     * Converts domain object input into [Geofence] objects for use with [GeofenceClient]
     *
     * @param geofenceDomainModels input to be converted to [Geofence] objects
     */
    private void populateGeofences(List<GeofenceDomainModel> geofenceDomainModels) {
        for (int i = 0; i < geofenceDomainModels.size(); i++) {
            Double latitude = geofenceDomainModels.get(i).getLatitude();
            Double longitude = geofenceDomainModels.get(i).getLongitude();
            Float radius = geofenceDomainModels.get(i).getRadius();

            geofences.add(new Geofence.Builder()
                    .setRequestId(Integer.toString(i))
                    .setCircularRegion(
                            latitude,
                            longitude,
                            radius
                    )
                    .setLoiteringDelay(1000)
                    .setExpirationDuration(1000000000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .build());

        }
    }

    // starts monitoring geofences
    private void startGeofenceMonitoring() {
        geofenceHelper.start(
                this,
                getGeofencePendingIntent(),
                geofences,
                new GeofenceHelperCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure() {
                    }
                }
        );
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.
                        FLAG_UPDATE_CURRENT
        );
        return geofencePendingIntent;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGeofenceMonitoring();
        } else {
            Log.e("MYK", "User denied permissions");
        }
    }

    /**
     * Checks if the user has given the required permissions
     *
     * @return true if the user has given all permissions, false otherwise.
     */
    private boolean checkPermissions() {
        boolean hasPermission = true;

        int result = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        );
        if (result == PERMISSION_DENIED) {
            hasPermission = false;
        }

        // ACCESS_BACKGROUND_LOCATION is only required on API 29 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            result = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            );
            if (result == PERMISSION_DENIED) {
                hasPermission = false;
            }
        }

        return hasPermission;
    }
}