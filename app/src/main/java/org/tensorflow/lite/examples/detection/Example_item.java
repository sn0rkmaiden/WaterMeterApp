package org.tensorflow.lite.examples.detection;

public class Example_item {

    private String value, date;

    public Example_item(String mValue, String mDate){
        value = mValue;
        date = mDate;
    }

    public String getValue(){
        return value;
    }

    public String getDate(){
        return date;
    }

}
