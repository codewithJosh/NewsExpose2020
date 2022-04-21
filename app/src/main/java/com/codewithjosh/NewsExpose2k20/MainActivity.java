package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    Button btn_login, btn_register;
    LinearLayout is_loading;
    RelativeLayout is_unsupported;
    TextView tv_version_name;

    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

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

                        if (value != null) {

                            if (value.exists()) onCheckCurrentAuthState();
                            else {

                                final String s_version_name = BuildConfig.VERSION_NAME;

                                is_loading.setVisibility(View.GONE);
                                is_unsupported.setVisibility(View.VISIBLE);
                                tv_version_name.setText(s_version_name);
                            }
                        }
                    });
        }
        else Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        is_loading = findViewById(R.id.is_loading);
        is_unsupported = findViewById(R.id.is_unsupported);
        tv_version_name = findViewById(R.id.tv_version_name);

        btn_login.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btn_register.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

    }

    private void onCheckCurrentAuthState() {

        is_loading.setVisibility(View.GONE);
        is_unsupported.setVisibility(View.GONE);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

}