package org.tensorflow.lite.examples.detection.db;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class UserViewModel extends AndroidViewModel {

    private String TAG = this.getClass().getSimpleName();
    private UserDao userDao;
    private AppDatabase appDatabase;
    private LiveData<List<User>> mAllUsers;
    private LiveData<String[]> mAllLocations;
    private LiveData<User> mUser;
    private int UserLocationTypeExists;

    public UserViewModel(@NonNull Application application) {
        super(application);

        appDatabase = AppDatabase.getInstance(application);
        userDao = appDatabase.userDao();
        mAllUsers = userDao.getAllValues();
        mAllLocations = userDao.getAllLocations();
    }

    public void setId(int id){
        mUser = userDao.getUserWithId(id);
    }

    public User getUserWithLocationType(String type, String location){
        return userDao.getUserwithLocationType(type, location);
    }

    public int setTypeLocation(String type, String location){
        UserLocationTypeExists = userDao.UserWithTypeLocationExists(type, location);
        return UserLocationTypeExists;
    }

    public void update(User user) {
        new UpdateAsyncTask(userDao).execute(user);
    }

    public void insert(User user){
        new InsertAsyncTask(userDao).execute(user);
    }

    public void delete(User user){
        new DeleteAsyncTask(userDao).execute(user);
    }

    public List<User> getUserWithType(String type){
        return userDao.getUserWithType(type);
    }

    public LiveData<List<User>> getAllUsers(){
        return mAllUsers;
    }

    public LiveData<String[]> getAllLocations() {
        return mAllLocations;
    }

    public LiveData<User> getUserAtId(){
        return mUser;
    }

    public User getUserWithIdNoLiveData(int id){
        return userDao.getUserWithIdNoLiveData(id);
    }

    private class InsertAsyncTask extends AsyncTask<User, Void, Void> {

        UserDao userDao;

        public InsertAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(User... users) {
            userDao.insertValue(users[0]);
            return null;
        }
    }

    private class DeleteAsyncTask extends AsyncTask<User, Void, Void> {

        UserDao userDao;

        public DeleteAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(User... users) {
            userDao.deleteValue(users[0]);
            return null;
        }
    }

    private class UpdateAsyncTask extends AsyncTask<User, Void, Void> {

        UserDao userDao;

        public UpdateAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(User... users) {
            userDao.update(users[0]);
            return null;
        }
    }
}
