package com.bs_enterprises.enterprise_backend_template.models.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String code;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // ✅ Success builder (always success = true, code required)
    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(true, message, data, code);
    }

    public static <T> ApiResponse<T> success(String code, String message) {
        return new ApiResponse<>(true, message, null, code);
    }

    // ✅ Failure builder (always success = false, code required)
    public static <T> ApiResponse<T> failure(String code, String message, T data) {
        return new ApiResponse<>(false, message, data, code);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, message, null, code);
    }

}