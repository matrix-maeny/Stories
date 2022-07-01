package com.matrix_maeny.stories.fragments.search;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.databinding.SearchModelBinding;
import com.matrix_maeny.stories.posts.PostModel;
import com.matrix_maeny.stories.posts.PostViewerActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.viewHolder> {

    private final Context context;
    private final ArrayList<PostModel> list;

    public SearchAdapter(Context context, ArrayList<PostModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_model, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        PostModel model = list.get(position);

        try {
            Picasso.get().load(model.getImageUrl()).placeholder(R.drawable.placeholder).into(holder.binding.srPostIv);
        } catch (Exception e) {
            e.printStackTrace();
            holder.binding.srPostIv.setImageResource(R.drawable.placeholder);
        }


        holder.binding.srPostTitleTv.setText(model.getTitle());
        holder.binding.srPostTagTv.setText(model.getTagLine());

        holder.binding.srImgForeLayout.setOnClickListener(v -> viewSelectedPost(model));

        holder.binding.srPostTagTv.setOnClickListener(v -> viewSelectedPost(model));

        holder.binding.srPostTitleTv.setOnClickListener(v -> viewSelectedPost(model));
    }


    private void viewSelectedPost(PostModel model) {
        PostViewerActivity.model = model;
        context.startActivity(new Intent(context.getApplicationContext(), PostViewerActivity.class));

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        SearchModelBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = SearchModelBinding.bind(itemView);
        }
    }

}
