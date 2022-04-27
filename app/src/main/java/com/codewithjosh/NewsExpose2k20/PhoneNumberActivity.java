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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;
import com.hbb20.CountryCodePicker;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PhoneNumberActivity extends AppCompatActivity {

    final String s_tag = RegisterActivity.class.getSimpleName();
    final String s_site = "6LdmXHcfAAAAAAGqu4EGoI8Ihrk8IB78NdM2cKFJ";
    final String s_secret = "6LdmXHcfAAAAAHMLaAuerAsSOZDkNJYA-gJ8Fma3";
    Button btn_next;
    CheckBox cb_recaptcha;
    CountryCodePicker ccp_country;
    EditText et_contact;
    LinearLayout is_loading;
    TextView btn_skip;
    String s_user_id, s_user_contact, s_contact;
    FirebaseFirestore firebaseFirestore;

    RequestQueue req;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    DocumentReference documentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        initViews();
        initInstance();
        initSharedPref();
        load();
        build();

    }

    private void initViews() {

        btn_next = findViewById(R.id.btn_next);
        cb_recaptcha = findViewById(R.id.cb_recaptcha);
        ccp_country = findViewById(R.id.ccp_country);
        et_contact = findViewById(R.id.et_contact);
        is_loading = findViewById(R.id.is_loading);
        btn_skip = findViewById(R.id.btn_skip);

    }

    private void initInstance() {

        firebaseFirestore = FirebaseFirestore.getInstance();
        req = Volley.newRequestQueue(getApplicationContext());

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void load() {

        s_user_id = sharedPref.getString("s_user_id", String.valueOf(MODE_PRIVATE));
        s_user_contact = sharedPref.getString("s_user_contact", String.valueOf(MODE_PRIVATE));
        et_contact.setText(s_user_contact);

    }

    private void build() {

        btn_skip.setOnClickListener(v -> {

            is_loading.setVisibility(View.VISIBLE);

            if (isConnected()) {

                final String s_user_contact = "";
                final boolean user_is_verified = true;

                final HashMap<String, Object> user = new HashMap<>();
                user.put("user_contact", s_user_contact);
                user.put("user_is_verified", user_is_verified);

                updateUser(user);
            }
            else {
                is_loading.setVisibility(View.GONE);
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            }

        });

        btn_next.setOnClickListener(v -> {

            s_contact = et_contact.getText().toString();

            if (validate(v)) checkContact();

            else is_loading.setVisibility(View.GONE);

        });

        ccp_country.registerCarrierNumberEditText(et_contact);

        cb_recaptcha.setOnClickListener(v -> onRecaptcha());

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void updateUser(final HashMap<String, Object> user) {

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

                                        Toast.makeText(this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(this, HomeActivity.class));
                                        finish();
                                    });
                });

    }

    private boolean validate(final View v) {

        is_loading.setVisibility(View.VISIBLE);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CommentActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected()) Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (s_contact.isEmpty()) Toast.makeText(this, "Phone Number is required!", Toast.LENGTH_SHORT).show();

        else if (!s_contact.startsWith("09")) Toast.makeText(this, "Provide a valid Phone Number", Toast.LENGTH_SHORT).show();

        else if (s_contact.length() < 11) Toast.makeText(this, "Phone Number must be at least 11 digits", Toast.LENGTH_SHORT).show();

        else if (!cb_recaptcha.isChecked()) onRecaptcha();

        else return true;

        return false;

    }

    private void onRecaptcha() {

        cb_recaptcha.setChecked(false);

        SafetyNet
                .getClient(this)
                .verifyWithRecaptcha(s_site)
                .addOnSuccessListener(recaptchaTokenResponse -> {

                    if (recaptchaTokenResponse.getTokenResult() != null
                            && !recaptchaTokenResponse.getTokenResult().isEmpty()) {

                        handleSiteVerify(recaptchaTokenResponse.getTokenResult());
                    }

                }).addOnFailureListener(e -> {

            if (e instanceof ApiException) {

                ApiException apiException = (ApiException) e;
                Log.d(s_tag, "Error message: " + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
            } else Log.d(s_tag, "Unknown type of error: " + e.getMessage());

        });

    }

    protected void handleSiteVerify(final String s_tokenResult) {

        final String s_url = "https://www.google.com/recaptcha/api/siteverify";

        StringRequest sr_req = new StringRequest(Request.Method.POST, s_url,
                response -> {

                    try {

                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("success")) {

                            cb_recaptcha.setTextColor(getResources().getColor(R.color.colorFulvous));
                            cb_recaptcha.setChecked(true);
                            cb_recaptcha.setClickable(false);
                        } else
                            Toast.makeText(getApplicationContext(), jsonObject.getString("error-codes"), Toast.LENGTH_LONG).show();

                    } catch (Exception ex) {

                        Log.d(s_tag, "JSON exception: " + ex.getMessage());
                    }
                },
                error -> Log.d(s_tag, "Error message: " + error.getMessage())) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("secret", s_secret);
                params.put("response", s_tokenResult);
                return params;

            }
        };

        sr_req.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        req.add(sr_req);

    }

    private void checkContact() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_contact", s_contact)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    final HashMap<String, Object> user = new HashMap<>();
                    user.put("user_contact", s_contact);

                    if (queryDocumentSnapshots != null)

                        if (!queryDocumentSnapshots.isEmpty())

                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                final UserModel getUser = snapshot.toObject(UserModel.class);
                                final String _s_user_id = getUser.getUser_id();

                                if (!_s_user_id.equals(s_user_id)) {

                                    is_loading.setVisibility(View.GONE);
                                    Toast.makeText(this, "Phone Number is Unavailable!", Toast.LENGTH_SHORT).show();
                                } else onNext(user);
                            }
                        else onNext(user);

                });

    }

    private void onNext(final HashMap<String, Object> user) {

        firebaseFirestore
                .collection("Users")
                .document(s_user_id)
                .update(user)
                .addOnSuccessListener(runnable -> {

                    editor.putString("s_user_contact", ccp_country.getFullNumberWithPlus());
                    editor.apply();
                    startActivity(new Intent(this, VerificationActivity.class));
                    finish();
                });

    }

}