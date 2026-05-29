package com.spacework.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String error;

    public ApiResponse() {}

    private ApiResponse(boolean success) {
        this.success = success;
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>(true);
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        ApiResponse<T> r = new ApiResponse<>(true);
        r.data = data;
        r.message = message;
        return r;
    }

    public static <T> ApiResponse<T> ok(String message) {
        ApiResponse<T> r = new ApiResponse<>(true);
        r.message = message;
        return r;
    }

    public static <T> ApiResponse<T> error(String errorMsg) {
        ApiResponse<T> r = new ApiResponse<>(false);
        r.error = errorMsg;
        return r;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
