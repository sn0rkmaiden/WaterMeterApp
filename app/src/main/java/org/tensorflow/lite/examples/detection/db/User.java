package org.tensorflow.lite.examples.detection.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.tensorflow.lite.examples.detection.Converters;

import java.util.ArrayList;

@Entity
public class User {

    public int getId() {
        return id;
    }

    @NonNull
    public ArrayList<String> getValues(){
        return values;
    }

    @NonNull
    public ArrayList<String> getDates(){
        return dates;
    }

    @NonNull
    public String getType_counter() {
        return type_counter;
    }

    @NonNull
    public String getWhere_counter() {
        return where_counter;
    }

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "d")
    @TypeConverters({Converters.class})
    public ArrayList<String> dates;

    @ColumnInfo(name = "value")
    @TypeConverters({Converters.class})
    public ArrayList<String> values;

    @ColumnInfo(name = "type")
    public String type_counter;

    @ColumnInfo(name = "location")
    public String where_counter;
}
