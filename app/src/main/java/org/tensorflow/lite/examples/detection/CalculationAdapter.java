package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.detection.db.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CalculationAdapter extends RecyclerView.Adapter<CalculationAdapter.CalcViewHolder>{

    private LayoutInflater inflater;
    private Context mContext;
    private ArrayList<String> mValues;
    private ArrayList<String> mDates;
    private User mUser;

    public void sortArrays(ArrayList<String> mValues1, ArrayList<String> mDates1){
        ArrayList<String> newDates = new ArrayList<>(mDates1);
        Collections.sort(newDates, new Comparator<String>() {
            DateFormat f = new SimpleDateFormat("MM.yyyy");
            @Override
            public int compare(String o1, String o2) {
                try {
                    return f.parse(o1).compareTo(f.parse(o2));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
        //Log.i("sort", String.valueOf(newDates));

        ArrayList<String> newValues = new ArrayList<>();
        for (int i = 0; i < mValues1.size(); i++){
            //get index of value in mValues
            int idx = mDates1.indexOf(newDates.get(i));
            //Log.i("new idx", String.valueOf(idx));
            newValues.add(mValues1.get(idx));
        }

        //Log.i("sort2", String.valueOf(newValues));
        mValues = newValues;
        mDates = newDates;
    }

    public CalculationAdapter(Context ctx){
        inflater = LayoutInflater.from(ctx);
        mContext = ctx;
    }

    public void setUser(User user){
        mUser = user;
        mValues = user.getValues();
        mDates = user.getDates();
        notifyDataSetChanged();
        sortArrays(mValues, mDates);
        Log.i("values", String.valueOf(mValues));
        Log.i("dates", String.valueOf(mDates));
    }

    @NonNull
    @Override
    public CalculationAdapter.CalcViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.calculations_item, parent, false);
        CalculationAdapter.CalcViewHolder holder = new CalculationAdapter.CalcViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CalculationAdapter.CalcViewHolder holder, int position) {
        if (mValues != null & mDates != null){
            String value = mValues.get(position);
            String date = mDates.get(position);
            holder.setValuesDates(value, date, position);
        }
        else{
            holder.mValue.setText("No value");
            holder.mDate.setText("No date");
        }
    }

    @Override
    public int getItemCount() {
        if (mValues != null){
            return mValues.size();
        } else return 0;
    }

    class CalcViewHolder extends RecyclerView.ViewHolder {

        private TextView mValue;
        private TextView mDate;
        private int mPosition;

        public CalcViewHolder(@NonNull View itemView) {
            super(itemView);

            mValue = itemView.findViewById(R.id.item_value);
            mDate = itemView.findViewById(R.id.item_date);

        }

        public void setValuesDates(String value, String date, int position){
            mValue.setText(value);
            mDate.setText(date);
            mPosition = position;
        }
    }

}
