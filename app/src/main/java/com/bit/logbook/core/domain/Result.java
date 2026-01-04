package com.bit.logbook.core.domain;

public class Result<T> {
    private final T data;
    private final Throwable error;
    private final Status status;

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    private Result(T data, Throwable error, Status status) {
        this.data = data;
        this.error = error;
        this.status = status;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, Status.SUCCESS);
    }

    public static <T> Result<T> error(Throwable error) {
        return new Result<>(null, error, Status.ERROR);
    }

    public static <T> Result<T> loading() {
        return new Result<>(null, null, Status.LOADING);
    }

    public T getData() {
        return data;
    }

    public Throwable getError() {
        return error;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }
}
