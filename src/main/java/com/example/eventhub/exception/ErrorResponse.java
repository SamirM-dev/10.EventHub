package com.example.eventhub.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
 public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private List<ErrorField> errorFields=new ArrayList<>();

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp=LocalDateTime.now();
    }

    public void setErrorFields(List<ErrorField> errorFields){
        this.errorFields=errorFields;
    }

    @Getter
    static
    class ErrorField{
        private String filed;
        private String error;
        public ErrorField(String filed,String error){
            this.filed=filed;
            this.error=error;
        }
    }
}
