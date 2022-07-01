package com.matrix_maeny.stories.comments;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.databinding.CommentModelBinding;
import com.matrix_maeny.stories.fragments.profile.ProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewHolder> {

    private Context context;
    private ArrayList<CommentModel> list;
    private final String currentUid;

    public CommentAdapter(Context context, ArrayList<CommentModel> list) {
        this.context = context;
        this.list = list;
        currentUid = FirebaseAuth.getInstance().getUid();

    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_model, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        CommentModel model = list.get(position);

        try {
            Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(holder.binding.cmMUserIv);
        } catch (Exception e) {
            e.printStackTrace();
            holder.binding.cmMUserIv.setImageResource(R.drawable.profile_pic);
        }

        String commTxt = "<b><i>" + model.getUsername() + "</i></b> " + model.getComment();

        holder.binding.cmMusernameTv.setText(Html.fromHtml(commTxt));

        holder.binding.cmMUserIv.setOnClickListener(v -> {
            goToUserProfile(model.getUserId());
        });
    }

    private void goToUserProfile(@NonNull String userId) {
        if (!userId.equals(currentUid)) {
            ProfileActivity.profileUserId = userId;

            context.startActivity(new Intent(context.getApplicationContext(), ProfileActivity.class));
        }else{
            Toast.makeText(context, "It is your profile, go to profile section instead", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        CommentModelBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = CommentModelBinding.bind(itemView);
        }
    }

}
