package com.bit.logbook.core.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class UseCase<T, Params> {

    private final Executor executor;

    public UseCase() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    public UseCase(Executor executor) {
        this.executor = executor;
    }

    protected abstract T execute(Params params) throws Exception;

    public LiveData<Result<T>> executeAsync(Params params) {
        MutableLiveData<Result<T>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        executor.execute(() -> {
            try {
                T data = execute(params);
                result.postValue(Result.success(data));
            } catch (Exception e) {
                result.postValue(Result.error(e));
            }
        });

        return result;
    }

    public void executeAsync(Params params, UseCaseCallback<T> callback) {
        executor.execute(() -> {
            try {
                T data = execute(params);
                callback.onSuccess(data);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public interface UseCaseCallback<T> {
        void onSuccess(T data);
        void onError(Throwable error);
    }
}
