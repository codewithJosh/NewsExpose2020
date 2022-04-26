package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    Button btn_login;
    EditText et_user_name, et_password;
    LinearLayout nav_register, is_loading;

    int i_version_code;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btn_login);
        et_user_name = findViewById(R.id.et_user_name);
        et_password = findViewById(R.id.et_password);
        nav_register = findViewById(R.id.nav_register);
        is_loading = findViewById(R.id.is_loading);

        i_version_code = BuildConfig.VERSION_CODE;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        nav_register.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        btn_login.setOnClickListener(v -> {

            is_loading.setVisibility(View.VISIBLE);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

            final String s_user_name = et_user_name.getText().toString().toLowerCase();
            final String s_password = et_password.getText().toString();

            if (!isConnected()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            } else if (s_user_name.isEmpty() || s_password.isEmpty()) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else if (s_password.length() < 6) {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Password Must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {

                firebaseFirestore
                        .collection("Users")
                        .whereEqualTo("user_name", s_user_name)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {

                            if (!queryDocumentSnapshots.isEmpty()) {

                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                    final UserModel user = snapshot.toObject(UserModel.class);
                                    onLogin(user, s_password);
                                }
                            } else {

                                firebaseDatabase
                                        .getReference("Users")
                                        .orderByChild("user_name")
                                        .equalTo(s_user_name)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {

                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists()) {

                                                    for (DataSnapshot _snapshot : snapshot.getChildren()) {

                                                        final String s_user_bio = String.valueOf(_snapshot.child("user_bio").getValue());
                                                        final String s_user_email = String.valueOf(_snapshot.child("user_email").getValue());
                                                        final String s_user_id = String.valueOf(_snapshot.child("user_id").getValue());
                                                        final String s_user_image = String.valueOf(_snapshot.child("user_image").getValue());
                                                        final boolean user_is_admin = Boolean.getBoolean(String.valueOf(_snapshot.child("user_is_admin").getValue()));
                                                        final int i_user_version_code = Integer.parseInt(String.valueOf(_snapshot.child("user_version_code").getValue()));

                                                        final UserModel user = new UserModel(
                                                                s_user_bio,
                                                                s_user_email,
                                                                s_user_id,
                                                                s_user_image,
                                                                user_is_admin,
                                                                s_user_name,
                                                                i_user_version_code
                                                        );

                                                        onLogin(user, s_password);
                                                    }
                                                } else {
                                                    is_loading.setVisibility(View.GONE);
                                                    Toast.makeText(LoginActivity.this, "User Doesn't Exist!", Toast.LENGTH_SHORT).show();
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        });
            }
        });

    }

    private void onLogin(final UserModel user, final String s_password) {

        final String s_email = user.getUser_email();

        if (s_email != null)

            firebaseAuth
                    .signInWithEmailAndPassword(s_email, s_password)
                    .addOnSuccessListener(authResult -> {

                        final int i_user_version_code = user.getUser_version_code();
                        final String s_user_id = authResult.getUser().getUid();

                        if (i_user_version_code == i_version_code) {

                            checkCurrentUserVerified(user, s_user_id);
                        } else if (i_user_version_code > i_version_code) {

                            is_loading.setVisibility(View.GONE);
                            firebaseAuth.signOut();
                            Toast.makeText(this, "Your account is incompatible to this version!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {

                            firebaseFirestore
                                    .collection("Users")
                                    .document(s_user_id)
                                    .set(user)
                                    .addOnSuccessListener(runnable -> checkCurrentUserVerified(user, s_user_id));
                        }

                    }).addOnFailureListener(e -> {

                if (e.toString().contains("The password is invalid or the user does not have a password")) {
                    is_loading.setVisibility(View.GONE);
                    ;
                    Toast.makeText(LoginActivity.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                } else if (e.toString().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred")) {
                    is_loading.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                } else {
                    is_loading.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();
                }
            });

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void checkCurrentUserVerified(final UserModel user, final String s_user_id) {

        final String s_user_contact = user.getUser_contact();
        final boolean user_is_verified = user.isUser_is_verified();

        if (user_is_verified) {

            is_loading.setVisibility(View.GONE);
            Toast.makeText(this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, HomeActivity.class));
        }
        else {

            Intent intent = new Intent(this, PhoneNumberActivity.class);
            intent.putExtra("s_user_id", s_user_id);
            intent.putExtra("s_user_contact", s_user_contact);
            startActivity(intent);
        }
        finish();

    }

}
