package ru.ip.restclient;

import java.util.List;
import java.util.Map;

public class BatchResult {
    private List<Map<String, Object>> success;
    private List<Map<String, Object>> errors;

    public List<Map<String, Object>> getSuccess() {
        return success;
    }

    public void setSuccess(List<Map<String, Object>> success) {
        this.success = success;
    }

    public List<Map<String, Object>> getErrors() {
        return errors;
    }

    public void setErrors(List<Map<String, Object>> errors) {
        this.errors = errors;
    }
}
