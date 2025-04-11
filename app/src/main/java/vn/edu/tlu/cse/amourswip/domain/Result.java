package vn.edu.tlu.cse.amourswip.domain;

public class Result<T> {
    private final Status status;
    private final T data;
    private final String error;

    private Result(Status status, T data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS, data, null);
    }

    public static <T> Result<T> error(String error) {
        return new Result<>(Status.ERROR, null, error);
    }

    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}