package com.codewithjosh.NewsExpose2k20.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.CommentActivity;
import com.codewithjosh.NewsExpose2k20.R;
import com.codewithjosh.NewsExpose2k20.models.UpdateModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.ViewHolder> {

    public Context mContext;
    public List<UpdateModel> mUpdate;

    private FirebaseUser firebaseUser;

    public UpdateAdapter(Context mContext, List<UpdateModel> mUpdate) {
        this.mContext = mContext;
        this.mUpdate = mUpdate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_update, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final UpdateModel updateModel = mUpdate.get(position);

        Glide.with(mContext).load(updateModel.getUpdateimage()).into(holder.update_image);

        if (updateModel.getSubject().equals("")) {
            holder.subject.setVisibility(View.GONE);
        } else {
            holder.subject.setVisibility(View.VISIBLE);
            holder.subject.setText(updateModel.getSubject());
        }

        if (updateModel.getSource().equals("")) {
            holder.source.setVisibility(View.VISIBLE);
            holder.source.setText("Anonymous");
        } else {
            holder.source.setVisibility(View.VISIBLE);
            holder.source.setText(updateModel.getSource());
        }

        updateInfo(holder.image_profile, holder.username, updateModel.getSource());

        isSeen(updateModel.getUpdateid(), holder.seen);

        holder.seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.seen.getTag().equals("seen")) {
                    FirebaseDatabase.getInstance().getReference().child("Seen").child(updateModel.getUpdateid())
                            .child(firebaseUser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Seen").child(updateModel.getUpdateid())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        numSeen(holder.seens, updateModel.getUpdateid());

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("updateid", updateModel.getUpdateid());
                intent.putExtra("userid", updateModel.getSource());
                mContext.startActivity(intent);
            }
        });

        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("updateid", updateModel.getUpdateid());
                intent.putExtra("userid", updateModel.getSource());
                mContext.startActivity(intent);
            }
        });

        getComments(updateModel.getUpdateid(), holder.comments);

    }

    @Override
    public int getItemCount() {
        return mUpdate.size();
    }

    private void updateInfo(final ImageView image_profile, final TextView username, final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                Glide.with(mContext).load(userModel.getImageurl()).into(image_profile);
                username.setText(userModel.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isSeen(String updateid, final ImageView imageView) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Seen")
                .child(updateid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_seened);
                    imageView.setTag("seened");
                } else {
                    imageView.setImageResource(R.drawable.ic_seen);
                    imageView.setTag("seen");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void numSeen(final TextView seens, String updateimage) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Seen")
                .child(updateimage);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                seens.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {

                    seens.setTextColor(Color.parseColor("#e50913"));
                } else {
                    seens.setTextColor(Color.parseColor("#d3d3d3"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getComments(String updateid, final TextView comments) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(updateid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView update_image, seen, comment, image_profile;
        public TextView source, seens, subject, comments, username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.civ_user_image);
            username = itemView.findViewById(R.id.tv_user_name);
            update_image = itemView.findViewById(R.id.iv_update_image);
            seens = itemView.findViewById(R.id.tv_seen_count);
            comment = itemView.findViewById(R.id.nav_comment);
            source = itemView.findViewById(R.id.tv_user_name);
            seen = itemView.findViewById(R.id.btn_seen);
            subject = itemView.findViewById(R.id.tv_update_content);
            comments = itemView.findViewById(R.id.tv_comment_count);

        }

    }

}
