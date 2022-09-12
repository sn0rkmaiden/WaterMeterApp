package org.tensorflow.lite.examples.detection;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.db.User;
import org.tensorflow.lite.examples.detection.db.UserAdapter;
import org.tensorflow.lite.examples.detection.db.UserViewModel;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.env.Utils;
import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.YoloV4Classifier;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;

    public static String selected_location = "";
    private String edited_prediction;
    private String edited_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Water Meter Reader");

        resultView = findViewById(R.id.textInputEditText);
        //cameraButton = findViewById(R.id.cameraButton);
        detectButton = findViewById(R.id.detectButton);
        detectButton.setMaxLines(2);
        imageView = findViewById(R.id.imageView);
        take_picture_button = findViewById(R.id.button_camera);
        take_picture_button.setMaxLines(2);
        save_button = findViewById(R.id.button_save);
        all_values = findViewById(R.id.button_all_values);

        //cameraButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DetectorActivity.class)));

        detectButton.setOnClickListener(v -> {

            Handler handler = new Handler();

            new Thread(() -> {
                final List<Classifier.Recognition> results;
                if (photoTaken){
                    results = detector.recognizeImage(photo);
                }
                else{
                    results = detector.recognizeImage(cropBitmap);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (photoTaken){
                            handleResult(photo, results);
                        }
                        else {
                            handleResult(cropBitmap, results);
                        }
                    }
                });
            }).start();

        });

         take_picture_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                }else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (String.valueOf(resultView.getText()).equals("Здесь будет предсказание")){
                    Toast.makeText(MainActivity.this, "Нет значения, чтобы сохранить!", Toast.LENGTH_LONG).show();
                }
                else if (String.valueOf(resultView.getText()).equals("На фото нет счётчика!")){
                    Toast.makeText(MainActivity.this, "На фото нет счётчика!", Toast.LENGTH_SHORT).show();
                }
                else{

                    showOptionsDialog();

                }

            }
        });

        all_values.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAllValuesActivity();
            }
        });

        this.sourceBitmap = Utils.getBitmapFromAsset(MainActivity.this, "watermeter2.jpg");

        this.cropBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE);

        this.imageView.setImageBitmap(cropBitmap);

        initBox();

        /*resultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInputPredictionDialog();
            }
        });*/

    }

    public void openInputPredictionDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Если предсказание неточное, введите правильное значение:");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        //final MaterialTextView input2 = new MaterialTextView(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Введите правильное значение");

        //final MaterialTextView date2 = new MaterialTextView(this);
        final EditText date = new EditText(this);
        date.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        date.setHint("Введите дату в формате - мм.гггг");

        layout.addView(input);
        layout.addView(date);

        builder.setView(layout);
        builder.setPositiveButton("Дальше", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                edited_prediction = input.getText().toString();
                edited_date = date.getText().toString();
                dialogInterface.dismiss();
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


    public void openAllValuesActivity(){
        Intent intent = new Intent(this, AllValuesActivity.class);
        startActivity(intent);
    }

    public void showOptionsDialog(){
        String[] items = {"Горячая вода", "Холодная вода", "Газ", "Электричество"};
        final String[] selected = {""};
        /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("What is the counter for?");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selected[0] = items[i];
                //Toast.makeText(MainActivity.this, selected[0], Toast.LENGTH_SHORT).show();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (selected[0].equals("")){
                    Toast.makeText(MainActivity.this, "Nothing is chosen! Value is not saved.", Toast.LENGTH_SHORT).show();
                }
                else {
                    openEditTextDialog(selected[0]);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();*/

        MaterialAlertDialogBuilder builder1 = new MaterialAlertDialogBuilder(this);

        builder1.setTitle("Выберите тип счетчика:")
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selected[0] = items[i];
                        //Toast.makeText(MainActivity.this, selected[0], Toast.LENGTH_SHORT).show();
                    }
                })
        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        })
        .setPositiveButton("Дальше", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (selected[0].equals("")){
                    Toast.makeText(MainActivity.this, "Ничего не выбрано! Значение не сохранено.", Toast.LENGTH_SHORT).show();
                }
                else {
                    openEditTextDialog(selected[0]);
                }
            }
        })
        .show();

    }

    public void openEditTextDialog(String selected){

        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_recyclerview);

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.80);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.80);
        dialog.getWindow().setLayout(width, height);

        Button cancel = (Button) dialog.findViewById(R.id.button_dialog_cancel);
        Button ok = (Button) dialog.findViewById(R.id.button_dialog_ok);
        String[] items = {""};
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("selected_location", selected_location);
                if (!selected_location.equals("") & !selected_location.equals(getString(R.string.new_location))){
                    String value;
                    if (edited_prediction != null){
                        value = edited_prediction;
                    }
                    else{
                        value = String.valueOf(resultView.getText());
                    }
                    saveNewValue(value, selected, selected_location);
                    dialog.dismiss();
                }
                else if (selected_location.equals(getString(R.string.new_location))){
                    openInputNewLocationDialog(selected);
                    dialog.dismiss();
                }
                else{
                    Toast.makeText(MainActivity.this, "Выберите расположение!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView_dialog);
        DialogAdapter dialogAdapter = new DialogAdapter(MainActivity.this, items);
        recyclerView.setAdapter(dialogAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userViewModel.getAllLocations().observe(this, new Observer<String[]>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChanged(String[] strings) {
                List<String> strings2 = Arrays.asList(strings);
                String[] locs;
                if (strings2.contains(getString(R.string.new_location))){
                    Set<String> temp = new LinkedHashSet<>(Arrays.asList(strings));
                    locs = temp.toArray( new String[temp.size()] );

                    //locs = Arrays.copyOf(Arrays.stream(strings).distinct().toArray(String[]::new), strings.length);
                }
                else{
                    Set<String> temp = new LinkedHashSet<>(Arrays.asList(strings));
                    locs = temp.toArray( new String[temp.size() + 1] );
                    locs[locs.length - 1] = getString(R.string.new_location);

                    //locs = Arrays.copyOf(Arrays.stream(strings).distinct().toArray(String[]::new), strings.length + 1);
                    //locs[locs.length - 1] = "New location";
                }

                dialogAdapter.setLocations(locs);


            }
        });

        dialog.show();

    }

    private void openInputNewLocationDialog(String selected){
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.enter_new_location);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setBackgroundResource(R.drawable.backtext);
        builder.setView(input);
        builder.setPositiveButton("Дальше", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selected_location = input.getText().toString();
                // save new value here
                if (!selected_location.equals("")){
                    String value;
                    if (edited_prediction != null){
                        value = edited_prediction;
                    }
                    else{
                        value = String.valueOf(resultView.getText());
                    }
                    saveNewValue(value, selected, selected_location);
                }
                else{
                    Toast.makeText(MainActivity.this, R.string.no_location_provided, Toast.LENGTH_SHORT).show();
                }
                dialogInterface.dismiss();
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

    private void saveNewValue(String value, String type, String location){

        // Getting current date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM.yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        int date1 = Integer.parseInt(formattedDate.split("\\.")[0]);
        int date2 = Integer.parseInt(formattedDate.split("\\.")[1]);
        formattedDate = String.valueOf(date1 + "." + date2);

        if (edited_date != null){
            formattedDate = edited_date;
        }

        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        // checking if the counter exists
        int mList = userViewModel.setTypeLocation(type, location);

        if (mList == 0){
            //Saving new User
            User user = new User();

            if (user.values == null){
                user.values = new ArrayList<>();
            }
            if (user.dates == null){
                user.dates = new ArrayList<>();
            }
            user.values.add(value);
            user.dates.add(formattedDate);
            user.type_counter = type;
            user.where_counter = location;

            userViewModel.insert(user);
            Toast.makeText(this, "Успешно сохранено!", Toast.LENGTH_SHORT).show();
        }
        else {
            //Getting current data
            User mUser = userViewModel.getUserWithLocationType(type, location);
            ArrayList<String> values_user = mUser.getValues();
            ArrayList<String> dates_user = mUser.getDates();

            if (dates_user.contains(formattedDate)){
                Toast.makeText(this, "В месяц можно сохранить только одно значение!", Toast.LENGTH_LONG).show();
            }
            else{
                // Updating arrays
                values_user.add(value);
                dates_user.add(formattedDate);

                // Creating new User object with updated values
                User updatedUser = new User();
                updatedUser.id = mUser.getId();
                updatedUser.type_counter = type;
                updatedUser.where_counter = location;
                updatedUser.values = values_user;
                updatedUser.dates = dates_user;

                userViewModel.update(updatedUser);

                Toast.makeText(this, "Успешно обновлено!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){

            if (requestCode == 3){
                this.photo = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(this.photo.getWidth(), this.photo.getHeight());
                this.photo = ThumbnailUtils.extractThumbnail(this.photo, dimension, dimension);
                this.photo = Bitmap.createScaledBitmap(this.photo, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, false);
                this.imageView.setImageBitmap(photo);
                photoTaken = true;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static final Logger LOGGER = new Logger();

    public static final int TF_OD_API_INPUT_SIZE = 416;

    private static final boolean TF_OD_API_IS_QUANTIZED = false;

    private static final String TF_OD_API_MODEL_FILE = "yolov4_tiny2_v1_darknet-416_fp16.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco.txt";

    // Minimum detection confidence to track a detection.
    private static final boolean MAINTAIN_ASPECT = false;
    private Integer sensorOrientation = 90;

    private Classifier detector;
    private boolean photoTaken = false;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    private OverlayView trackingOverlay;

    protected int previewWidth = 0;
    protected int previewHeight = 0;

    private Bitmap sourceBitmap;
    private Bitmap photo;
    private Bitmap cropBitmap;

    private MaterialButton cameraButton, detectButton, take_picture_button, save_button, all_values;
    private ImageView imageView;
    private TextView resultView;
    private TextView input_prediction;

    private Dialog dialog;

    private UserAdapter userAdapter;
    private CalculationAdapter calculationAdapter;

    private UserViewModel userViewModel;

    private void initBox() {
        previewHeight = TF_OD_API_INPUT_SIZE;
        previewWidth = TF_OD_API_INPUT_SIZE;
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        tracker = new MultiBoxTracker(this);
        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> tracker.draw(canvas));

        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, sensorOrientation);

        try {
            detector =
                    YoloV4Classifier.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                canvas.drawRect(location, paint);
//                cropToFrameTransform.mapRect(location);
//
//                result.setLocation(location);
//                mappedRecognitions.add(result);
            }
        }
//        tracker.trackResults(mappedRecognitions, new Random().nextInt());
//        trackingOverlay.postInvalidate();
        imageView.setImageBitmap(bitmap);

        String result1 = String.valueOf(results);
        String result = result1.substring(1);
        List<String> classes = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "counter", "liters");
        List<String> preds = new ArrayList<String>();
        Map<Integer, String> map = new HashMap<Integer, String>();

        Log.i("result", String.valueOf(results));
        if (String.valueOf(results).equals("[]")){
            resultView.setText("На фото нет счётчика!");
        }
        else{
            for (String s : result.split("\\),")){
                //Log.i("check", s.split(" \\(")[0]);
                //Log.i("check2", s.substring(s.indexOf("[") + 1, s.indexOf("]")));
                //Log.i("check3", s.substring(s.indexOf("]") + 2, s.indexOf("(") - 1));
                map.put(Integer.valueOf(s.substring(s.indexOf("[") + 1, s.indexOf("]"))), s.substring(s.indexOf("]") + 2, s.indexOf("(") - 1));
            }
            SortedSet<Integer> keys = new TreeSet<Integer>(map.keySet());
            for (Integer key : keys) {
                String value = map.get(key);
                if ((!value.equals("counter")) & (!value.equals(("liters")))){
                    preds.add(value);
                }
                //Log.i("sorted", value);
            }
        /*for (String p : result.split("\\s")){
            //Log.i("res", p);
            if ((classes.contains(p)) & (!p.equals("counter")) & (!p.equals("liters"))) {
                preds.add(p);
            }
        }*/

        /*for (String one_class : classes) {
            if (result.contains(" " + one_class + " ")) {
                preds.add(one_class);
            }
        }*/
            Log.i("preds", "[" + String.join(", ", preds) + "]");
            resultView.setText(String.join("", preds));
        }
    }
}