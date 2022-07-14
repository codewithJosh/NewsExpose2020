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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    EditText etUserName;
    EditText etPassword;
    ConstraintLayout navRegister;
    ConstraintLayout isLoading;
    int versionCode;
    String userName;
    String password;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        versionCode = BuildConfig.VERSION_CODE;

        initViews();
        initInstances();
        initSharedPref();
        buildButtons();

    }

    private void initViews() {

        btnLogin = findViewById(R.id.btn_login);
        etUserName = findViewById(R.id.et_user_name);
        etPassword = findViewById(R.id.et_password);
        navRegister = findViewById(R.id.nav_register);
        isLoading = findViewById(R.id.is_loading);

    }

    private void initInstances() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        editor = getSharedPreferences("user", MODE_PRIVATE).edit();

    }

    private void buildButtons() {

        navRegister.setOnClickListener(v ->
        {

            startActivity(new Intent(this, RegisterActivity.class));
            finish();

        });

        btnLogin.setOnClickListener(v ->
        {

            getString();

            if (validate(v)) checkUserNameFirestore();

            else isLoading.setVisibility(View.GONE);

        });

    }

    private void getString() {

        userName = etUserName.getText().toString().toLowerCase();
        password = etPassword.getText().toString();

    }

    private boolean validate(final View v) {

        isLoading.setVisibility(View.VISIBLE);
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected())
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (userName.isEmpty() || password.isEmpty())
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();

        else if (password.length() < 6)
            Toast.makeText(this, "Password Must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else return true;

        return false;

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onLogin(final UserModel user) {

        final String email = user.getUser_email();

        if (email != null)

            firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult ->
                    {

                        firebaseUser = authResult.getUser();

                        if (firebaseUser != null) {

                            final int userVersionCode = user.getUser_version_code();
                            final String userId = firebaseUser.getUid();

                            editor.putString("user_id", userId);
                            editor.apply();

                            if (userVersionCode == versionCode) checkCurrentUserVerified(user);

                            else if (userVersionCode > versionCode) {

                                firebaseAuth.signOut();
                                Toast.makeText(this, "Your account is incompatible to this version!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();

                            } else {

                                user.setUser_version_code(versionCode);

                                firebaseFirestore
                                        .collection("Users")
                                        .document(userId)
                                        .set(user)
                                        .addOnSuccessListener(runnable -> checkCurrentUserVerified(user));

                            }

                        }

                    }).addOnFailureListener(e ->
                    {

                        isLoading.setVisibility(View.GONE);

                        final String _e = e.toString().toLowerCase();

                        if (_e.contains("the password is invalid or the user does not have a password"))
                            Toast.makeText(this, "Incorrect Password!", Toast.LENGTH_SHORT).show();

                        else if (_e.contains("a network error (such as timeout, interrupted connection or unreachable host) has occurred"))
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                        else
                            Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

                    });

    }

    private void checkUserNameFirestore() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", userName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {

                    if (queryDocumentSnapshots != null) {

                        if (!queryDocumentSnapshots.isEmpty())

                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                final UserModel user = snapshot.toObject(UserModel.class);
                                onLogin(user);

                            }

                        else checkUserNameRealtime();

                    }

                });

    }

    private void checkUserNameRealtime() {

        firebaseDatabase
                .getReference("Users")
                .orderByChild("user_name")
                .equalTo(userName)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists())

                            for (DataSnapshot _snapshot : snapshot.getChildren()) {

                                final UserModel user = _snapshot.getValue(UserModel.class);
                                if (user != null) onLogin(user);

                            }

                        else {

                            isLoading.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "User Doesn't Exist!", Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

    }

    private void checkCurrentUserVerified(final UserModel user) {

        final String userContact = user.getUser_contact();
        final boolean userIsVerified = user.isUser_is_verified();

        if (userIsVerified) {

            Toast.makeText(this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, HomeActivity.class));

        } else {

            editor.putString("user_contact", userContact);
            editor.apply();
            startActivity(new Intent(this, PhoneNumberActivity.class));

        }

        finish();

    }

}
