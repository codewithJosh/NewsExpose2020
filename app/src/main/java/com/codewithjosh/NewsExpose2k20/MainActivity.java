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

    Button nav_login, nav_register;
    ConstraintLayout is_loading, is_unsupported;
    TextView tv_version_name;

    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onStart() {
        super.onStart();

        is_loading.setVisibility(View.VISIBLE);

        firebaseFirestore = FirebaseFirestore.getInstance();

        final String s_version_code = String.valueOf(BuildConfig.VERSION_CODE);

        if (isConnected()) {

            firebaseFirestore
                    .collection("Supports")
                    .document(s_version_code)
                    .addSnapshotListener((value, error) -> {

                        if (value != null)

                            if (value.exists()) checkCurrentAuthState();

                            else onUnsupported();

                    });
        } else Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void checkCurrentAuthState() {

        is_loading.setVisibility(View.GONE);
        is_unsupported.setVisibility(View.GONE);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {

            final String s_user_id = firebaseUser.getUid();

            firebaseFirestore
                    .collection("Users")
                    .document(s_user_id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        final UserModel user = documentSnapshot.toObject(UserModel.class);

                        if (user != null) {

                            final String s_user_contact = user.getUser_contact();
                            final boolean user_is_verified = user.isUser_is_verified();

                            if (user_is_verified)
                                startActivity(new Intent(this, HomeActivity.class));
                            else {

                                editor.putString("s_user_contact", s_user_contact);
                                editor.apply();
                                startActivity(new Intent(this, PhoneNumberActivity.class));
                            }
                            finish();
                        }
                    });
        }

    }

    private void onUnsupported() {

        final String s_version_name = BuildConfig.VERSION_NAME;

        is_loading.setVisibility(View.GONE);
        is_unsupported.setVisibility(View.VISIBLE);
        tv_version_name.setText(s_version_name);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initSharedPref();
        buildNavButton();

    }

    private void initView() {

        nav_login = findViewById(R.id.nav_login);
        nav_register = findViewById(R.id.nav_register);
        is_loading = findViewById(R.id.is_loading);
        is_unsupported = findViewById(R.id.is_unsupported);
        tv_version_name = findViewById(R.id.tv_version_name);

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void buildNavButton() {

        nav_login.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        nav_register.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

    }

}