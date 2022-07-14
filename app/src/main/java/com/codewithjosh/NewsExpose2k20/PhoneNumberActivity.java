package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hbb20.CountryCodePicker;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PhoneNumberActivity extends AppCompatActivity {

    final String tag = RegisterActivity.class.getSimpleName();
    final String site = "6LdmXHcfAAAAAAGqu4EGoI8Ihrk8IB78NdM2cKFJ";
    final String secret = "6LdmXHcfAAAAAHMLaAuerAsSOZDkNJYA-gJ8Fma3";
    Button btnNext;
    ConstraintLayout isLoading;
    CheckBox cbRecaptcha;
    CountryCodePicker ccpCountry;
    EditText etContact;
    TextView btnSkip;
    String userId;
    String userContact;
    String contact;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentRef;
    RequestQueue requestQueue;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        initViews();
        initInstances();
        initSharedPref();
        load();
        build();

    }

    private void initViews() {

        btnNext = findViewById(R.id.btn_next);
        cbRecaptcha = findViewById(R.id.cb_recaptcha);
        ccpCountry = findViewById(R.id.ccp_country);
        etContact = findViewById(R.id.et_contact);
        isLoading = findViewById(R.id.is_loading);
        btnSkip = findViewById(R.id.btn_skip);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();
        requestQueue = Volley.newRequestQueue(this);

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void load() {

        userId = sharedPref.getString("user_id", String.valueOf(MODE_PRIVATE));
        userContact = sharedPref.getString("user_contact", String.valueOf(MODE_PRIVATE));
        etContact.setText(userContact);

    }

    private void build() {

        btnSkip.setOnClickListener(v ->
        {

            isLoading.setVisibility(View.VISIBLE);

            if (isConnected()) {

                final String userContact = "";
                final boolean userIsVerified = true;

                final HashMap<String, Object> user = new HashMap<>();
                user.put("user_contact", userContact);
                user.put("user_is_verified", userIsVerified);

                updateUser(user);

            } else {

                isLoading.setVisibility(View.GONE);
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

            }

        });

        btnNext.setOnClickListener(v ->
        {

            contact = etContact.getText().toString();

            if (validate(v)) checkContact();

            else isLoading.setVisibility(View.GONE);

        });

        ccpCountry.registerCarrierNumberEditText(etContact);

        cbRecaptcha.setOnClickListener(v -> onRecaptcha());

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void updateUser(final HashMap<String, Object> user) {

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

                                    Toast.makeText(this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(this, HomeActivity.class));
                                    finish();

                                });

                });

    }

    private boolean validate(final View v) {

        isLoading.setVisibility(View.VISIBLE);
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected())
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (contact.isEmpty())
            Toast.makeText(this, "Phone Number is required!", Toast.LENGTH_SHORT).show();

        else if (!contact.startsWith("09"))
            Toast.makeText(this, "Provide a valid Phone Number", Toast.LENGTH_SHORT).show();

        else if (contact.length() < 11)
            Toast.makeText(this, "Phone Number must be at least 11 digits", Toast.LENGTH_SHORT).show();

        else if (!cbRecaptcha.isChecked()) onRecaptcha();

        else return true;

        return false;

    }

    private void onRecaptcha() {

        cbRecaptcha.setChecked(false);

        SafetyNet
                .getClient(this)
                .verifyWithRecaptcha(site)
                .addOnSuccessListener(recaptchaTokenResponse ->
                {

                    if (recaptchaTokenResponse.getTokenResult() != null
                            && !recaptchaTokenResponse.getTokenResult().isEmpty())

                        handleSiteVerify(recaptchaTokenResponse.getTokenResult());

                }).addOnFailureListener(e ->
                {

                    if (e instanceof ApiException) {

                        final ApiException apiException = (ApiException) e;
                        Log.d(tag, "Error message: " + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));

                    } else Log.d(tag, "Unknown type of error: " + e.getMessage());

                });

    }

    protected void handleSiteVerify(final String tokenResult) {

        final String url = "https://www.google.com/recaptcha/api/siteverify";

        final StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response ->
                {

                    try {

                        final JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("success")) {

                            cbRecaptcha.setTextColor(getColor(R.color.color_fulvous));
                            cbRecaptcha.setChecked(true);
                            cbRecaptcha.setClickable(false);

                        } else
                            Toast.makeText(this, jsonObject.getString("error-codes"), Toast.LENGTH_LONG).show();

                    } catch (Exception ex) {

                        Log.d(tag, "JSON exception: " + ex.getMessage());

                    }

                },
                error -> Log.d(tag, "Error message: " + error.getMessage())) {

            @Override
            protected Map<String, String> getParams() {

                final Map<String, String> params = new HashMap<>();
                params.put("secret", secret);
                params.put("response", tokenResult);
                return params;

            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(stringRequest);

    }

    private void checkContact() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_contact", contact)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {

                    final HashMap<String, Object> user = new HashMap<>();
                    user.put("user_contact", contact);

                    if (queryDocumentSnapshots != null) {

                        if (queryDocumentSnapshots.isEmpty()) onNext(user);

                        else

                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                final UserModel getUser = snapshot.toObject(UserModel.class);
                                final String _userId = getUser.getUser_id();

                                if (_userId.equals(userId)) onNext(user);

                                else {

                                    isLoading.setVisibility(View.GONE);
                                    Toast.makeText(this, "Phone Number is Unavailable!", Toast.LENGTH_SHORT).show();

                                }

                            }

                    }

                });

    }

    private void onNext(final HashMap<String, Object> user) {

        firebaseFirestore
                .collection("Users")
                .document(userId)
                .update(user)
                .addOnSuccessListener(runnable ->
                {

                    editor.putString("user_contact", ccpCountry.getFullNumberWithPlus());
                    editor.apply();
                    startActivity(new Intent(this, VerificationActivity.class));
                    finish();

                });

    }

}