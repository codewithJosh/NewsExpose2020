package com.codewithjosh.NewsExpose2k20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    Button btn_login;
    EditText et_email, et_password;
    TextView nav_register;

    int i_version_code;

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

        i_version_code = BuildConfig.VERSION_CODE;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        nav_register.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        btn_login.setOnClickListener(v -> {
            pd = new ProgressDialog(this);
            pd.setMessage("Logging in");
            pd.show();

            final String s_email = et_email.getText().toString();
            final String s_password = et_password.getText().toString();

            if (s_email.isEmpty() || s_password.isEmpty()) {
                pd.dismiss();
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else {

                firebaseAuth
                        .signInWithEmailAndPassword(s_email, s_password)
                        .addOnSuccessListener(authResult -> {

                            final String s_user_id = authResult.getUser().getUid();

                            databaseRef = firebaseDatabase
                                    .getReference("Users")
                                    .child(s_user_id);

                            databaseRef
                                    .get()
                                    .addOnSuccessListener(dataSnapshot -> {

                                        final UserModel getUser = dataSnapshot.getValue(UserModel.class);

                                        final int i_user_version_code = getUser.getUser_version_code();

                                        if (i_user_version_code == i_version_code) {

                                            pd.dismiss();
                                            startActivity(new Intent(this, HomeActivity.class));
                                            finish();
                                        } else if (i_user_version_code > i_version_code) {

                                            firebaseAuth.signOut();
                                            Toast.makeText(this, "Your account is incompatible to this version!", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(this, MainActivity.class));
                                            finish();
                                        } else {

                                            final UserModel setUser = onMigrate(getUser, i_user_version_code);

                                            databaseRef
                                                    .setValue(setUser)
                                                    .addOnSuccessListener(runnable -> {

                                                        pd.dismiss();
                                                        startActivity(new Intent(this, HomeActivity.class));
                                                        finish();
                                                    });
                                        }

                                    }).addOnFailureListener(e -> pd.dismiss());


                        }).addOnFailureListener(e -> {

                    pd.dismiss();
                    Toast.makeText(this, "Incorrect Email or Password", Toast.LENGTH_SHORT).show();
                });
            }
        });

    }

    private UserModel onMigrate(final UserModel getUser, final int i_user_version_code) {

        UserModel user = new UserModel();

        if (i_user_version_code < 5) {

            user = new UserModel(
                    getUser.getUser_bio(),
                    getUser.getUser_email(),
                    getUser.getUser_id(),
                    getUser.getUser_image(),
                    getUser.isUser_is_admin(),
                    getUser.getUser_name(),
                    i_version_code
            );
        }
        return user;

    }

}
