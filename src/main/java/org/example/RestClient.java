package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RestClient {
    private static final String BASE_URL = "http://localhost:8080";
    private static final String TOKEN_VALIDATION_STATUS_AUTHORIZED = "AUTHORIZED";
    private String token;
    private String user;
    private String password;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {

    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return this.user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    private void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    /**
     * Выполняет операцию входа в систему, используя предоставленное имя пользователя и пароль.
     *
     * @param  username  имя пользователя для операции входа
     * @param  password  пароль для операции входа
     * При успешной авторизации, устанавливает значение токена у объекта, при следующих запрос токен берется из объекта
     */
    public void login(String username, String password) throws IOException {

        HttpPost httpPost = new HttpPost(BASE_URL + "/json/login");
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("user", username);
        requestBody.put("password", password);
        String jsonBody = mapToJson(requestBody);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);

        httpPost.setEntity(entity);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    setToken(null);
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }

                if (statusCode == HttpStatus.SC_OK) {
                    Map<String, Object> jsonResponse = jsonToMap(responseString);
                    Map<String, Object> tokenMap = (Map) jsonResponse.get("token");

                    setToken((String) tokenMap.get("value"));
                    //System.out.println(token);
                }
            }
        }
    }

    /**
     * Проверяет переданный токен
     *
     * @param  token   токен для проверки
     * @return        true, если токен действителен, в противном случае - false
     */
    public Boolean validateToken(String token) throws IOException {

        HttpPost httpPost = new HttpPost(BASE_URL + "/json/m/auth/validate");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("token", token);
        String jsonBody = mapToJson(requestBody);
        HttpEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }
                if (statusCode == HttpStatus.SC_OK) {
                    Map<String, Object> jsonResponse = jsonToMap(responseString);
                    String tokenStatus = (String) jsonResponse.get("status");
                    if (RestClient.TOKEN_VALIDATION_STATUS_AUTHORIZED.equals(tokenStatus)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Отправляет запрос на выход из системы.
     * Токен у объекта уничтожается
     *
     * @return         	true, если запрос на выход выполнен успешно, в противном случае - false
     */
    public Boolean logout() throws IOException {

        HttpPost httpPost = new HttpPost(BASE_URL + "/json/logout");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-AUTH", token);


        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + EntityUtils.toString(responseEntity));
                }
                if (statusCode == HttpStatus.SC_OK) {
                    setToken(null);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Создает запись в указанной таблице с использованием предоставленных полей.
     *
     * @param  tablename  имя таблицы, в которой создается запись
     * @param  fields     структура, содержащая имена полей и их соответствующие значения
     * @return            структура созданной записи
     */
    public Map<String, Object> createRecord(String tablename, Map<String, Object> fields) throws IOException {
        HttpPost httpPost = new HttpPost(BASE_URL + "/json/v2/xapi/entity/"+tablename+"/create");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-AUTH", token);

        String jsonBody = mapToJson(fields);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        Map<String, Object> mapResponse = new HashMap<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }
                mapResponse = jsonToMap(responseString);
            }
        }

        return mapResponse;
    }

    /**
     * Обновляет запись в указанной таблице с использованием предоставленных полей.
     *
     * @param  tablename  имя таблицы, в которой создается запись
     * @param  fields     структура, содержащая имена полей и их соответствующие значения
     * @return            структура обновленной записи
     */
    public Map<String, Object> updateRecord(String tablename, Map<String, Object> fields) throws IOException {
        HttpPost httpPost = new HttpPost(BASE_URL + "/json/v2/xapi/entity/"+tablename+"/update");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-AUTH", token);

        String jsonBody = mapToJson(fields);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        Map<String, Object> mapResponse = new HashMap<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }
                mapResponse = jsonToMap(responseString);
            }
        }

        return mapResponse;
    }

    /**
     * Обновляет запись в указанной таблице по ее ID.
     *
     * @param  tablename  имя таблицы для обновления
     * @param  id         ID записи для обновления
     * @param  fields     поля для обновления в записи
     * @return            обновленная запись в виде структуры
     */
    public Map<String, Object> updateRecordById(String tablename, Integer id, Map<String, Object> fields) throws IOException {
        HttpPost httpPost = new HttpPost(BASE_URL + "/json/v2/xapi/entity/"+tablename+"/"+id+"/update");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-AUTH", token);

        String jsonBody = mapToJson(fields);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        Map<String, Object> mapResponse = new HashMap<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }
                mapResponse = jsonToMap(responseString);
            }
        }

        return mapResponse;
    }

    /**
     * Обновляет запись в указанной таблице по ее shortname.
     *
     * @param  tablename  имя таблицы для обновления
     * @param  shortname  shortname записи
     * @param  fields     структура полей и их значений для обновления
     * @return            структура полей и их значений обновленной записи
     */
    public Map<String, Object> updateRecordByShortname(String tablename, String shortname, Map<String, Object> fields) throws IOException {
        HttpPost httpPost = new HttpPost(BASE_URL + "/json/v2/xapi/entity/"+tablename+"/sn/"+shortname+"/update");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-AUTH", token);

        String jsonBody = mapToJson(fields);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        Map<String, Object> mapResponse = new HashMap<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }
                mapResponse = jsonToMap(responseString);
            }
        }

        return mapResponse;
    }

    /**
     * Удаляет запись из указанной таблицы по ее идентификатору и возвращает ответ в виде структуры.
     *
     * @param  tablename  имя таблицы, из которой нужно удалить запись
     * @param  id         идентификатор записи, которую нужно удалить
     * @return            карта, содержащая ответ от операции удаления
     */
    public Map<String, Object> deleteRecordById(String tablename, Integer id) throws IOException {
        HttpPost httpPost = new HttpPost(BASE_URL + "/json/v2/xapi/entity/"+tablename+"/"+id+"/delete");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-AUTH", token);

        Map<String, Object> mapResponse = new HashMap<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }
                mapResponse = jsonToMap(responseString);
            }
        }

        return mapResponse;
    }

    /**
     * Удаляет запись из указанной таблицы по ее shortname и возвращает ответ в виде структуры.
     *
     * @param  tablename  имя таблицы, из которой нужно удалить запись
     * @param  shortname  shorname записи, которую нужно удалить
     * @return            карта, содержащая ответ от операции удаления
     */
    public Map<String, Object> deleteRecordByShortname(String tablename, String shortname, Map<String, Object> fields) throws IOException {
        HttpPost httpPost = new HttpPost(BASE_URL + "/json/v2/xapi/entity/"+tablename+"/sn/"+shortname+"/delete");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-AUTH", token);

        Map<String, Object> mapResponse = new HashMap<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }
                mapResponse = jsonToMap(responseString);
            }
        }

        return mapResponse;
    }

    /**
     * Метод для пакетного создания записей.
     *
     * @param  tablename  имя таблицы
     * @param  dataList   список записей для создания, состоящий из структур
     * @return           результат операции пакетного создания, содержит два списка success и errors
     * Список success содержит список созданных записей и значения их полей
     * Список errors содержит список записей, которые создать не удалось, и причинами ошибки
     */
    public BatchResult batchCreateRecords(String tablename, List<Map<String, Object>> dataList) throws IOException {
        HttpPost httpPost = new HttpPost(BASE_URL + "/json/v2/xapi/batch/"+tablename+"/create");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-AUTH", token);

        String jsonBody = mapListToJson(dataList);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        BatchResult mapResponse = new BatchResult();

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }
                mapResponse = jsonToMap(responseString, new TypeReference<BatchResult>() {});
            }
        }

        return mapResponse;
    }

    /**
     * Метод для пакетного обновления записей.
     *
     * @param  tablename  имя таблицы
     * @param  dataList   список записей для обновления, состоящий из структур с новыми значениями полей
     * @return           результат операции пакетного создания, содержит два списка success и errors
     * Список success содержит список обновленных записей и значения их полей
     * Список errors содержит список записей, которые обновить не удалось, и причинами ошибки
     */
    public BatchResult batchUpdateRecords(String tablename, List<Map<String, Object>> dataList) throws IOException {
        HttpPost httpPost = new HttpPost(BASE_URL + "/json/v2/xapi/batch/"+tablename+"/update");
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("X-AUTH", token);

        String jsonBody = mapListToJson(dataList);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        BatchResult mapResponse = new BatchResult();

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 299) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode + ", error message : " + responseString);
                }
                mapResponse = jsonToMap(responseString, new TypeReference<BatchResult>() {});
            }
        }

        return mapResponse;
    }

    private void handleResponse(CloseableHttpResponse response) throws IOException {
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseString = EntityUtils.toString(entity);
                Map<String, Object> jsonResponse = jsonToMap(responseString);
                System.out.println(mapToJson(jsonResponse));
            }
        } finally {
            response.close();
        }
    }

    private String mapToJson(Map<String, ?> map) throws IOException {
        return objectMapper.writeValueAsString(map);
    }

    private String mapListToJson(List<?> list) throws IOException {
        return objectMapper.writeValueAsString(list);
    }

    private Map<String, Object> jsonToMap(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
        });
    }

    private BatchResult jsonToMap(String jsonString, TypeReference Type) throws IOException {
        return (BatchResult) objectMapper.readValue(jsonString, Type);
    }

    private static List<Map<String, Object>> jsonToMapList(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
    }
}
