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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private static final int secondMillis = 1000;
    private static final int minuteMillis = 60 * secondMillis;
    private static final int hourMillis = 60 * minuteMillis;
    private static final int dayMillis = 24 * hourMillis;
    private static final int weekMillis = 7 * dayMillis;
    public Context context;
    public List<CommentModel> comments;
    DateFormat dateFormat;
    FirebaseFirestore firebaseFirestore;

    public CommentAdapter(final Context context, final List<CommentModel> comments) {

        this.context = context;
        this.comments = comments;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, viewGroup, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final CommentModel comment = comments.get(position);

//        initViews
        final CircleImageView civUserImage = holder.civUserImage;
        final TextView tvCommentContent = holder.tvCommentContent;
        final TextView tvSubtitle = holder.tvSubtitle;
        final TextView tvUserBio = holder.tvUserBio;

//        load
        final String commentContent = comment.getComment_content();
        final Date commentTimestamp = comment.getComment_timestamp();
        final String userId = comment.getUser_id();

        initInstances();

        loadUser(civUserImage, tvUserBio, tvSubtitle, commentTimestamp, userId);

        tvCommentContent.setText(commentContent);

    }

    private void initInstances()
    {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void loadUser(final CircleImageView civUserImage, final TextView tvUserBio, TextView tvSubtitle, Date commentTimestamp, final String userId) {

        firebaseFirestore
                .collection("Users")
                .document(userId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null && value.exists())
                    {

                        final UserModel user = value.toObject(UserModel.class);

                        if (user != null)
                        {

                            final String userImage = user.getUser_image();
                            final String userBio = user.getUser_bio();
                            final String userName = user.getUser_name();
                            final String subtitle = getTimeAgo(commentTimestamp) + " · " + userName;

                            Glide.with(context).load(userImage).into(civUserImage);
                            tvUserBio.setText(userBio);
                            tvSubtitle.setText(subtitle);

                        }

                    }

                });

    }

    private String getTimeAgo(final Date commentTimestamp) {

        final Calendar calendar = Calendar.getInstance();
        final String yearFormat = "yyyy";
        final String commentTimestampWithinTheYearFormat = "MMMM d";
        final String commentTimestampFormat = "MMMM d, yyyy";

        dateFormat = new SimpleDateFormat(yearFormat);
        final int yearNow = calendar.get(Calendar.YEAR);
        final int year = Integer.parseInt(dateFormat.format(commentTimestamp));

        dateFormat = new SimpleDateFormat(commentTimestampWithinTheYearFormat);
        final String commentTimestampWithinTheYear = dateFormat.format(commentTimestamp);

        dateFormat = new SimpleDateFormat(commentTimestampFormat);
        final String _commentTimestamp = dateFormat.format(commentTimestamp);

        final long now = calendar.getTime().getTime();
        long time = commentTimestamp.getTime();

        if (time < 1000000000000L) time *= 1000;
        final long diff = now - time;

        if (diff < minuteMillis) return "Just now";

        else if (diff < 60 * minuteMillis) return diff / minuteMillis + "m";

        else if (diff < 24 * hourMillis) return diff / hourMillis + "h";

        else if (diff < 7 * dayMillis) return diff / dayMillis + "d";

        else if (diff < 4L * weekMillis) return diff / weekMillis + "w";

        else if (year == yearNow) return commentTimestampWithinTheYear;

        else return _commentTimestamp;

    }

    @Override
    public int getItemCount() {

        return comments.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView civUserImage;
        public TextView tvUserBio;
        public TextView tvSubtitle;
        public TextView tvCommentContent;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            civUserImage = itemView.findViewById(R.id.civ_user_image);
            tvUserBio = itemView.findViewById(R.id.tv_user_bio);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvCommentContent = itemView.findViewById(R.id.tv_comment_content);

        }

    }

}
