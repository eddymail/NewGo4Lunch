package com.loussouarn.edouard.go4lunch.utils;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.api.RestaurantFirebase;
import com.loussouarn.edouard.go4lunch.api.UserFirebase;
import com.loussouarn.edouard.go4lunch.model.Restaurant;
import com.loussouarn.edouard.go4lunch.model.User;
import com.loussouarn.edouard.go4lunch.view.activities.AuthActivity;

import java.util.ArrayList;
import java.util.List;

public class NotificationService extends FirebaseMessagingService {
    public static final String SHARED_PREFS = "SharedPrefsPerso";
    public static final String NOTIFICATIONS_PREFS = "notifications";
    private static final String TAG = "NotificationsService";
    private static final int NOTIFICATION_ID = 0;
    private static final String NOTIFICATION_TAG = "FIREBASEOC";
    private String userId;
    private String restaurantOfTheDayId;
    private String restaurantOfTheDayName;
    private String restaurantOfTheDayAddress;
    private List<String> listUserId = new ArrayList<>();
    private String listNames = "";
    private boolean notificationsIsOk;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Test Firebase notification service
        if(remoteMessage.getNotification() != null) {
            Log.e("Test", "onMessageReceived remoteMessage.getNotification() = " + remoteMessage.getNotification());
            //Get Message send by Firebase
            RemoteMessage.Notification testNotification = remoteMessage.getNotification();
            //Show message in console
            Log.e("Test", "onMessageReceived" + testNotification);
        }

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        notificationsIsOk = sharedPreferences.getBoolean(NOTIFICATIONS_PREFS, true);
        userId = UserFirebase.getCurrentUserId();

        // Check if the user wants to receive notifications
        checkIfNotificationOfTheDay();
    }


    private void checkIfNotificationOfTheDay() {
        DateFormat forToday = new DateFormat();
        final String today = forToday.getTodayDate();

        // Check if user has selected a restaurant for today
        UserFirebase.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                String restaurantOfTheDay;
                if (user != null) {
                    restaurantOfTheDay = user.getRestaurantOfTheDay();
                    String registeredDate = user.getRestaurantChoiceDate();
                    if (!restaurantOfTheDay.isEmpty() && registeredDate.equals(today) && notificationsIsOk) {
                        showNotification();
                    }
                }
            }
        });
    }

    private void showNotification() {
        UserFirebase.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    restaurantOfTheDayId = user.getRestaurantOfTheDay();
                }
                RestaurantFirebase.getRestaurant(restaurantOfTheDayId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Restaurant restaurant = documentSnapshot.toObject((Restaurant.class));
                        if (restaurant != null) {
                            restaurantOfTheDayName = restaurant.getRestaurantName();
                            restaurantOfTheDayAddress = restaurant.getAddress();
                            //Recover the workmates list who have chosen this restaurant
                            listUserId = restaurant.getClientsTodayList();
                        }
                        for (int i = 0; i < listUserId.size(); i++) {
                            UserFirebase.getUser(listUserId.get(i)).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    User user = documentSnapshot.toObject(User.class);
                                    String name = null;
                                    if (user != null) {
                                        name = user.getUserName();
                                    }
                                    listNames += name + ", ";

                                    String restaurantName = getResources().getString(R.string.notification_message_today_restaurant) + " " + restaurantOfTheDayName;
                                    String address = restaurantOfTheDayAddress;
                                    String workmates = listNames;
                                    if (workmates.endsWith(", "))
                                        workmates = workmates.substring(0, workmates.length() - 2);

                                    sendVisualNotification(restaurantName, address, workmates);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void sendVisualNotification(String restaurantName, String address, String colleagues) {
        //  Create an Intent that will be shown when user will click on the Notification
        Intent intent = new Intent(this, AuthActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //  Create a Style for the Notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.notification_title));
        inboxStyle.addLine(restaurantName);
        inboxStyle.addLine(address);
        inboxStyle.addLine(getResources().getString(R.string.notification_message_workmates));
        inboxStyle.addLine(colleagues);

        //  Create a Channel (Android 8)
        String channelId = getString(R.string.default_notification_channel_id);

        //  Build a Notification object
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(restaurantName)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        //  Add the Notification to the Notification Manager and show it.
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //  Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Firebase Message";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }

        //  Show notification
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }

}
