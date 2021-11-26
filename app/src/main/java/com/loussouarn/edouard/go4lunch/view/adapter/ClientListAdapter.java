package com.loussouarn.edouard.go4lunch.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.api.UserFirebase;
import com.loussouarn.edouard.go4lunch.model.User;

import java.util.List;

public class ClientListAdapter extends RecyclerView.Adapter {

    private List<String> clientsList;
    private RequestManager glide;
    private Activity activity;

    // Constructor
    public ClientListAdapter(List<String> clientsList, RequestManager glide) {
        this.clientsList = clientsList;
        this.glide = glide;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_workmates, parent, false);
        ClientListViewHolder viewHolder = new ClientListViewHolder(v, parent.getContext());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ClientListViewHolder viewHolder = (ClientListViewHolder) holder;
        viewHolder.updateClient(this.clientsList.get(position), this.glide);
    }

    @Override
    public int getItemCount() {
        return clientsList.size();
    }

    static class ClientListViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private ImageView picture;
        private Context mContext;

        public ClientListViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            name = itemView.findViewById(R.id.item_list_workmates_txt);
            picture = itemView.findViewById(R.id.item_list_workmates_image);
            mContext = context;

        }

        private void updateClient(final String clientId, final RequestManager glide) {

            UserFirebase.getUser(clientId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User client = documentSnapshot.toObject(User.class);
                    String text;
                    if (client != null) {
                        text = client.getUserName() + mContext.getResources().getString(R.string.list_client_adapter_is_joining);
                        name.setText(text);
                        name.setTypeface(null, Typeface.NORMAL);
                        name.setTextColor(mContext.getResources().getColor(R.color.black));
                    }

                    // Images
                    if (client != null) {
                        if (client.getUrlPicture() != null) {
                            if (client.getUrlPicture().length()>0){
                                glide.load(client.getUrlPicture())
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(picture);
                            } else {
                                picture.setImageResource(R.drawable.ic_baseline_people_24);
                            }
                        }
                    }
                }
            });

        }
    }
}
