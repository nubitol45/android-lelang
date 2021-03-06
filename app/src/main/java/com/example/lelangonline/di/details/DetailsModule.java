package com.example.lelangonline.di.details;

import com.bumptech.glide.request.RequestOptions;
import com.example.lelangonline.database.saved.SavedDao;
import com.example.lelangonline.ui.details.DetailsRepository;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
class DetailsModule {

    @Provides
    @DetailsScope
    @Named("croppedRequestOption")
    static RequestOptions provideNonCircleRequestOptions() {
        return RequestOptions.centerInsideTransform();
    }

    @Provides
    @DetailsScope
    static DetailsRepository provideDetailsRepository(SavedDao savedDao, CompositeDisposable disposable){
        return new DetailsRepository(savedDao, disposable);
    }
}
