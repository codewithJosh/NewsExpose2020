package com.codewithjosh.NewsExpose2k20.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.R;
import com.codewithjosh.NewsExpose2k20.models.CommentModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    public Context context;
    public List<CommentModel> commentList;
    FirebaseFirestore firebaseFirestore;

    public CommentAdapter(Context context, List<CommentModel> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final CommentModel comment = commentList.get(position);

//        initViews
        final CircleImageView civ_user_image = holder.civ_user_image;
        final TextView tv_comment_content = holder.tv_comment_content;
        final TextView tv_user_bio = holder.tv_user_bio;

//        load
        final String s_comment_content = comment.getComment_content();
        final String s_user_id = comment.getUser_id();

        firebaseFirestore = FirebaseFirestore.getInstance();

        loadUser(civ_user_image, tv_user_bio, s_user_id);

        tv_comment_content.setText(s_comment_content);

    }

    private void loadUser(final CircleImageView civ_user_image, final TextView tv_user_bio, final String s_user_id) {

        firebaseFirestore
                .collection("Users")
                .document(s_user_id)
                .addSnapshotListener((value, error) -> {

                    if (value != null)

                        if (value.exists()) {

                            final UserModel user = value.toObject(UserModel.class);

                            if (user != null) {

                                final String s_user_image = user.getUser_image();
                                final String s_user_bio = user.getUser_bio();

                                Glide.with(context).load(s_user_image).into(civ_user_image);
                                tv_user_bio.setText(s_user_bio);
                            }
                        }
                });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civ_user_image;
        TextView tv_user_bio, tv_comment_content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            civ_user_image = itemView.findViewById(R.id.civ_user_image);
            tv_user_bio = itemView.findViewById(R.id.tv_user_bio);
            tv_comment_content = itemView.findViewById(R.id.tv_comment_content);

        }

    }

}
