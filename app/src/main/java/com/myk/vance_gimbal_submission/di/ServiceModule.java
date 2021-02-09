package com.myk.vance_gimbal_submission.di;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.myk.vance_gimbal_submission.data.service.RandomTextService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ServiceModule {

    @Singleton
    @Provides
    RandomTextService provideRandomTextService() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .baseUrl("https://www.randomtext.me/api/")
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(RandomTextService.class);
    }
}
