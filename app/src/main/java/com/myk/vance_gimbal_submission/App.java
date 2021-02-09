package com.myk.vance_gimbal_submission;

import android.app.Application;

import com.myk.vance_gimbal_submission.di.ApiComponent;
import com.myk.vance_gimbal_submission.di.DaggerApiComponent;

public class App extends Application {

    // creates dagger graph
    public ApiComponent appComponent = DaggerApiComponent.create();
}
