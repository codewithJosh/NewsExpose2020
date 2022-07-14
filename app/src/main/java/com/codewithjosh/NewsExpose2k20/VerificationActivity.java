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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

    Button btnSubmit;
    EditText etOTP;
    ConstraintLayout btnResend;
    ConstraintLayout isLoading;
    TextView tvResend;
    String userContact;
    String OTP;
    String verificationId;
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
        initInstances();
        initSharedPref();
        load();
        sendVerificationCode();
        buildButtons();

    }

    private void initViews() {

        btnSubmit = findViewById(R.id.btn_submit);
        etOTP = findViewById(R.id.et_o_t_p);
        btnResend = findViewById(R.id.btn_resend);
        isLoading = findViewById(R.id.is_loading);
        tvResend = findViewById(R.id.tv_resend);

    }

    private void initInstances() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);

    }

    private void load() {

        userContact = sharedPref.getString("user_contact", String.valueOf(MODE_PRIVATE));

    }

    // TODO: DEBUG
    private void sendVerificationCode() {

        isLoading.setVisibility(View.VISIBLE);

        if (isConnected()) {

            new CountDownTimer(60000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                    tvResend.setText(String.valueOf(millisUntilFinished / 1000));
                    btnResend.setEnabled(false);

                }

                @Override
                public void onFinish() {

                    tvResend.setText(R.string.text_resend);
                    btnResend.setEnabled(true);

                }

            }.start();

            PhoneAuthOptions options = PhoneAuthOptions
                    .newBuilder(firebaseAuth)
                    .setPhoneNumber(userContact)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {

                            isLoading.setVisibility(View.GONE);
                            Toast.makeText(VerificationActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                            isLoading.setVisibility(View.GONE);
                            Toast.makeText(VerificationActivity.this, "You'll receive your OTP shortly", Toast.LENGTH_SHORT).show();
                            VerificationActivity.this.verificationId = s;

                        }

                    }).build();

            PhoneAuthProvider.verifyPhoneNumber(options);

        } else {

            isLoading.setVisibility(View.GONE);
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        }

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void buildButtons() {

        btnResend.setOnClickListener(v ->
        {

            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
            sendVerificationCode();

        });

        btnSubmit.setOnClickListener(v ->
        {

            OTP = etOTP.getText().toString();

            if (validate(v)) onSubmit();

            else isLoading.setVisibility(View.GONE);

        });

    }

    private boolean validate(final View v) {

        isLoading.setVisibility(View.VISIBLE);
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected())
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (OTP.isEmpty()) Toast.makeText(this, "OTP is required!", Toast.LENGTH_SHORT).show();

        else if (OTP.length() != 6)
            Toast.makeText(this, "OTP must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else return true;

        return false;

    }

    private void onSubmit() {

        final PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, OTP);
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)

            firebaseUser
                    .linkWithCredential(credential)
                    .addOnSuccessListener(authResult ->
                    {

                        final String userId = firebaseUser.getUid();
                        final boolean userIsVerified = true;

                        final HashMap<String, Object> user = new HashMap<>();
                        user.put("user_is_verified", userIsVerified);

                        updateUser(userId, user);

                    }).addOnFailureListener(e ->
                    {

                        isLoading.setVisibility(View.GONE);

                        final String _e = e.toString().toLowerCase();

                        if (_e.contains("expired"))
                            Toast.makeText(this, "OTP has expired", Toast.LENGTH_SHORT).show();

                        else if (_e.contains("invalid"))
                            Toast.makeText(this, "OTP doesn't match", Toast.LENGTH_SHORT).show();

                        else
                            Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

                    });

    }

    private void updateUser(final String userId, final HashMap<String, Object> user) {

        documentRef = firebaseFirestore
                .collection("Users")
                .document(userId);

        documentRef
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {

                    if (documentSnapshot != null && documentSnapshot.exists())

                        documentRef
                                .update(user)
                                .addOnSuccessListener(unused ->
                                {

                                    firebaseAuth.signOut();
                                    Toast.makeText(this, "You're Successfully Added!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();

                                });

                });

    }

}