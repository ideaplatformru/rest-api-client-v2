package ru.ip.restclient;

import java.util.List;
import java.util.Map;

public class BatchResult {
    //Возвращает список успешно обработанных объектов вместе со значениями их полей
    private List<Map<String, Object>> success;
    //Возвращает список неуспешно обработанных объектов и причинами ошибок
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
