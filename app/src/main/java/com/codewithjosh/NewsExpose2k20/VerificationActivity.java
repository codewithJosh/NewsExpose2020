package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    Button btn_submit;
    EditText et_otp;
    LinearLayout btn_resend, is_loading;
    TextView tv_resend;

    String s_user_contact, s_otp, s_verification_id;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    DocumentReference documentRef;

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        initViews();
        initInstance();
        load();
        sendVerificationCode();
        buildButton();

    }

    private void initViews() {

        btn_submit = findViewById(R.id.btn_submit);
        et_otp = findViewById(R.id.et_otp);
        btn_resend = findViewById(R.id.btn_resend);
        is_loading = findViewById(R.id.is_loading);
        tv_resend = findViewById(R.id.tv_resend);

    }

    private void initInstance() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void load() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        s_user_contact = sharedPref.getString("s_user_contact", String.valueOf(MODE_PRIVATE));

    }

    // TODO: DEBUG
    private void sendVerificationCode() {

        is_loading.setVisibility(View.VISIBLE);

        if (!isConnected()) {

            is_loading.setVisibility(View.GONE);
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        } else {

            new CountDownTimer(60000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                    tv_resend.setText(String.valueOf(millisUntilFinished / 1000));
                    btn_resend.setEnabled(false);

                }

                @Override
                public void onFinish() {

                    tv_resend.setText(R.string.text_resend);
                    btn_resend.setEnabled(true);

                }

            }.start();

            PhoneAuthOptions options = PhoneAuthOptions
                    .newBuilder(firebaseAuth)
                    .setPhoneNumber(s_user_contact)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {

                            is_loading.setVisibility(View.GONE);
                            Toast.makeText(VerificationActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                            is_loading.setVisibility(View.GONE);
                            Toast.makeText(VerificationActivity.this, "You'll receive your OTP shortly", Toast.LENGTH_SHORT).show();
                            VerificationActivity.this.s_verification_id = s;

                        }
                    }).build();

            PhoneAuthProvider.verifyPhoneNumber(options);
        }

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void buildButton() {

        btn_resend.setOnClickListener(v -> {

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
            sendVerificationCode();
        });

        btn_submit.setOnClickListener(v -> {

            s_otp = et_otp.getText().toString();

            if (validate(v)) onSubmit();

            else is_loading.setVisibility(View.GONE);

        });

    }

    private boolean validate(final View v) {

        is_loading.setVisibility(View.VISIBLE);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected()) Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (s_otp.isEmpty()) Toast.makeText(this, "OTP is required!", Toast.LENGTH_SHORT).show();

        else if (s_otp.length() != 6) Toast.makeText(this, "OTP must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else return true;

        return false;

    }

    private void onSubmit() {

        final PhoneAuthCredential credential = PhoneAuthProvider.getCredential(s_verification_id, s_otp);
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {

            firebaseUser
                    .linkWithCredential(credential)
                    .addOnSuccessListener(authResult -> {

                        final String s_user_id = firebaseUser.getUid();
                        final boolean user_is_verified = true;

                        final HashMap<String, Object> user = new HashMap<>();
                        user.put("user_is_verified", user_is_verified);

                        updateUser(user, s_user_id);

                    }).addOnFailureListener(e -> {

                is_loading.setVisibility(View.GONE);

                final String _e = e.toString().toLowerCase();

                if (_e.contains("expired")) Toast.makeText(this, "OTP has expired", Toast.LENGTH_SHORT).show();

                else if (_e.contains("invalid")) Toast.makeText(this, "OTP doesn't match", Toast.LENGTH_SHORT).show();

                else Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

            });
        }

    }

    private void updateUser(HashMap<String, Object> user, String s_user_id) {

        documentRef = firebaseFirestore
                .collection("Users")
                .document(s_user_id);

        documentRef
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot != null)

                        if (documentSnapshot.exists())

                            documentRef
                                    .update(user)
                                    .addOnSuccessListener(unused -> {

                                        firebaseAuth.signOut();
                                        Toast.makeText(this, "You're Successfully Added!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    });

                });

    }

}