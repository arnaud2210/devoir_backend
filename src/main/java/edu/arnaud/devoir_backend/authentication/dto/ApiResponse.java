package edu.arnaud.devoir_backend.authentication.dto;

import com.fasterxml.jackson.annotation.JsonView;
import edu.arnaud.devoir_backend.authentication.controller.Views;

public class ApiResponse {
    @JsonView(Views.Public.class)
    private boolean success;

    @JsonView(Views.Public.class)
    private String message;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
