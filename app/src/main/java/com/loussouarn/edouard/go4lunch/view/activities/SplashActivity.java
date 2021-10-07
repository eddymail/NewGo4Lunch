package com.loussouarn.edouard.go4lunch.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.loussouarn.edouard.go4lunch.R;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    private static final String USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkIfUserLogged();

    }

    // If User is logged launch MainActivity, if not launch AuthActivity
    public void checkIfUserLogged() {
        if (this.isCurrentUserLogged()) {
            this.startMainActivity();
        } else {
            this.startAuthActivity();
        }
    }

    // Recover the user id before launching activity
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(USER_ID, Objects.requireNonNull(this.getCurrentUser()).getUid());
        startActivity(intent);
        finish();
    }

    private void startAuthActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    @Nullable
    protected FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    protected Boolean isCurrentUserLogged() {
        return (this.getCurrentUser() != null);
    }
}