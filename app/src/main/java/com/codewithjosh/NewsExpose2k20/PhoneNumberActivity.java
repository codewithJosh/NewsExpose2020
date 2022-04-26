package com.codewithjosh.NewsExpose2k20;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;

public class PhoneNumberActivity extends AppCompatActivity {

    Button btn_next;
    CountryCodePicker ccp_country;
    EditText et_contact;
    LinearLayout is_loading;
    TextView btn_skip;

    String s_user_id, s_user_contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        btn_next = findViewById(R.id.btn_next);
        ccp_country = findViewById(R.id.ccp_country);
        et_contact = findViewById(R.id.et_contact);
        is_loading = findViewById(R.id.is_loading);
        btn_skip = findViewById(R.id.btn_skip);

        s_user_id = getIntent().getStringExtra("s_user_id");
        s_user_contact = getIntent().getStringExtra("s_user_contact");

        et_contact.setText(s_user_contact);

    }

}