package com.service;

import javax.ws.rs.core.Response;

/**
 * Информирует клиент о том, завершена операция успешно или произошла ошибка.
 */
public class ResponseCreator {

    /**
     * Создайт ответ сервера при возникновении ошибки при выполнении
     * @param status код статуса сервера
     * @param errorCode код ошибки
     * @param version хедер
     * @return сформированный ответ сервера
     */
    public static Response error(int status, int errorCode, String version) {
        Response.ResponseBuilder response = Response.status(status);
        response.header("version", version);
        response.header("errorcode", errorCode);
        response.entity("none");
        return response.build();
    }

    /**
     * Создайт ответ сервера при успешном завершении операции
     * @param version хедер
     * @param object возвращаемый объект
     * @return сформированный ответ сервера
     */
    public static Response success(String version, Object object) {
        Response.ResponseBuilder response = Response.ok();
        response.header("version", version);
        if (object != null) {
            response.entity(object);
        } else {
            response.entity("none");
        }
        return response.build();
    }
}