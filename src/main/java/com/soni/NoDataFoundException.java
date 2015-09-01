package com.soni;

/**
 * Created by asoni1 on 8/23/15.
 */
public class NoDataFoundException extends RuntimeException {

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public NoDataFoundException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public NoDataFoundException(String message, String errorMessage) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public NoDataFoundException(String message, Throwable cause, String errorMessage) {
        super(message, cause);
        this.errorMessage = errorMessage;
    }

    public NoDataFoundException(Throwable cause, String errorMessage) {
        super(cause);
        this.errorMessage = errorMessage;
    }

    public NoDataFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String errorMessage) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorMessage = errorMessage;
    }
}
