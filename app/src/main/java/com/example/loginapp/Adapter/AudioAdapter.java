package com.example.loginapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.loginapp.Fragments.AudioFragment;
import com.example.loginapp.Fragments.PhotoFragment;
import com.example.loginapp.Models.Upload;
import com.example.loginapp.R;

import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.DataViewHolder>{
    private AudioFragment fragment;
    private List<Upload> uploads;
    private List<String> keysList;


    public AudioAdapter(AudioFragment fragment, List<Upload> noteList, List<String> keysList){
        this.fragment = fragment;
        this.uploads = noteList;
        this.keysList = keysList;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.photo_item_list, null, true);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, final int position) {

        holder.tvName.setText(uploads.get(position).getName());
        Glide.with(fragment.getActivity()).load(uploads.get(position).getUrl()).into(holder.ivImage);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                fragment.deleteDialog(position);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class DataViewHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        ImageView ivImage;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_image_name);
            ivImage = itemView.findViewById(R.id.iv_image_view);
        }
    }

}
