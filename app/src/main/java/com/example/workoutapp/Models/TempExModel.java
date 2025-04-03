package com.example.workoutapp.Models;

import java.util.List;

public class TempExModel {

    String exName;
    String typeEx;
    int ex_id;
    String data;

    List<SetsModel> setsList;

    public TempExModel(String presetName, List<SetsModel> setList) {
        this.exName = presetName;
        this.setsList = setList;
    }

    public String getExName() {
        return exName;
    }

    public void setExName(String exName) {
        this.exName = exName;
    }

    public int getEx_id() {
        return ex_id;
    }

    public void setEx_id(int ex_id) {
        this.ex_id = ex_id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    public List<SetsModel> getSetsList() {
        return setsList;
    }

    public void setSetsList(List<SetsModel> setsList) {
        this.setsList = setsList;
    }
    public String getTypeEx() {
        return typeEx;
    }

    public void setTypeEx(String typeEx) {
        this.typeEx = typeEx;
    }


}
