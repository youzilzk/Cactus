package com.youzi.tunnel.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class JsonUtil {

    @SuppressWarnings("unchecked")
    public static <T> T json2object(String json, TypeToken<T> typeToken) {
        try {
            Gson gson = new Gson();
            return (T) gson.fromJson(json, typeToken.getType());
        } catch (Exception e) {
        }
        return null;
    }

    /**
     *
     * java对象转为json对象
     *
     */
    public static String object2json(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

}