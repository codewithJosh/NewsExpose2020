package com.codewithjosh.NewsExpose2k20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.adapters.CommentAdapter;
import com.codewithjosh.NewsExpose2k20.models.CommentModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    String s_update_id;
    String s_user_id;
    EditText et_comment_content;
    ImageButton btn_back, btn_comment;
    ImageView iv_user_image;
    RecyclerView recycler_comments;
    FirebaseDatabase firebaseDatabase;
    private CommentAdapter commentAdapter;
    private List<CommentModel> mComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        recycler_comments = findViewById(R.id.recycler_comments);
        recycler_comments.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_comments.setLayoutManager(linearLayoutManager);
        mComment = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, mComment);
        recycler_comments.setAdapter(commentAdapter);

        et_comment_content = findViewById(R.id.et_comment_content);
        btn_back = findViewById(R.id.btn_back);
        btn_comment = findViewById(R.id.btn_comment);
        iv_user_image = findViewById(R.id.civ_user_image);

        firebaseDatabase = FirebaseDatabase.getInstance();

        btn_back.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));

//        TODO: FOUND ISSUE: UPDATE PASS PARAMETERS incl. UpdateAdapter
        s_update_id = getIntent().getStringExtra("updateid");
        s_user_id = getIntent().getStringExtra("userid");

        btn_comment.setOnClickListener(v -> {

            if (et_comment_content.getText().toString().isEmpty())
                Toast.makeText(this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
            else onSend();

        });

        getUserImage();
        getComments();
    }

    private void onSend() {

        final DatabaseReference updateRef = firebaseDatabase
                .getReference("Comments")
                .child(s_update_id);

        HashMap<String, Object> comment = new HashMap<>();
        comment.put("comment", et_comment_content.getText().toString().trim());
        comment.put("userid", s_user_id);

        updateRef
                .push()
                .setValue(comment)
                .addOnSuccessListener(runnable -> et_comment_content.setText(""));

    }

    private void getUserImage() {

        final DatabaseReference userRef = firebaseDatabase
                .getReference("Users")
                .child(s_user_id);

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        userRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                Glide.with(getApplicationContext()).load(userModel.getImageurl()).into(iv_user_image);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getComments() {

        final DatabaseReference updateRef = firebaseDatabase
                .getReference("Comments")
                .child(s_update_id);

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        updateRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mComment.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CommentModel commentModel = snapshot.getValue(CommentModel.class);
                    mComment.add(commentModel);
                }
                commentAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}