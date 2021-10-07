package com.loussouarn.edouard.go4lunch.view.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.model.User;

import java.util.List;

public class WorkmatesListAdapter extends RecyclerView.Adapter {
    //For Data

    private List<User> users;
    private RequestManager glide;
    private Activity activity;

    public WorkmatesListAdapter(List<User> users, RequestManager glide, Activity activity) {
        this.users = users;
        this.glide = glide;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_workmates, parent, false);
        WorkmatesListRestaurantViewHolder viewHolder = new WorkmatesListRestaurantViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WorkmatesListRestaurantViewHolder viewHolder = (WorkmatesListRestaurantViewHolder) holder;
        viewHolder.updateWorkmates(users.get(position), glide);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class WorkmatesListRestaurantViewHolder extends RecyclerView.ViewHolder {

        private TextView itemText;
        private ImageView picture;
        // private Activity activity;

        //public WorkmatesListRestaurantViewHolder(@NonNull View itemView, Activity activity)
        public WorkmatesListRestaurantViewHolder(@NonNull View itemView) {
            super(itemView);

            picture = itemView.findViewById(R.id.item_list_workmates_image);
            itemText = itemView.findViewById(R.id.item_list_workmates_txt);

            //this.activity = activity;
        }

        private void updateWorkmates(User user, RequestManager glide) {

            glide.load(user.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(picture);
            String message = itemText.getContext().getString(R.string.list_workmates_adapter_is_joining, user.getUserName())
                    ;
            itemText.setText(message);

        }
    }
}
