package com.service;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZloiY on 09.04.17.
 */
public class JsonTransformer {
    private List<PatternModel> patternsList;
    private PatternModel pattern;

    public JsonTransformer(Gson patternGson){
    }

    public JsonTransformer(){

    }

    public Gson patternToJson(PatternModel pattern){
        Gson gsonObject = new Gson();
        System.out.println(gsonObject.toJson(pattern));
        return gsonObject;
    }
}
