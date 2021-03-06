package com.example.lelangonline.paging.main.barang;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import com.example.lelangonline.models.DataItem;
import com.example.lelangonline.ui.MainRepository;
import com.example.lelangonline.utils.DataStatus;

import io.reactivex.disposables.CompositeDisposable;

public class BarangDataSource extends PageKeyedDataSource<Integer, DataItem> {

    private static final String TAG = "NewsDataSource";
    private CompositeDisposable disposable;
    private MainRepository mainRepository;
    private String category;
    private String search;
    private String date;
    private MutableLiveData<DataStatus> mutableLiveData;

    public LiveData<DataStatus> getMutableLiveData() {
        return mutableLiveData;
    }

    public BarangDataSource(CompositeDisposable disposable, MainRepository mainRepository, String category, String search, String date) {
        this.disposable = disposable;
        this.mainRepository = mainRepository;
        mutableLiveData = new MutableLiveData<>();
        if(category.equalsIgnoreCase("semua"))
            category = "";
        this.category = category;
        this.search = search;
        this.date = date;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, DataItem> callback) {
        mutableLiveData.postValue(DataStatus.LOADING);
        disposable.add(mainRepository.fetchFromApi(1, params.requestedLoadSize, category, search, date)
                .subscribe(data -> {
                            if (data.getData().isEmpty())
                                throw new NullPointerException();

                            callback.onResult(data.getData(), null, 2);
                            mutableLiveData.postValue(DataStatus.LOADED);
                            mainRepository.saveData(data);
                        }, throwable ->
                                disposable.add(mainRepository.fetchFromDB(10, 0)
                                        .subscribe(data -> {
                                            if (data.isEmpty())
                                                mutableLiveData.postValue(DataStatus.ERROR);
                                            else {
                                                mutableLiveData.postValue(DataStatus.LOADED);
                                                callback.onResult(data, null, 10);
                                            }
                                        }, error -> Log.d(TAG, "loadInitial: " + error)))
                ));

    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, DataItem> callback) {
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, DataItem> callback) {
        disposable.add(
                mainRepository.fetchFromApi(params.key, params.requestedLoadSize, category, search, date)
                        .subscribe(data -> {
                                    callback.onResult(data.getData(), params.key + 1);
                                    mutableLiveData.postValue(DataStatus.LOADED);
                                    mainRepository.saveData(data);
                                }, throwable -> disposable.add(
                                mainRepository.fetchFromDB(10, params.key)
                                        .subscribe(data -> {
                                                    Log.d(TAG, "loadMMM: New Room " + data.size());
                                                    callback.onResult(data, params.key + 10);
                                                    mutableLiveData.postValue(DataStatus.LOADED);
                                                },
                                                throwable1 -> Log.d(TAG, "database  ERROR " + throwable1))
                                )

                        ));
    }
}
