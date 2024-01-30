package ru.ip.restclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestExample {

    public static void main(String[] args) throws Exception {

        //Создаем объект класса RestClient
        RestClient client = new RestClient("http://localhost:8080");


        //Пример вызова метода для авторизации, в качестве параметра передаем имя пользователя и пароль
        client.login("user", "password");

        //Пример вызова метода проверки валидности токена, сохраненного в текущем объекте класса RestClient
        System.out.println("Статус токена: " + client.validateToken(client.getToken()));

        //Пример вызова метода выхода из системы. Сессия закроется для текущего сохраненного токена
        System.out.println("Успешный выход: " + client.logout());

        //Пример вызова метода проверки валидности токена, сохраненного в текущем объекте класса RestClient
        System.out.println("Статус токена: " + client.validateToken(client.getToken()));

        //Пример вызова метода для создания записи в указанной таблице tablename
        //В объекте fields передаем название полей и значения
        String tablename = "testreva";

        Map<String, Object> fields = new HashMap<>();
        fields.put("displayname", "inttest1");
        fields.put("message", "test1");
        Map<String, Object> createTestRecord = client.createRecord(tablename, fields);
        System.out.println(createTestRecord);


        //Пример вызова метода для обновления записи в указанной таблице tablename
        //В объекте fieldsUpdate передаем название полей и их новые значения
        //Обязательно должен быть указан ID записи для обновления (в ключевом поле таблицы) или shortname записи
        Map<String, Object> fieldsUpdate2 = new HashMap<>();
        fieldsUpdate2.put("message", "test3");
        Integer fieldId = 8;
        Map<String, Object> updateTestRecord2 = client.updateRecordById(tablename, fieldId, fieldsUpdate2);
        System.out.println(updateTestRecord2);

        //Пример вызова метода для обновления записи в указанной таблице tablename
        //В объекте fieldsUpdate передаем название полей и их новые значения
        //Обязательно должен быть указан ID записи для обновления (в ключевом поле таблицы) или shortname записи
        Map<String, Object> fieldsUpdate3 = new HashMap<>();
        fieldsUpdate3.put("message", "test4");
        String shortname = "9e421b83-8fc7-d08c-0a75-c640935a762f";
        Map<String, Object> updateTestRecord3 = client.updateRecordByShortname(tablename, shortname, fieldsUpdate3);
        System.out.println(updateTestRecord3);

        //Пример вызова метода для получения метаданных таблицы tablename
        Map<String, Object> metadataRecord = client.getMetadata(tablename);
        System.out.println(metadataRecord);

        //Пример вызова метода для получения записи в указанной таблице tablename по ee id
        Integer fieldToFindId = 8;
        Map<String, Object> findedRecord = client.getRecordById(tablename, fieldToFindId);
        System.out.println(findedRecord);

        //Пример вызова метода для получения записи в указанной таблице tablename по ee shortname
        String shortnameToFind = "9e421b83-8fc7-d08c-0a75-c640935a762f";
        Map<String, Object> findedRecordByShortname = client.getRecordByShortname(tablename, shortnameToFind);
        System.out.println(findedRecordByShortname);

        //Пример вызова метода для удаления записи в указанной таблице tablename по ee id
        Integer fieldToDeleteId = 8;
        client.deleteRecordById(tablename, fieldToDeleteId);

        //Пример вызова метода для удаления записи в указанной таблице tablename по ee shortname
        String shortnameToDelete = "9e421b83-8fc7-d08c-0a75-c640935a762f";
        client.deleteRecordByShortname(tablename, shortnameToDelete);

        //Пример вызова метода для массового создания записей в указанной таблице tablename
        //В объектах fields1 и fields2 указываем название полей и значения
        //Объединяем эти два объекта в список dataList
        Map<String, Object> fields1 = new HashMap<>();
        fields1.put("displayname", "batch_inttest1");
        fields1.put("message", "batch_test1");
        Map<String, Object> fields2 = new HashMap<>();
        fields2.put("displayname", "batch_inttest2");
        fields2.put("message", "batch_test2");
        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(fields1);
        dataList.add(fields2);
        BatchResult batchCreateTestreva = client.batchCreateRecords(tablename, dataList);
        System.out.println(batchCreateTestreva);

        //Пример вызова метода для массового обновления записей в указанной таблице tablename
        //В объектах fields1 и fields2 указываем название полей и значения.
        // В каждом обновляемом объекте обязательно указывает значение поля реконсиляции таблицы, в нашем случае это shortname
        //Объединяем эти два объекта в список dataList
        Map<String, Object> updateFields1 = new HashMap<>();
        updateFields1.put("shortname", "6365c047-0068-cf28-cf06-e6ad0b27a4fc");
        updateFields1.put("displayname", "batch_inttest11");
        updateFields1.put("message", "batch_test11");
        Map<String, Object> updateFields2 = new HashMap<>();
        updateFields2.put("shortname", "4abbd3ba-7e1a-3d74-9c11-c227ef767ab3");
        updateFields2.put("displayname", "batch_inttest22");
        updateFields2.put("message", "batch_test22");
        List<Map<String, Object>> updateDataList = new ArrayList<>();
        updateDataList.add(updateFields1);
        updateDataList.add(updateFields2);
        BatchResult batchUpdateTestreva = client.batchUpdateRecords("testreva", updateDataList);
        System.out.println(batchUpdateTestreva);

    }
}
