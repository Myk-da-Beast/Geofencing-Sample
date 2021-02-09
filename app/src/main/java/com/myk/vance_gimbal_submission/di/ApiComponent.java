package com.myk.vance_gimbal_submission.di;

import com.myk.vance_gimbal_submission.presentation.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ServiceModule.class, ViewModelModule.class})
public interface ApiComponent {
    void inject(MainActivity activity);
}