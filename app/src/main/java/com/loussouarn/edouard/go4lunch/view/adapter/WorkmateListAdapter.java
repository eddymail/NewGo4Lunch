package com.loussouarn.edouard.go4lunch.view.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.model.User;
import com.loussouarn.edouard.go4lunch.utils.DateFormat;

public class WorkmateListAdapter extends FirestoreRecyclerAdapter<User, WorkmateListAdapter.WorkmatesListViewHolder> {

    private final static String TAG = "WorkmateAdapter";
    private RequestManager glide;
    private Context context;
    private OnItemClickListener mListener;

    public WorkmateListAdapter(@NonNull FirestoreRecyclerOptions<User> options, RequestManager glide) {
        super(options);
        this.glide = glide;
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmatesListViewHolder workmatesListViewHolder, int i, @NonNull User user) {
        DateFormat forToday = new DateFormat();
        String today = forToday.getTodayDate();
        Log.e(TAG, "WorkmateListAdapter date du jour = " + today);
        String registeredDate = user.getRestaurantChoiceDate();
        Log.e(TAG, "restaurantOfTheDayName() = " + user.getRestaurantOfTheDayName());

        // Update Pictures
        if (user.getUrlPicture() != null && !user.getUrlPicture().isEmpty()) {
            String urlPhoto = user.getUrlPicture();
            glide.load(urlPhoto)
                    .apply(RequestOptions.circleCropTransform())
                    .into(workmatesListViewHolder.picture);
        } else {
            workmatesListViewHolder.picture.setImageResource(R.drawable.ic_baseline_people_24);
        }

        // Default values
        String text;

        text = user.getUserName() + context.getString(R.string.list_workmates_adapter_hasnt_decided_yet);
        workmatesListViewHolder.textUser.setTypeface(null, Typeface.ITALIC);
        workmatesListViewHolder.textUser.setTextColor(context.getResources().getColor(R.color.colorGrey));


        // Specifications if a restaurant was chosen for today
        if (user.getRestaurantOfTheDayName() != null && !user.getRestaurantOfTheDayName().isEmpty()) {
            if (registeredDate.equals(today)) {
                text = user.getUserName() + context.getString(R.string.list_workmates_adapter_decided) + user.getRestaurantOfTheDayName();
                workmatesListViewHolder.textUser.setTypeface(null, Typeface.NORMAL);
                workmatesListViewHolder.textUser.setTextColor(context.getResources().getColor(R.color.black));
                Log.e(TAG, "color Black");
            }
        }

        Log.e(TAG, "color Grey");
        workmatesListViewHolder.textUser.setText(text);

    }

    @NonNull
    @Override
    public WorkmatesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_workmates, parent, false);
        context = parent.getContext();
        return new WorkmatesListViewHolder(view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    class WorkmatesListViewHolder extends RecyclerView.ViewHolder {
        TextView textUser;
        ImageView picture;

        WorkmatesListViewHolder(@NonNull View itemView) {
            super(itemView);
            textUser = itemView.findViewById(R.id.item_list_workmates_txt);
            picture = itemView.findViewById(R.id.item_list_workmates_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (mListener != null) {
                        mListener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

}