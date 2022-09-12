package org.tensorflow.lite.examples.detection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.tensorflow.lite.examples.detection.db.User;
import org.tensorflow.lite.examples.detection.db.UserAdapter;
import org.tensorflow.lite.examples.detection.db.UserViewModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllValuesActivity extends AppCompatActivity{

    private UserAdapter userAdapter;
    private UserViewModel userViewModel;
    private Button calculate_button;

    private Boolean calc_drainage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_values);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Счётчики");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        calculate_button = findViewById(R.id.button_calculate);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        userAdapter = new UserAdapter(this);
        recyclerView.setAdapter(userAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                userAdapter.setUsers(users);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                userViewModel.delete(userAdapter.getUserAt(viewHolder.getAdapterPosition()));
                Toast.makeText(AllValuesActivity.this, "Значение удалено!", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);


        calculate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userAdapter.getItemCount() != 0){
                    openDatePickerDialog();
                }
                else{
                    Toast.makeText(AllValuesActivity.this, "Нет никаких счётчиков!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void openDatePickerDialog() {
        final int[] year1 = new int[1];
        final int[] month1 = new int[1];
        final int[] year2 = new int[1];
        final int[] month2 = new int[1];
        MonthYearPickerDialog pd = new MonthYearPickerDialog();
        MonthYearPickerDialog pd2 = new MonthYearPickerDialog();
        pd.setListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int i2) {
                //Toast.makeText(AllValuesActivity.this, month + " " + year , Toast.LENGTH_SHORT).show();
                year1[0] = year;
                month1[0] = month;
                pd2.show(getSupportFragmentManager(), "MonthYearPickerDialog");
            }
        });
        pd.show(getSupportFragmentManager(), "MonthYearPickerDialog");

        pd2.setListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int i2) {
                //Toast.makeText(AllValuesActivity.this, month + " " + year, Toast.LENGTH_SHORT).show();
                year2[0] = year;
                month2[0] = month;

                String[] types = {"Горячая вода", "Холодная вода", "Газ", "Электричество"};
                ArrayList<String> existing_types = new ArrayList<>();
                List<User> type_user;
                for (String type : types) {
                    type_user = userViewModel.getUserWithType(type);
                    //Log.i("types", type + " " + type_user);
                    if (!type_user.isEmpty()){
                        existing_types.add(type);
                    }
                }

                if (year2[0] > year1[0]){
                    openInputRateDialog(existing_types, year1[0], month1[0], year2[0], month2[0]);
                }
                else if (year2[0] == year1[0]){
                    if (month2[0] > month1[0]){
                        openInputRateDialog(existing_types, year1[0], month1[0], year2[0], month2[0]);
                    }
                    else{
                        Toast.makeText(AllValuesActivity.this, "Второй месяц должен быть больше, чем первый!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(AllValuesActivity.this, "Второй год должен быть больше, чем первый!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openInputRateDialog(ArrayList<String> existing_types, int year1, int month1,
                                     int year2, int month2) {
        ArrayList<Integer> rates = new ArrayList<>();

        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Введите тариф:");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        for (String type: existing_types){
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint("Введите " + type.toLowerCase() + " тариф");
            layout.addView(input);
        }

        if (existing_types.contains("Горячая вода") & existing_types.contains("Холодная вода")) {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint("Введите тариф для водоотведения");
            layout.addView(input);
            calc_drainage = true;
            existing_types.add("Водоотведение");
        }
        else {
            calc_drainage = false;
            Toast.makeText(this, "Чтобы посчитать водоотведение нужно добавить горячую и холодную воду!", Toast.LENGTH_SHORT).show();
        }

        builder.setView(layout);
        builder.setPositiveButton("Дальше", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int cnt = 0; cnt < layout.getChildCount(); cnt++){
                    EditText input = (EditText) layout.getChildAt(cnt);
                    Log.i("input", String.valueOf(input.getText().toString().isEmpty()));
                    if (!input.getText().toString().isEmpty()){
                        rates.add(Integer.valueOf(input.getText().toString()));
                        //Log.i("added", "value " + Integer.valueOf(input.getText().toString()) + " is added");
                    }
                    else{
                        Toast.makeText(AllValuesActivity.this, "Введите тариф!", Toast.LENGTH_SHORT).show();
                    }
                    dialogInterface.dismiss();
                }
                if (existing_types.size() == rates.size()){
                    openCalculateDialog(existing_types, rates, year1, month1, year2, month2);
                }
                else{
                    Toast.makeText(AllValuesActivity.this, "Не все тарифы введены!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void openCalculateDialog(ArrayList<String> existing_types, ArrayList<Integer> rates, int year1, int month1, int year2, int month2) {
        //Toast.makeText(this, existing_types + " " + rates + " " + year1 + " " + month1 + " " + year2 + " " + month2, Toast.LENGTH_LONG).show();
        //Log.i("sizes", existing_types.size() + " ___ " + rates.size());
        assert existing_types.size() == rates.size();

        int total = 0;

        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Результат:");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int result_drainage = 0;

        for (int i = 0; i < existing_types.size(); i ++){

            int result = 0;

            String current_type = existing_types.get(i);
            int current_rate = rates.get(i);

            List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

            String date1, date2;

            if (numbers.contains(month1)){
                date1 = "0" + month1 + "." + year1;
            }
            else{
                date1 = month1 + "." + year1;
            }

            if (numbers.contains(month2)){
                date2 = "0" + month2 + "." + year2;
            }
            else{
                date2 = month2 + "." + year2;
            }

            List<User> mUser = userViewModel.getUserWithType(current_type);

            for (int j = 0; j < mUser.size(); j++){
                ArrayList<String> dates = mUser.get(j).getDates();
                Log.i("dates", current_type + " " + j + dates);
                ArrayList<String> values = mUser.get(j).getValues();

                Log.i("dates", dates + " ---- " + date1 + " ---- " + date2);

                if (dates.contains(date1) & dates.contains(date2)){
                    int position_date1 = dates.indexOf(date1);
                    int position_date2 = dates.indexOf(date2);

                    int value1 = Integer.parseInt(values.get(position_date1));
                    int value2 = Integer.parseInt(values.get(position_date2));

                    int difference = (month2 - month1) + (year2 - year1) * 12;
                    result += (value2 - value1) * difference;

                }
                else if (!dates.contains(date1)){
                    Toast.makeText(this, "Значения с датой № 1 - " + date1 + " - нет в базе данных!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Значения с датой № 2 - " + date2 + " - нет в базе данных!", Toast.LENGTH_SHORT).show();
                }
            }

            if (calc_drainage){
                if (current_type.equals("Горячая вода") || current_type.equals("Холодная вода")){
                    result_drainage += result;
                }
            }

            total += result * current_rate;

            TextView res = new TextView(this);

            if (current_type.equals("Водоотведение")){
                Log.i("result_drainage", String.valueOf(result_drainage));
                String ending = returnEnding((result_drainage * current_rate) % 10);
                res.setText(current_type + " : " + result_drainage + " x " + current_rate + " = " + result_drainage * current_rate + ending);
            }
            else{
                String ending = returnEnding((result * current_rate) % 10);
                res.setText(current_type + " : " + result + " x " + current_rate + " = " + result * current_rate + ending);
            }
            res.setTextSize(20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(20);
            res.setLayoutParams(params);

            if (calc_drainage && current_type.equals("Водоотведение")){
                if (result_drainage != 0 && result == 0){
                    layout.addView(res);
                }
            }

            if (result != 0){
                layout.addView(res);
            }
        }

        total += result_drainage;

        TextView tot = new TextView(this);
        String ending = returnEnding(total % 10);
        tot.setText("Всего : " + total + ending);
        tot.setTextSize(20);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(20);
        tot.setLayoutParams(params);
        layout.addView(tot);    

        builder.setView(layout);
        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private String returnEnding(int number){
        List<Integer> a = Arrays.asList(0, 5, 6, 7, 8, 9);
        List<Integer> b = Arrays.asList(2, 3, 4);
        if (a.contains(number)){
            return " рублей";
        }
        else if (b.contains(number)){
            return " рубля";
        }
        else{
            return " рубль";
        }
    }
}