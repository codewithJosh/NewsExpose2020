package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

public class LoginActivity extends AppCompatActivity {

    Button btn_login;
    EditText et_user_name, et_password;
    LinearLayout nav_register, is_loading;

    int i_version_code;
    String s_user_name, s_password;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        i_version_code = BuildConfig.VERSION_CODE;

        initViews();
        initInstance();
        initSharedPref();
        buildButton();

    }

    private void initViews() {

        btn_login = findViewById(R.id.btn_login);
        et_user_name = findViewById(R.id.et_user_name);
        et_password = findViewById(R.id.et_password);
        nav_register = findViewById(R.id.nav_register);
        is_loading = findViewById(R.id.is_loading);

    }

    private void initInstance() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void buildButton() {

        nav_register.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        btn_login.setOnClickListener(v -> {

            getString();

            if (validate(v)) checkUserNameFirestore();

            else is_loading.setVisibility(View.GONE);

        });

    }

    private void getString() {

        s_user_name = et_user_name.getText().toString().toLowerCase();
        s_password = et_password.getText().toString();

    }

    private boolean validate(final View v) {

        is_loading.setVisibility(View.VISIBLE);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected()) Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (s_user_name.isEmpty() || s_password.isEmpty()) Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();

        else if (s_password.length() < 6) Toast.makeText(this, "Password Must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else return true;

        return false;

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onLogin(final UserModel user) {

        final String s_email = user.getUser_email();

        if (s_email != null)

            firebaseAuth
                    .signInWithEmailAndPassword(s_email, s_password)
                    .addOnSuccessListener(authResult -> {

                        firebaseUser = authResult.getUser();

                        if (firebaseUser != null) {

                            final int i_user_version_code = user.getUser_version_code();
                            final String s_user_id = firebaseUser.getUid();

                            editor.putString("s_user_id", s_user_id);
                            editor.apply();

                            if (i_user_version_code == i_version_code) checkCurrentUserVerified(user);

                            else if (i_user_version_code > i_version_code) {

                                firebaseAuth.signOut();
                                Toast.makeText(LoginActivity.this, "Your account is incompatible to this version!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {

                                firebaseFirestore
                                        .collection("Users")
                                        .document(s_user_id)
                                        .set(onMigrate(user))
                                        .addOnSuccessListener(runnable -> checkCurrentUserVerified(user));
                            }
                        }

                    }).addOnFailureListener(e -> {

                is_loading.setVisibility(View.GONE);

                final String s_e = e.toString().toLowerCase();

                if (s_e.contains("the password is invalid or the user does not have a password")) Toast.makeText(this, "Incorrect Password!", Toast.LENGTH_SHORT).show();

                else if (s_e.contains("a network error (such as timeout, interrupted connection or unreachable host) has occurred")) Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                else Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

            });

    }

    private void checkUserNameFirestore() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", s_user_name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots != null)

                        if (!queryDocumentSnapshots.isEmpty())

                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                final UserModel user = snapshot.toObject(UserModel.class);
                                onLogin(user);
                            }
                    else checkUserNameRealtime();

                });

    }

    private void checkUserNameRealtime() {

        firebaseDatabase
                .getReference("Users")
                .orderByChild("user_name")
                .equalTo(s_user_name)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists())

                            for (DataSnapshot _snapshot : snapshot.getChildren()) {

                                final UserModel user = _snapshot.getValue(UserModel.class);
                                if (user != null) onLogin(user);
                            }
                        else {

                            is_loading.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "User Doesn't Exist!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void checkCurrentUserVerified(final UserModel user) {

        final String s_user_contact = user.getUser_contact();
        final boolean user_is_verified = user.isUser_is_verified();

        if (user_is_verified) {

            Toast.makeText(this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, HomeActivity.class));
        } else {

            editor.putString("s_user_contact", s_user_contact);
            editor.apply();
            startActivity(new Intent(this, PhoneNumberActivity.class));
        }
        finish();

    }

    private UserModel onMigrate(final UserModel user) {

        return new UserModel(
                user.getUser_bio(),
                user.getUser_email(),
                user.getUser_id(),
                user.getUser_image(),
                user.isUser_is_admin(),
                user.getUser_name(),
                i_version_code
        );

    }

}
