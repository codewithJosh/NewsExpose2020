package com.codewithjosh.NewsExpose2k20.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.R;
import com.codewithjosh.NewsExpose2k20.models.Update;
import com.codewithjosh.NewsExpose2k20.models.User;
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
    public List<Update> mUpdate;

    private FirebaseUser firebaseUser;

    public UpdateAdapter(Context mContext, List<Update> mUpdate) {
        this.mContext = mContext;
        this.mUpdate = mUpdate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.update_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Update update = mUpdate.get(position);

        Glide.with(mContext).load(update.getUpdateimage()).into(holder.update_image);

        if(update.getSubject().equals("")){
            holder.subject.setVisibility(View.GONE);
        } else {
            holder.subject.setVisibility(View.VISIBLE);
            holder.subject.setText(update.getSubject());
        }

        if(update.getSource().equals("")){
            holder.source.setVisibility(View.VISIBLE);
            holder.source.setText("Anonymous");
        } else {
            holder.source.setVisibility(View.VISIBLE);
            holder.source.setText(update.getSource());
        }

        updateInfo(holder.image_profile, holder.username, update.getSource());
    }

    @Override
    public int getItemCount() {
        return mUpdate.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView update_image, seen, comment, image_profile;
        public TextView source, seens, subject, comments, username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            update_image = itemView.findViewById(R.id.update_image);
            seens = itemView.findViewById(R.id.seens);
            comment = itemView.findViewById(R.id.comment);
            source = itemView.findViewById(R.id.username);
            seen = itemView.findViewById(R.id.seen);
            subject = itemView.findViewById(R.id.subject);
            comments = itemView.findViewById(R.id.comments);

        }

    }

    private void updateInfo(final ImageView image_profile, final TextView username, final String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
