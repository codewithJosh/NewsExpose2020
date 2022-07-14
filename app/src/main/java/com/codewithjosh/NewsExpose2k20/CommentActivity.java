package com.codewithjosh.NewsExpose2k20;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.adapters.CommentAdapter;
import com.codewithjosh.NewsExpose2k20.models.CommentModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    Button btnComment;
    ConstraintLayout isLoading;
    EditText etCommentContent;
    ImageButton btnBack;
    ImageView ivUserImage;
    RecyclerView recyclerComments;
    TextView tvStatus;
    String updateId;
    String userId;
    String commentContent;
    CollectionReference collectionRef;
    DocumentReference documentRef;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences sharedPref;
    private CommentAdapter commentAdapter;
    private List<CommentModel> comments;

    @Override
    protected void onStart() {

        super.onStart();

        initInstances();
        load();
        loadUserImage();

    }

    private void initInstances()
    {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void load() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        updateId = sharedPref.getString("update_id", String.valueOf(MODE_PRIVATE));
        userId = sharedPref.getString("user_id", String.valueOf(MODE_PRIVATE));

        collectionRef = firebaseFirestore
                .collection("Updates")
                .document(updateId)
                .collection("Comments");

    }

    private void loadUserImage() {

        firebaseFirestore
                .collection("Users")
                .document(userId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null)
                    {

                        final UserModel user = value.toObject(UserModel.class);

                        if (user != null)
                        {

                            final String userImage = user.getUser_image();
                            Glide.with(this).load(userImage).into(ivUserImage);

                        }

                    }

                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        initViews();
        initInstances();
        load();
        loadComments();
        build();

    }

    private void initViews() {

        etCommentContent = findViewById(R.id.et_comment_content);
        btnBack = findViewById(R.id.btn_back);
        btnComment = findViewById(R.id.btn_comment);
        ivUserImage = findViewById(R.id.civ_user_image);
        isLoading = findViewById(R.id.is_loading);
        recyclerComments = findViewById(R.id.recycler_comments);
        tvStatus = findViewById(R.id.tv_status);

        initRecyclerView();

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments);
        recyclerComments.setAdapter(commentAdapter);

    }

    private void initRecyclerView()
    {

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerComments.setLayoutManager(linearLayoutManager);
        recyclerComments.setHasFixedSize(true);

    }

    private void loadComments() {

        isLoading.setVisibility(View.VISIBLE);

        collectionRef
                .orderBy("comment_timestamp")
                .addSnapshotListener((value, error) ->
                {

                    if (value != null)
                    {

                        if (validate(value)) onLoadComments(value);

                        else isLoading.setVisibility(View.GONE);

                    }

                });

    }

    private boolean validate(final QuerySnapshot value) {

        if (!isConnected()) tvStatus.setText(R.string.text_status_disconnected);

        else if (value.isEmpty()) tvStatus.setText(R.string.text_status_empty_1);

        else return true;

        return false;

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onLoadComments(final QuerySnapshot value) {

        isLoading.setVisibility(View.GONE);
        tvStatus.setText("");

        comments.clear();
        for (QueryDocumentSnapshot snapshot : value)
        {

            final CommentModel comment = snapshot.toObject(CommentModel.class);
            comments.add(comment);

        }
        commentAdapter.notifyDataSetChanged();

    }

    private void build() {

        btnBack.setOnClickListener(v -> onBackPressed());

        btnComment.setOnClickListener(v ->
        {

            commentContent = etCommentContent.getText().toString().trim();

            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

            if (isConnected()) onComment();

            else Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        });

        etCommentContent.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                commentContent = etCommentContent.getText().toString().trim();
                btnComment.setEnabled(!commentContent.isEmpty());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

    }

    private void onComment() {

        final String commentId = collectionRef
                .document()
                .getId();

        final Calendar calendar = Calendar.getInstance();
        final Date commentTimestamp = calendar.getTime();

        final CommentModel comment = new CommentModel(
                commentContent,
                commentTimestamp,
                userId
        );

        onSend(commentId, comment);

    }

    private void onSend(final String commentId, final CommentModel comment) {

        documentRef = collectionRef
                .document(commentId);

        documentRef.addSnapshotListener((value, error) ->
        {

            if (value != null && !value.exists())

                documentRef
                    .set(comment)
                    .addOnSuccessListener(unused -> etCommentContent.setText(""));

        });

    }

}