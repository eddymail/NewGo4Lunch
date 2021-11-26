package com.loussouarn.edouard.go4lunch.view.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loussouarn.edouard.go4lunch.R;

public class SettingsActivity extends AppCompatActivity {

    public static final String SHARED_PREFERENCES = "sharedPersonalPreferences";
    public static final String NOTIFICATIONS_PREFERENCES = "notifications";
    Button submit;
    private Switch notifications;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        notifications = findViewById(R.id.switch_notifications);
        submit = (Button) findViewById(R.id.submitButton);
        notifications.setChecked(sharedPreferences.getBoolean(NOTIFICATIONS_PREFERENCES, true));

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String statusSwitch1, statusSwitch2;
                if (notifications.isChecked()) {
                    statusSwitch1 = notifications.getTextOn().toString();
                    notifications.setChecked(sharedPreferences.getBoolean(NOTIFICATIONS_PREFERENCES, true));
                } else {
                    statusSwitch1 = notifications.getTextOff().toString();
                    notifications.setChecked(sharedPreferences.getBoolean(NOTIFICATIONS_PREFERENCES, false));

                }
                editor = sharedPreferences.edit();
                Toast.makeText(getApplicationContext(), statusSwitch1, Toast.LENGTH_SHORT).show(); // display the current state for switch's
                finish();

            }
        });
    }
}