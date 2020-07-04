package com.example.cosc_project;

public class DataModel {
    String exam_name;
    String details;

    public DataModel(String exam_name,String details){
        this.exam_name=exam_name;
        this.details=details;
    }
    public String getExamName() {
        return exam_name;
    }

    public String getDetails() {
        return details;
    }
}
