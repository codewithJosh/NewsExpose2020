package com.codewithjosh.NewsExpose2k20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    Button btn_register;
    EditText et_user_name, et_email, et_password, et_re_password;
    TextView nav_login;

    String s_user_name, s_email, s_password, s_re_password;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    DatabaseReference databaseRef;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn_register = findViewById(R.id.btn_register);
        et_user_name = findViewById(R.id.et_user_name);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_re_password = findViewById(R.id.et_re_password);
        nav_login = findViewById(R.id.nav_login);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        nav_login.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btn_register.setOnClickListener(v -> {
            pd = new ProgressDialog(this);
            pd.setMessage("Signing up");
            pd.show();

            s_user_name = et_user_name.getText().toString();
            s_email = et_email.getText().toString();
            s_password = et_password.getText().toString();
            s_re_password = et_re_password.getText().toString();

            if (s_user_name.isEmpty() || s_email.isEmpty()
                    || s_password.isEmpty() || s_re_password.isEmpty()) {
                pd.dismiss();
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else if (s_password.length() < 6) {
                pd.dismiss();
                Toast.makeText(this, "Password must at least 6 characters", Toast.LENGTH_SHORT).show();
            } else if (!s_password.equals(s_re_password)) {
                pd.dismiss();
                Toast.makeText(this, "Password doesn't match!", Toast.LENGTH_SHORT).show();
            } else onRegister(s_user_name, s_email, s_password);
        });

    }

    private void onRegister(final String s_user_name, final String s_email, final String s_password) {

        firebaseAuth
                .createUserWithEmailAndPassword(s_email, s_password)
                .addOnSuccessListener(authResult -> {

                    final String s_user_id = authResult.getUser().getUid();

                    databaseRef = firebaseDatabase
                            .getReference()
                            .child("Users")
                            .child(s_user_id);

//                    TODO: FOUND ISSUE: UPDATE THE CREDENTIALS
                    HashMap<String, Object> user = new HashMap<>();
                    user.put("id", s_user_id);
                    user.put("username", s_user_name);
                    user.put("bio", "");
                    user.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/news-expose-2k20.appspot.com/o/20220410_Res%2FDefaultUserImage.png?alt=media&token=20");
                    user.put("admin", false);

                    databaseRef
                            .setValue(user)
                            .addOnSuccessListener(runnable -> {
                                pd.dismiss();
                                Toast.makeText(this, "You're Successfully Added!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            });

                }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(this, "Incorrect Email Address", Toast.LENGTH_SHORT).show();
        });

    }

}
