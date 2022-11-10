package com.codewithjosh.NewsExpose2k20.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithjosh.NewsExpose2k20.CommentActivity;
import com.codewithjosh.NewsExpose2k20.R;
import com.codewithjosh.NewsExpose2k20.models.UpdateModel;
import com.codewithjosh.NewsExpose2k20.models.UserModel;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.ViewHolder> {

    private static final int secondMillis = 1000;
    private static final int minuteMillis = 60 * secondMillis;
    private static final int hourMillis = 60 * minuteMillis;
    private static final int dayMillis = 24 * hourMillis;
    private static final int weekMillis = 7 * dayMillis;
    public Context context;
    public List<UpdateModel> updates;
    String userId;
    CollectionReference seenRef;
    CollectionReference commentRef;
    DateFormat dateFormat;
    DocumentReference documentRef;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public UpdateAdapter(final Context context, final List<UpdateModel> updates) {

        this.context = context;
        this.updates = updates;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_update, viewGroup, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final UpdateModel update = updates.get(position);

//        initViews
        final CircleImageView civUserImage = holder.circleImageView;
        final ImageButton btnSeen = holder.btnSeen;
        final ImageButton navComment = holder.navComment;
        final ImageView ivUpdateImage = holder.ivUpdateImage;
        final TextView tvSeenCount = holder.tvSeenCount;
        final TextView tvUpdateContent = holder.tvUpdateContent;
        final TextView tvCommentCount = holder.tvCommentCount;
        final TextView tvUserBio = holder.tvUserBio;
        final TextView tvSubtitle = holder.tvSubtitle;

//        load
        final String updateId = update.getUpdate_id();
        final String updateImage = update.getUpdate_image();
        final String updateContent = update.getUpdate_content();
        final Date updateTimestamp = update.getUpdate_timestamp();
        final String updateUserId = update.getUser_id();

        initInstances();
        initSharedPref();
        load();

        loadUser(civUserImage, tvUserBio, tvSubtitle, updateUserId, updateTimestamp);

        if (!updateContent.isEmpty()) {

            tvUpdateContent.setVisibility(View.VISIBLE);
            tvUpdateContent.setText(update.getUpdate_content());

        } else tvUpdateContent.setVisibility(View.GONE);

        Glide.with(context).load(updateImage).into(ivUpdateImage);

        seenRef = firebaseFirestore
                .collection("Updates")
                .document(updateId)
                .collection("Seen");

        isSeen(btnSeen, tvSeenCount);

        seenCount(tvSeenCount);

        btnSeen.setOnClickListener(v -> onSeen(btnSeen, updateId));

        commentRef = firebaseFirestore
                .collection("Updates")
                .document(updateId)
                .collection("Comments");

        commentCount(tvCommentCount);

        navComment.setOnClickListener(v ->
        {

            editor.putString("update_id", updateId);
            editor.apply();
            context.startActivity(new Intent(context, CommentActivity.class));

        });

        ivUpdateImage.setOnTouchListener(new View.OnTouchListener() {

            private final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    onSeen(btnSeen, updateId);
                    return super.onDoubleTap(e);

                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent event) {

                    return false;

                }

            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                gestureDetector.onTouchEvent(event);
                return true;

            }

        });

    }

    private void onSeen(final ImageButton btnSeen, final String updateId) {

        final UserModel user = new UserModel();

        documentRef = firebaseFirestore
                .collection("Updates")
                .document(updateId)
                .collection("Seen")
                .document(userId);

        if (btnSeen.getTag().equals("seen")) documentRef.set(user);

        else documentRef.delete();

    }

    private void initInstances() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void load() {

        userId = sharedPref.getString("user_id", String.valueOf(Context.MODE_PRIVATE));

    }

    private void loadUser(final CircleImageView civUserImage, final TextView tvUserName, final TextView tvSubtitle, final String updateUserId, final Date updateTimestamp) {

        firebaseFirestore
                .collection("Users")
                .document(updateUserId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null && value.exists()) {

                        final UserModel user = value.toObject(UserModel.class);

                        if (user != null) {

                            final String userImage = user.getUser_image();
                            final String userBio = user.getUser_bio();
                            final String userName = user.getUser_name();
                            final String subtitle = getTimeAgo(updateTimestamp) + " · " + userName;

                            Glide.with(context).load(userImage).into(civUserImage);
                            tvUserName.setText(userBio);
                            tvSubtitle.setText(subtitle);

                        }

                    }

                });

    }

    private String getTimeAgo(final Date updateTimestamp) {

        final Calendar calendar = Calendar.getInstance();
        final String yearFormat = "yyyy";
        final String updateTimestampWithinTheYearFormat = "MMMM d";
        final String updateTimestampFormat = "MMMM d, yyyy";

        dateFormat = new SimpleDateFormat(yearFormat);
        final int yearNow = calendar.get(Calendar.YEAR);
        final int year = Integer.parseInt(dateFormat.format(updateTimestamp));

        dateFormat = new SimpleDateFormat(updateTimestampWithinTheYearFormat);
        final String updateTimestampWithinTheYear = dateFormat.format(updateTimestamp);

        dateFormat = new SimpleDateFormat(updateTimestampFormat);
        final String _updateTimestamp = dateFormat.format(updateTimestamp);

        final long now = calendar.getTime().getTime();
        long time = updateTimestamp.getTime();

        if (time < 1000000000000L) time *= 1000;
        final long diff = now - time;

        if (diff < minuteMillis) return "Just now";

        else if (diff < 60 * minuteMillis) return diff / minuteMillis + "m";

        else if (diff < 24 * hourMillis) return diff / hourMillis + "h";

        else if (diff < 48 * hourMillis) return "yesterday";

        else if (diff < 7 * dayMillis) return diff / dayMillis + "d";

        else if (diff < 4L * weekMillis) return diff / weekMillis + "w";

        else if (year == yearNow) return updateTimestampWithinTheYear;

        else return _updateTimestamp;

    }

    private void isSeen(final ImageButton btnSeen, final TextView tvSeenCount) {

        seenRef
                .document(userId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        if (value.exists()) {

                            btnSeen.setImageResource(R.drawable.ic_seened);
                            btnSeen.setTag("");
                            tvSeenCount.setTextColor(context.getColor(R.color.color_fulvous));

                        } else {

                            btnSeen.setTag("seen");
                            btnSeen.setImageResource(R.drawable.ic_seen);
                            tvSeenCount.setTextColor(context.getColor(R.color.color_white_ff));

                        }

                    }

                });

    }

    private void seenCount(final TextView tvSeenCount) {

        seenRef
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        final String seenCount = String.valueOf(value.size());
                        tvSeenCount.setText(seenCount);

                    }

                });

    }

    private void commentCount(final TextView tvCommentCount) {

        commentRef
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        final String commentCount = String.valueOf(value.size());
                        tvCommentCount.setText(commentCount);

                    }

                });

    }

    @Override
    public int getItemCount() {

        return updates.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView circleImageView;
        public ImageButton btnSeen;
        public ImageButton navComment;
        public ImageView ivUpdateImage;
        public TextView tvSeenCount;
        public TextView tvUpdateContent;
        public TextView tvCommentCount;
        public TextView tvUserBio;
        public TextView tvSubtitle;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            circleImageView = itemView.findViewById(R.id.civ_user_image);
            btnSeen = itemView.findViewById(R.id.btn_seen);
            navComment = itemView.findViewById(R.id.nav_comment);
            ivUpdateImage = itemView.findViewById(R.id.iv_update_image);
            tvUserBio = itemView.findViewById(R.id.tv_user_bio);
            tvUpdateContent = itemView.findViewById(R.id.tv_update_content);
            tvSeenCount = itemView.findViewById(R.id.tv_seen_count);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);

        }

    }

}
