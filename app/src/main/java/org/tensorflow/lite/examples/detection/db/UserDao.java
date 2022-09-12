package org.tensorflow.lite.examples.detection.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM User ")
    LiveData<List<User>> getAllValues();

    @Insert
    void insertValue(User ... users);

    @Delete
    void deleteValue(User user);

    @Query("SELECT location FROM User")
    LiveData<String[]> getAllLocations();

    @Query("SELECT * from User WHERE type=:type_counter AND location=:location_counter")
    int UserWithTypeLocationExists(String type_counter, String location_counter);

    @Query("SELECT * from User WHERE id=:id_counter")
    LiveData<User> getUserWithId(int id_counter);

    @Query("SELECT * from User WHERE type=:type_counter1 AND location=:location_counter1")
    User getUserwithLocationType(String type_counter1, String location_counter1);

    @Query("SELECT * from User WHERE id=:id_user")
    User getUserWithIdNoLiveData(int id_user);

    @Query("SELECT * from User WHERE type=:type_counter")
    List<User> getUserWithType(String type_counter);

    @Update
    void update(User user);

}
