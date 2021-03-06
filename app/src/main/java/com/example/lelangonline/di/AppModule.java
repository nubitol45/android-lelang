package com.example.lelangonline.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.lelangonline.database.NewsDao;
import com.example.lelangonline.database.NewsDatabase;
import com.example.lelangonline.database.saved.SavedDao;
import com.example.lelangonline.network.main.MainApi;
import com.example.lelangonline.utils.Constants;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

@Module
public class AppModule {

    @Provides
    @Singleton
    static Retrofit provideRetrofit(OkHttpClient client){
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    @Singleton
    @Provides
    static HttpLoggingInterceptor provideOkHttpInterceptor (){
        return new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Singleton
    @Provides
    static OkHttpClient provideOkHttp(HttpLoggingInterceptor logging){
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    @Singleton
    @Provides
    static CompositeDisposable provideDisposable() {
        return new CompositeDisposable();
    }


    @Provides
    @Singleton
    static RequestManager provideGlide(Application application){
        return Glide.with(application);
    }

    @Singleton
    @Provides
    static NewsDatabase provideRoomDatabase(Application application){
        return Room.databaseBuilder(application,
                NewsDatabase.class, Constants.DATABASE_NAME)
                .build();
    }

    @Singleton
    @Provides
    static SharedPreferences.Editor provideEditSharedPref(Application application){
        return application.getSharedPreferences(Constants.USERID_PREFS, MODE_PRIVATE).edit();
    }


    @Singleton
    @Provides
    static SharedPreferences provideSharedPref(Application application){
        return application.getSharedPreferences(Constants.USERID_PREFS, MODE_PRIVATE);
    }

    @Singleton
    @Provides
    static MainApi provideMainApi(Retrofit retrofit) {
        return retrofit.create(MainApi.class);
    }


    @Singleton
    @Provides
    static SavedDao provideSavedDao(NewsDatabase database) {
        return database.savedDao();
    }

    @Singleton
    @Provides
    static NewsDao provideNewsDao(NewsDatabase database) {
        return database.newsDao();
    }

    @Provides
    @Singleton
    @Named("circleRequestOption")
    static RequestOptions provideCircleRequestOptions() {
        return RequestOptions.circleCropTransform();
    }

    @Provides
    @Singleton
    static ConnectivityManager connectivityManager(Application application){
        return (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }


}
