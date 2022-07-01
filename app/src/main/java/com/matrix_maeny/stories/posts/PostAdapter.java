package com.matrix_maeny.stories.posts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.UserModel;
import com.matrix_maeny.stories.comments.CommentModel;
import com.matrix_maeny.stories.comments.CommentViewerActivity;
import com.matrix_maeny.stories.databinding.PostModelBinding;
import com.matrix_maeny.stories.fragments.profile.ProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {

    private final Context context;
    private final ArrayList<PostModel> list;
    //    private int[] likesArray = {0,1};
    private final String currentUid;
    private final FirebaseDatabase firebaseDatabase;
    private PostAdapterListener listener;
    private String comment = null;

    public PostAdapter(Context context, ArrayList<PostModel> list) {
        this.context = context;
        this.list = list;

        try {
            listener = (PostAdapterListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }

        currentUid = FirebaseAuth.getInstance().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_model, parent, false);
        return new viewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        AtomicBoolean isLiked = new AtomicBoolean(false);
        AtomicBoolean isSaved = new AtomicBoolean(false);
        PostModel model = list.get(position);


        setProfilePic(holder.binding, model.getUserUid(), false);

        holder.binding.pamUsernameTv.setText(model.getUsername());

        Picasso.get().load(model.getImageUrl()).placeholder(R.drawable.placeholder).into(holder.binding.pamPostIv);

        holder.binding.pamPostTitleTv.setText(model.getTitle());
        holder.binding.pamPostTagTv.setText(model.getTagLine());

        setLikeImage(holder.binding, model, isLiked);
        setSavedStatus(holder.binding, model, isSaved);

        if (model.getUserUid().equals(currentUid)) {
            holder.binding.pamSaveIv.setVisibility(View.GONE);
            holder.binding.pamMenuIv.setVisibility(View.VISIBLE);

        } else {
            holder.binding.pamSaveIv.setVisibility(View.VISIBLE);
            holder.binding.pamMenuIv.setVisibility(View.GONE);

        }

        if (model.getLikes() != 0) {
            holder.binding.pamNoOfLikesTv.setText(model.getLikes() + " Likes");
//            holder.binding.pamNoOfLikesTv.setVisibility(View.VISIBLE);
        } else holder.binding.pamNoOfLikesTv.setVisibility(View.GONE);


        if (model.getComments() != 0)
            holder.binding.pamViewAllCommtsTv.setText("View all " + model.getComments() + " comments");
        else holder.binding.pamViewAllCommtsTv.setVisibility(View.GONE);



        holder.binding.pamImgForeLayout.setOnClickListener(v -> viewSelectedPost(model));

        holder.binding.pamPostTitleTv.setOnClickListener(v -> viewSelectedPost(model));

        holder.binding.pamPostTagTv.setOnClickListener(v -> viewSelectedPost(model));

        holder.binding.pamLikeIv.setOnClickListener(v -> {

            if (!isLiked.get()) {
                holder.binding.pamLikeIv.setImageResource(R.drawable.liked);
                isLiked.set(true);
            } else {
                holder.binding.pamLikeIv.setImageResource(R.drawable.like);
                isLiked.set(false);
            }

            if (isLiked.get()) {
                setAsLiked(model);
                model.setLikes(model.getLikes() + 1);
                holder.binding.pamNoOfLikesTv.setText(model.getLikes() + " Likes");
                holder.binding.pamNoOfLikesTv.setVisibility(View.VISIBLE);

            } else {
                setAsDisLiked(model);

                model.setLikes(model.getLikes() - 1);

                if (model.getLikes() != 0) {
                    holder.binding.pamNoOfLikesTv.setText(model.getLikes() + " Likes");
                    holder.binding.pamNoOfLikesTv.setVisibility(View.VISIBLE);
                } else {
                    holder.binding.pamNoOfLikesTv.setVisibility(View.GONE);

                }
            }

        });

        holder.binding.pamCommentIv.setOnClickListener(v -> {

            if (holder.binding.commentLayout2.getVisibility() == View.GONE) {
                holder.binding.commentLayout2.setVisibility(View.VISIBLE);
                setProfilePic(holder.binding, currentUid, true);
                holder.binding.cmCommentEt.requestFocus();
                holder.binding.pamCmHelpTv.setVisibility(View.VISIBLE);
                listener.showKeyboard(true, holder.binding.cmCommentEt);

            } else {
                holder.binding.commentLayout2.setVisibility(View.GONE);
                holder.binding.cmCommentEt.setText("");
                holder.binding.cmCommentEt.clearFocus();
                holder.binding.pamCmHelpTv.setVisibility(View.GONE);
                listener.showKeyboard(false, holder.binding.cmCommentEt);

            }
        });

        holder.binding.cmPostTv.setOnClickListener(v -> {

            if (checkComment(holder.binding)) {

//                model.setComments(model.getComments() + 1);
//                if (model.getUserUid().equals(currentUid)) {
//                    holder.binding.pamViewAllCommtsTv.setText("View all " + model.getComments() + " comments");
//                } else {
//                    holder.binding.pamViewAllCommtsTv.setText("View all " + model.getComments() + 1 + " comments");
//
//                }
                holder.binding.commentLayout2.setVisibility(View.GONE);
                holder.binding.pamCmHelpTv.setVisibility(View.GONE);
                holder.binding.cmCommentEt.setText("");
                holder.binding.cmCommentEt.clearFocus();

                listener.showKeyboard(false, holder.binding.cmCommentEt);
                postComment(model, holder.binding);
            }
        });


        if (model.getUserUid().equals(currentUid)) {

            holder.binding.pamMenuIv.setOnClickListener(v -> listener.showDeleteFragment(model));
        }


        if (!model.getUserUid().equals(currentUid)) {
            holder.binding.pamSaveIv.setOnClickListener(v -> {
                if (!isSaved.get()) {
                    holder.binding.pamSaveIv.setImageResource(R.drawable.saved);
                    saveStory(model);
                    isSaved.set(true);
                } else {
                    holder.binding.pamSaveIv.setImageResource(R.drawable.save);
                    unSaveStory(model);
                    isSaved.set(false);

                }
            });
        }
        holder.binding.pamUserIv.setOnClickListener(v -> goToUserProfile(model.getUserUid()));

        holder.binding.pamUsernameTv.setOnClickListener(v -> goToUserProfile(model.getUserUid()));

        holder.binding.pamViewAllCommtsTv.setOnClickListener(v -> {
            CommentViewerActivity.model = model;
            context.startActivity(new Intent(context.getApplicationContext(), CommentViewerActivity.class));
        });




    }




    private void goToUserProfile(@NonNull String userId) {
        if (!userId.equals(currentUid)) {
            ProfileActivity.profileUserId = userId;
            context.startActivity(new Intent(context.getApplicationContext(), ProfileActivity.class));
        } else {
            Toast.makeText(context, "It is your profile, go to profile section instead", Toast.LENGTH_LONG).show();
        }
    }

    private void unSaveStory(@NonNull PostModel model) {
        firebaseDatabase.getReference().child("Saves").child(currentUid)
                .child(model.getUserUid()).child(model.getTitle()).removeValue();
    }


    private void setSavedStatus(PostModelBinding binding, @NonNull PostModel model, AtomicBoolean isSaved) {

        firebaseDatabase.getReference().child("Saves").child(currentUid)
                .child(model.getUserUid()).child(model.getTitle())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            binding.pamSaveIv.setImageResource(R.drawable.saved);
                        } else {
                            binding.pamSaveIv.setImageResource(R.drawable.save);
                        }
                        isSaved.set(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void saveStory(@NonNull PostModel model) {
        firebaseDatabase.getReference().child("Saves").child(currentUid)
                .child(model.getUserUid()).child(model.getTitle()).setValue(model);
    }

    @SuppressLint("SetTextI18n")
    private void setAsDisLiked(@NonNull PostModel model) {
        firebaseDatabase.getReference().child("Likes").child(currentUid).child(model.getUserUid())
                .child(model.getTitle()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {


                        firebaseDatabase.getReference().child("Posts").child(model.getUserUid()).child(model.getTitle())
                                .child("likes").setValue(model.getLikes());

//                        updateModel(model);


                    } else
                        Toast.makeText(context, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    @SuppressLint("SetTextI18n")
    private void setAsLiked(@NonNull PostModel model) {
        firebaseDatabase.getReference().child("Likes").child(currentUid).child(model.getUserUid())
                .child(model.getTitle()).setValue(true).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {


                        firebaseDatabase.getReference().child("Posts").child(model.getUserUid()).child(model.getTitle())
                                .child("likes").setValue(model.getLikes());
//                        updateModel(model);


                    } else
                        Toast.makeText(context, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setLikeImage(PostModelBinding binding, @NonNull PostModel model, AtomicBoolean isLiked) {
        firebaseDatabase.getReference().child("Likes").child(currentUid).child(model.getUserUid())
                .child(model.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isLiked.set(snapshot.exists());
                        if (isLiked.get()) {
                            binding.pamLikeIv.setImageResource(R.drawable.liked);
                        } else {
                            binding.pamLikeIv.setImageResource(R.drawable.like);

                        }
//
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setProfilePic(PostModelBinding binding, String userId, boolean fromComment) {
        firebaseDatabase.getReference().child("Users").child(Objects.requireNonNull(userId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {
                            try {

                                if (fromComment) {
                                    Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.cmUserIv);
                                } else {
                                    Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.pamUserIv);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (fromComment) {
                                    binding.cmUserIv.setImageResource(R.drawable.profile_pic);
                                } else binding.pamUserIv.setImageResource(R.drawable.profile_pic);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void postComment(PostModel model, PostModelBinding binding) {
//        dismiss();

        firebaseDatabase.getReference().child("Users").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if (userModel != null) {
                            CommentModel commentModel = new CommentModel(snapshot.getKey(), userModel.getUsername(), userModel.getProfilePicUrl(), comment);

                            firebaseDatabase.getReference().child("Comments").child(model.getUserUid())
                                    .child(model.getTitle()).child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (!snapshot.exists()) {
                                                model.setComments(model.getComments() + 1);
                                            }

                                            firebaseDatabase.getReference().child("Comments").child(model.getUserUid())
                                                    .child(model.getTitle())
                                                    .child(Objects.requireNonNull(currentUid))
                                                    .setValue(commentModel).addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {

                                                            binding.pamViewAllCommtsTv.setText("View all " + model.getComments() + " comments");
                                                            binding.pamViewAllCommtsTv.setVisibility(View.VISIBLE);

                                                            Toast.makeText(context, "Comment posted", Toast.LENGTH_SHORT).show();


                                                            firebaseDatabase.getReference().child("Posts").child(model.getUserUid()).child(model.getTitle())
                                                                    .child("comments").setValue(model.getComments());

                                                        } else {
                                                            Toast.makeText(context, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private boolean checkComment(PostModelBinding binding) {
        try {
            comment = Objects.requireNonNull(binding.cmCommentEt.getText()).toString();
            if (!comment.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "Please enter Password", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void viewSelectedPost(PostModel model) {
        PostViewerActivity.model = model;
        context.startActivity(new Intent(context.getApplicationContext(), PostViewerActivity.class));

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface PostAdapterListener {

        void showKeyboard(boolean shouldShow, EditText editText);

        void showDeleteFragment(PostModel model);
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        PostModelBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = PostModelBinding.bind(itemView);
        }
    }

}
