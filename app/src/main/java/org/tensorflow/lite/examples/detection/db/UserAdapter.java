package org.tensorflow.lite.examples.detection.db;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.detection.CalculationActivity;
import org.tensorflow.lite.examples.detection.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder>{

    private final LayoutInflater layoutInflater;
    private Context mContext;
    private List<User> mUsers;
    private int id;

    public UserAdapter(Context context){
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.example_item, parent, false);
        UserHolder userHolder = new UserHolder(itemView);
        return userHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        if (mUsers != null){
            User user = mUsers.get(position);
            holder.setData(user.getType_counter(), user.getWhere_counter(), position);
        } else {
            //holder.mValue1.setText("No value");
            //holder.mDate1.setText("No date");
            holder.mLocation1.setText("No location");
            holder.mType1.setText("No type");
        }
    }

    public void setUsers(List<User> users){
        mUsers = users;
        notifyDataSetChanged();
    }

    public User getUserAt(int position){
        return mUsers.get(position);
    }

    @Override
    public int getItemCount() {
        if (mUsers != null){
            return mUsers.size();
        } else return 0;
    }

    public class UserHolder extends RecyclerView.ViewHolder{

        //private TextView mValue1;
        //private TextView mDate1;
        private TextView mType1;
        private TextView mLocation1;
        private int mPosition;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            //mValue1 = itemView.findViewById(R.id.item_value);
            //mDate1 = itemView.findViewById(R.id.item_date);
            mType1 = itemView.findViewById(R.id.item_type);
            mLocation1 = itemView.findViewById(R.id.item_location);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    id = mUsers.get(getAdapterPosition()).getId();
                    Intent intent = new Intent(mContext, CalculationActivity.class);
                    intent.putExtra("id", id);
                    mContext.startActivity(intent);

                }
            });
        }

        public void setData(String type, String location, int position){
            //mValue1.setText(value);
            //mDate1.setText(date);
            mType1.setText(type);
            mLocation1.setText(location);
            mPosition = position;
        }
    }
}
