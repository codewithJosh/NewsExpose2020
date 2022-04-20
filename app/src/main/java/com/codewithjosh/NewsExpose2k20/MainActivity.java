package com.codewithjosh.NewsExpose2k20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    Button btn_login, btn_register;

    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);

//        TODO: FOUND ISSUE: DISABLE BACK BUTTON
        btn_login.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        btn_register.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

    }

}