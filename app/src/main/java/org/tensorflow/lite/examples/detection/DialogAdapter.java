package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.detection.db.User;

import java.util.List;

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private String[] mAllPositions;
    private Context mContext;


    public DialogAdapter(Context ctx, String[] mAllPositions){

        inflater = LayoutInflater.from(ctx);
        this.mAllPositions = mAllPositions;
        mContext = ctx;
    }

    @Override
    public DialogAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.sample_dialog_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(DialogAdapter.MyViewHolder holder, int position) {

        if (mAllPositions != null){
            String loc = mAllPositions[position];
            holder.setData(loc, position);
        } else {
            holder.location.setText("No locations");
        }

    }

    public void setLocations(String[] locations){
        mAllPositions = locations;
        notifyDataSetChanged();
    }

    public String getLocationAt(int position){
        return mAllPositions[position];
    }

    @Override
    public int getItemCount() {
        return mAllPositions.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView location;
        private int mPosition;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.name);
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(mContext, mAllPositions[getAdapterPosition()], Toast.LENGTH_SHORT).show();
                    MainActivity.selected_location = mAllPositions[getAdapterPosition()];
                }
            });
        }

        public void setData(String loc, int position){
            location.setText(loc);
            mPosition = position;
        }


    }
}