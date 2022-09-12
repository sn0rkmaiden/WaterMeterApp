package org.tensorflow.lite.examples.detection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.tensorflow.lite.examples.detection.db.User;
import org.tensorflow.lite.examples.detection.db.UserAdapter;
import org.tensorflow.lite.examples.detection.db.UserViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class CalculationActivity extends AppCompatActivity{

    private int id_counter;
    private CalculationAdapter calcAdapter;
    String type;
    String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_counter = extras.getInt("id");
            Log.i("got_id", String.valueOf(id_counter));
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView_calc);
        calcAdapter = new CalculationAdapter(this);
        recyclerView.setAdapter(calcAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.setId(id_counter);
        userViewModel.getUserAtId().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                Log.i("User_id", String.valueOf(user.id));
                calcAdapter.setUser(user);
            }
        });
        User curUser = userViewModel.getUserWithIdNoLiveData(id_counter);
        type = curUser.getType_counter();
        location = curUser.getWhere_counter();
        if (type != null & location != null){
            getSupportActionBar().setTitle(type + " в " + "'" + location + "'");
        }
        else{
            getSupportActionBar().setTitle(R.string.counter_value);
        }

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //Getting current data
                User mUser = userViewModel.getUserWithIdNoLiveData(id_counter);
                ArrayList<String> values_user = mUser.getValues();
                ArrayList<String> dates_user = mUser.getDates();

                // Updating arrays
                values_user.remove(viewHolder.getAdapterPosition());
                dates_user.remove(viewHolder.getAdapterPosition());

                // Creating new User object with updated values
                User updatedUser = new User();
                updatedUser.id = mUser.getId();
                updatedUser.type_counter = mUser.type_counter;
                updatedUser.where_counter = mUser.where_counter;
                updatedUser.values = values_user;
                updatedUser.dates = dates_user;

                userViewModel.update(updatedUser);
                Toast.makeText(CalculationActivity.this, R.string.value_deleted, Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
    }
}