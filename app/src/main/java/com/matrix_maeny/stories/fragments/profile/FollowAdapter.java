package com.matrix_maeny.stories.fragments.profile;

import static com.matrix_maeny.stories.fragments.profile.ProfilePicViewerActivity.profilePicUrl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.matrix_maeny.stories.MainActivity;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.UserModel;
import com.matrix_maeny.stories.databinding.FollowModelBinding;
import com.matrix_maeny.stories.posts.PostViewerActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.viewHolder> {

    private Context context;
    private ArrayList<FollowModel> list;
    private final String currentUid;
    private FirebaseDatabase firebaseDatabase;

    public FollowAdapter(Context context, ArrayList<FollowModel> list) {
        this.context = context;
        this.list = list;
        currentUid = FirebaseAuth.getInstance().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();


    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.follow_model, parent, false);
        return new viewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        FollowModel model = list.get(position);

        AtomicBoolean isFollowing = new AtomicBoolean(FollowViewerActivity.followings);

        if (!FollowViewerActivity.followings) {
            setFollowings(holder.binding, isFollowing);
        }


        try {
            Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(holder.binding.cmMUserIv);
        } catch (Exception e) {
            e.printStackTrace();
            holder.binding.cmMUserIv.setImageResource(R.drawable.profile_pic);
        }


        holder.binding.cmMusernameTv.setText(model.getUsername());

        holder.binding.cmMUserIv.setOnClickListener(v -> goToUserProfile(model.getUserId()));

        holder.binding.cmMusernameTv.setOnClickListener(v -> goToUserProfile(model.getUserId()));

        holder.binding.fmFollowTv.setOnClickListener(v -> {
            if (isFollowing.get()) {
                isFollowing.set(false);
                holder.binding.fmFollowTv.setText("Follow");
                holder.binding.fmFollowTv.setBackgroundResource(R.drawable.follow_bg);
                unFollowUser(model);
            } else {
                isFollowing.set(true);
                holder.binding.fmFollowTv.setText("Following");
                holder.binding.fmFollowTv.setBackgroundResource(R.drawable.following_bg);
                followUser(model);
            }
        });
    }

    private void setFollowings(FollowModelBinding binding, AtomicBoolean isFollowing) {
        firebaseDatabase.getReference().child("Followings").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFollowing.set(snapshot.exists());
                        if (isFollowing.get()) {
                            binding.fmFollowTv.setBackgroundResource(R.drawable.following_bg);
                            binding.fmFollowTv.setText("Following");
                        } else {
                            binding.fmFollowTv.setBackgroundResource(R.drawable.follow_bg);
                            binding.fmFollowTv.setText("Follow");

                        }
                        ;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void unFollowUser(@NonNull FollowModel model) {
        firebaseDatabase.getReference().child("Followings").child(currentUid).child(model.getUserId()).removeValue()
                .addOnSuccessListener(unused -> {
                    followingCountForCurrentUser(false);

                    firebaseDatabase.getReference().child("Followers").child(model.getUserId())
                            .child(currentUid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    followerCountForModelUser(model.getUserId(), false);
                                }
                            }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

                }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

    }


    private void followUser(FollowModel model) {

        firebaseDatabase.getReference().child("Users").child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                if (userModel != null) {
                    FollowModel followModel = new FollowModel(userModel.getUsername(), userModel.getProfilePicUrl(), snapshot.getKey());

                    // for other user space
                    firebaseDatabase.getReference().child("Followers").child(model.getUserId())
                            .child(currentUid)
                            .setValue(followModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    followerCountForModelUser(model.getUserId(), true);


                                    goToCurrentSide(model);


                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void goToCurrentSide(@NonNull FollowModel model) {

        firebaseDatabase.getReference().child("Users").child(model.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if (userModel != null) {
                            FollowModel followModel2 = new FollowModel(model.getUsername(), userModel.getProfilePicUrl(), model.getUserId());


                            // for your space
                            firebaseDatabase.getReference().child("Followings").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                    .child(model.getUserId()).setValue(followModel2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            followingCountForCurrentUser(true);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }


    private void followerCountForModelUser(String userUid, boolean shouldIncrease) {
        firebaseDatabase.getReference().child("Users").child(userUid).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if (userModel != null) {
                            if (shouldIncrease) {
                                userModel.setFollowerCount(userModel.getFollowerCount() + 1);
                            } else
                                userModel.setFollowerCount(userModel.getFollowerCount() - 1);


                            firebaseDatabase.getReference().child("Users").child(userUid)
                                    .setValue(userModel);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void followingCountForCurrentUser(boolean shouldIncrease) {
        firebaseDatabase.getReference().child("Users").child(currentUid).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if (userModel != null) {
                            if (shouldIncrease) {
                                userModel.setFollowingCount(userModel.getFollowingCount() + 1);
                            } else
                                userModel.setFollowingCount(userModel.getFollowingCount() - 1);

                            firebaseDatabase.getReference().child("Users").child(currentUid)
                                    .setValue(userModel);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


//    private void setFollowings(FollowModelBinding binding, @NonNull FollowModel model, AtomicBoolean isFollowing) {
//        firebaseDatabase.getReference().child("Followings").child(currentUid).child(model.getUserId())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @SuppressLint("SetTextI18n")
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        isFollowing.set(snapshot.exists());
//                        if (isFollowing.get()) {
//                            binding.fmFollowTv.setText("Following");
//                            binding.fmFollowTv.setBackgroundResource(R.drawable.following_bg);
//                        } else {
//                            binding.fmFollowTv.setText("Follow");
//                            binding.fmFollowTv.setBackgroundResource(R.drawable.follow_bg);
//                        }
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


    private void goToUserProfile(@NonNull String userId) {
        if (!userId.equals(currentUid)) {
            ProfileActivity.profileUserId = userId;

            context.startActivity(new Intent(context.getApplicationContext(), ProfileActivity.class));
        } else {
            Toast.makeText(context, "It is your profile, go to profile section instead", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        FollowModelBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = FollowModelBinding.bind(itemView);
        }
    }

}
