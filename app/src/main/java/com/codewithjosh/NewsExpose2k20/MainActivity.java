package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    Button navLogin;
    Button navRegister;
    ConstraintLayout isLoading;
    ConstraintLayout isUnsupported;
    TextView tvVersionName;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;
    SharedPreferences.Editor editor;

    @Override
    protected void onStart() {

        super.onStart();

        isLoading.setVisibility(View.VISIBLE);

        firebaseFirestore = FirebaseFirestore.getInstance();

        final String versionCode = String.valueOf(BuildConfig.VERSION_CODE);

        if (isConnected())

            firebaseFirestore
                    .collection("Supports")
                    .document(versionCode)
                    .addSnapshotListener((value, error) ->
                    {

                        if (value != null) {

                            if (value.exists()) checkCurrentAuthState();

                            else onUnsupported();

                        }

                    });

        else Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void checkCurrentAuthState() {

        isLoading.setVisibility(View.GONE);
        isUnsupported.setVisibility(View.GONE);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {

            final String userId = firebaseUser.getUid();

            editor.putString("user_id", userId);
            editor.apply();

            firebaseFirestore
                    .collection("Users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot ->
                    {

                        final UserModel user = documentSnapshot.toObject(UserModel.class);

                        if (user != null) {

                            final String userContact = user.getUser_contact();
                            final boolean userIsVerified = user.isUser_is_verified();

                            if (userIsVerified) startActivity(new Intent(this, HomeActivity.class));

                            else {

                                editor.putString("user_contact", userContact);
                                editor.apply();
                                startActivity(new Intent(this, PhoneNumberActivity.class));

                            }

                            finish();

                        }

                    });

        }

    }

    private void onUnsupported() {

        final String versionName = BuildConfig.VERSION_NAME;

        isLoading.setVisibility(View.GONE);
        isUnsupported.setVisibility(View.VISIBLE);
        tvVersionName.setText(versionName);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initSharedPref();
        buildButtons();

    }

    private void initViews() {

        navLogin = findViewById(R.id.nav_login);
        navRegister = findViewById(R.id.nav_register);
        isLoading = findViewById(R.id.is_loading);
        isUnsupported = findViewById(R.id.is_unsupported);
        tvVersionName = findViewById(R.id.tv_version_name);

    }

    private void initSharedPref() {

        editor = getSharedPreferences("user", MODE_PRIVATE).edit();

    }

    private void buildButtons() {

        navLogin.setOnClickListener(v ->
        {

            startActivity(new Intent(this, LoginActivity.class));
            finish();

        });

        navRegister.setOnClickListener(v ->
        {

            startActivity(new Intent(this, RegisterActivity.class));
            finish();

        });

    }

}