package com.bit.logbook.core.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bit.logbook.core.domain.Result;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class BaseRepository {

    private final Executor executor;

    public BaseRepository() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    public BaseRepository(Executor executor) {
        this.executor = executor;
    }

    protected Executor getExecutor() {
        return executor;
    }

    protected <T> LiveData<Result<T>> executeInBackground(RepositoryTask<T> task) {
        MutableLiveData<Result<T>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        executor.execute(() -> {
            try {
                T data = task.execute();
                result.postValue(Result.success(data));
            } catch (Exception e) {
                result.postValue(Result.error(e));
            }
        });

        return result;
    }

    public interface RepositoryTask<T> {
        T execute() throws Exception;
    }
}
