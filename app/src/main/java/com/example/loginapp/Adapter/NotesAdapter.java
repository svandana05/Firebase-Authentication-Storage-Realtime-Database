package com.example.loginapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginapp.Fragments.NotesFragment;
import com.example.loginapp.Models.Note;
import com.example.loginapp.R;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.DataViewHolder> {
    private NotesFragment fragment;
    private List<Note> noteList;
    private List<String> keysList;

    public NotesAdapter(NotesFragment fragment, List<Note> noteList, List<String> keysList){
        this.fragment = fragment;
        this.noteList = noteList;
        this.keysList = keysList;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.notes_item_list, null, true);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, final int position) {
        holder.tvNote.setText(noteList.get(position).getText());
        String date = noteList.get(position).getDate();
        String[] arrOfStr = date.split("GMT");
        holder.tvDate.setText(arrOfStr[0]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.updateNoteDialog(position);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                fragment.deleteNoteDialog(position);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
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
        TextView tvNote, tvDate;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNote = itemView.findViewById(R.id.tv_name);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
