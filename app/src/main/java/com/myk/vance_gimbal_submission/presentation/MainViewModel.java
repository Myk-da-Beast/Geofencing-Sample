package com.myk.vance_gimbal_submission.presentation;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.Geofence;
import com.myk.vance_gimbal_submission.domain.GeofenceDomainModel;
import com.myk.vance_gimbal_submission.GibberishDomainModel;
import com.myk.vance_gimbal_submission.data.model.GibberishRemoteDataModel;
import com.myk.vance_gimbal_submission.data.service.RandomTextService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends ViewModel {
    private final RandomTextService service;

    private final MutableLiveData<String> output = new MutableLiveData<>();

    public LiveData<String> getOutput() {
        return output;
    }

    private final MutableLiveData<List<GeofenceDomainModel>> geofenceList = new MutableLiveData<>();

    public LiveData<List<GeofenceDomainModel>> getGeofenceList() {
        return geofenceList;
    }

    private List<GeofenceDomainModel> geofences = new ArrayList<>();

    @Inject
    public MainViewModel(RandomTextService service) {
        this.service = service;

        // default geofences
        geofences.add(new GeofenceDomainModel(34.0208, -118.3751, 1609.0f));
        geofences.add(new GeofenceDomainModel(34.0250, -118.3798, 1609.0f));
        geofences.add(new GeofenceDomainModel(34.0695, -118.2917, 1609.0f));
        geofenceList.setValue(geofences);
    }

    /**
     * Calls get gibberish endpoint and emits text to display
     */
    private void getGibberish() {
        service.getGibberish().enqueue(new Callback<GibberishRemoteDataModel>() {
            @Override
            public void onResponse(
                    @NonNull Call<GibberishRemoteDataModel> call,
                    @NonNull Response<GibberishRemoteDataModel> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    GibberishDomainModel gibberish = new GibberishDomainModel(response.body());
                    output.setValue(gibberish.getText());
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<GibberishRemoteDataModel> call,
                    @NonNull Throwable t
            ) {
                Log.e("MYK", "Error");
            }
        });
    }

    public void onGeofenceEventTriggered(Integer event) {
        if (event == Geofence.GEOFENCE_TRANSITION_ENTER) {
            getGibberish();
        } else if (event == Geofence.GEOFENCE_TRANSITION_EXIT) {
            output.setValue(null);
        } else {
            Log.e("MYK", String.format("Transition not handled: %s", event));
        }
    }

    /**
     * Takes users input and converts it to a domain object. Emits the updated list.
     *
     * @param latitude
     * @param longitude
     * @param radius
     */
    public void onAddButtonClicked(String latitude, String longitude, String radius) {
        try {
            Double lat = Double.parseDouble(latitude);
            Double lon = Double.parseDouble(longitude);
            Float rad = Float.parseFloat(radius);

            geofences.add(new GeofenceDomainModel(lat, lon, rad));
            geofenceList.setValue(geofences);
        } catch (Exception e) {
            Log.e("MYK", "Bad input");
        }
    }

    /**
     * Removes all geofences from the list and resets the output
     */
    public void onClearButtonClicked() {
        geofences = new ArrayList<>();
        geofenceList.setValue(geofences);
        output.setValue(null);
    }
}