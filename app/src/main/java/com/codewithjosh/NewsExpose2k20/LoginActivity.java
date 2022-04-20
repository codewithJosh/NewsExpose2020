package com.codewithjosh.NewsExpose2k20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    Button btn_login;
    EditText et_email, et_password;
    TextView nav_register;

    String s_email, s_password;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    DatabaseReference databaseRef;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btn_login);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        nav_register = findViewById(R.id.nav_register);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        nav_register.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        btn_login.setOnClickListener(v -> {
            pd = new ProgressDialog(this);
            pd.setMessage("Logging in");
            pd.show();

            s_email = et_email.getText().toString();
            s_password = et_password.getText().toString();

            if (s_email.isEmpty() || s_password.isEmpty()) {
                pd.dismiss();
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            }
            else {

                firebaseAuth.signInWithEmailAndPassword(s_email, s_password)
                        .addOnSuccessListener(authResult -> {

                            final String s_user_id = firebaseAuth.getCurrentUser().getUid();

                            databaseRef = firebaseDatabase
                                    .getReference()
                                    .child("Users")
                                    .child(s_user_id);

//                            TODO: USE GET METHOD ONCE IT IS AVAILABLE
                            databaseRef
                                    .addValueEventListener(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            pd.dismiss();
                                            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                                            finish();

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            pd.dismiss();

                                        }
                                    });

                        }).addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Incorrect Email or Password", Toast.LENGTH_SHORT).show();
                });
            }
        });

    }

}
