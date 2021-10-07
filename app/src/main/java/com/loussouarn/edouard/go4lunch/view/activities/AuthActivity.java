package com.loussouarn.edouard.go4lunch.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.loussouarn.edouard.go4lunch.R;


import java.util.Arrays;
import java.util.List;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener {

    //For data
    private final static int RC_SIGN_IN = 123;
    private final static String USER_ID = "userId";


    //For UI
    private Button googleButton;
    private Button facebookButton;
    private Button twitterButton;
    public LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        setupListenerAndView();
    }

    private void setupListenerAndView() {
        linearLayout = findViewById(R.id.auht_atcivity_linear_layout);

        googleButton = findViewById(R.id.auth_activity_google_button);
        facebookButton = findViewById(R.id.auth_activity_facebook_button);
        twitterButton = findViewById(R.id.auth_activity_twitter_button);

        googleButton.setOnClickListener(this);
        facebookButton.setOnClickListener(this);
        twitterButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auth_activity_twitter_button:
                startSignInActivityWithTwitter();
                break;
            default:
                startSignInActivity();
        }
    }

    private void startSignInActivityWithTwitter() {

        FirebaseAuth firebaseAuth;
        firebaseAuth = FirebaseAuth.getInstance();

        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");

        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    showSnackBar(linearLayout, getString(R.string.connection_succeed));
                                    createUserInFireStore();
                                    startMainActivity();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showSnackBar(linearLayout, getString(R.string.error_authentication_canceled));
                                }
                            });
        } else {

            firebaseAuth
                    .startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    showSnackBar(linearLayout, getString(R.string.connection_succeed));
                                    createUserInFireStore();
                                    startMainActivity();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showSnackBar(linearLayout, getString(R.string.error_authentication_canceled));
                                }
                            });
        }


    }

    private void startSignInActivity() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                //   new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


        // Launch the activity
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false, true)
                        .build(),
                RC_SIGN_IN);
    }

    // Management of the return of the connection / registration
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }


    // Show Snack Bar with a message
    private void showSnackBar(LinearLayout linearLayout, String message) {
        Snackbar.make(linearLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    // Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            // SUCCESS
            if (resultCode == RESULT_OK) {
                showSnackBar(linearLayout, getString(R.string.connection_succeed));
                createUserInFireStore();
                startMainActivity();
            } else {
                // ERRORS
                if (response == null) {
                    showSnackBar(linearLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError() != null) {
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        showSnackBar(linearLayout, getString(R.string.error_no_internet));
                    } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                        showSnackBar(linearLayout, getString(R.string.error_unknown_error));
                    }
                }
            }
        }
    }

    // User management

    @Nullable
    protected FirebaseUser getCurrentUser(){ return  FirebaseAuth.getInstance().getCurrentUser(); }

    private void createUserInFireStore(){

        if(getCurrentUser() != null){
            String uId  =this.getCurrentUser().getUid();
            String userName = this.getCurrentUser().getDisplayName();
            String email = this.getCurrentUser().getEmail();
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;

       //     UserFirebase.createUser(uId, userName, email, urlPicture ).addOnFailureListener(this.onFailureListener());
        }
    }

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }

    // Recover the user id before launching activity
    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(USER_ID, this.getCurrentUser().getUid());

        startActivity(intent);
        finish();
    }
}