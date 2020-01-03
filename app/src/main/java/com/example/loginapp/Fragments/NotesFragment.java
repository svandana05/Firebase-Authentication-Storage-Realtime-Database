package com.example.loginapp.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginapp.Adapter.NotesAdapter;
import com.example.loginapp.Models.Note;
import com.example.loginapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotesFragment extends Fragment {

    private ImageView addnote;
    private RecyclerView recyclerView;

    List<Note> noteList;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    String pushKey;
    List<String> keysList = new ArrayList<>();

    private  FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Notes/"+uid);


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notes, container, false);
        addnote = rootView.findViewById(R.id.addNote);
        recyclerView = rootView.findViewById(R.id.notes_list);
        noteList = new ArrayList<>();

        addnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNoteDialog();
            }
        });

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noteList.clear();
                try {
                    for(DataSnapshot noteSnapshot : dataSnapshot.getChildren()){
                        Note note = noteSnapshot.getValue(Note.class);
                        noteList.add(note);
                        keysList.add(noteSnapshot.getKey());
                    }
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    recyclerView.setLayoutManager(layoutManager);
                    NotesAdapter adapter = new NotesAdapter(NotesFragment.this, noteList, keysList);
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Database error- "+databaseError.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addNoteDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.input_note, null,false);
        final TextInputEditText text = view.findViewById(R.id.et_notes);

        builder.setView(view);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = text.getText().toString();
                if (TextUtils.isEmpty(value)){
                    Toast.makeText(getActivity(), "Please enter some notes!", Toast.LENGTH_SHORT).show();
                }else{
                    Note note = new Note();
                    Date date = new Date();
                    note.setDate(date.toString());
                    note.setText(value);

                    pushKey = myRef.push().getKey();
                    myRef.child(pushKey).setValue(note).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                Toast.makeText(getActivity(), "Note is saved successfully.", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), "Note is not saved.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        builder.show();
    }

    public void updateNoteDialog(final int position){
        String noteText = noteList.get(position).getText();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewLayout = LayoutInflater.from(getActivity()).inflate(R.layout.input_note, null,false);
        final TextInputEditText text = viewLayout.findViewById(R.id.et_notes);
        text.setText(noteText);
        builder.setView(viewLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (text.getText().length()<1) {
                    Toast.makeText(getActivity(), "Please enter some notes!", Toast.LENGTH_SHORT).show();
                }else {
                    Note note = new Note();
                    Date date = new Date();
                    note.setDate(date.toString());
                    note.setText(text.getText().toString());
                    pushKey = keysList.get(position);
                    Log.e("NotesFragment", "Updated item keysList- "+pushKey);
                    try {
                        myRef.child(pushKey).setValue(note)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "Note is update successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void deleteNoteDialog(final int position){
        String noteText = noteList.get(position).getText();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Note");
        builder.setMessage(noteText);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pushKey = keysList.get(position);
                Log.e("NotesFragment", "Deleted item keysList- " + pushKey);
                try {
                    myRef.child(pushKey).setValue(null)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Note is deleted successfully.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

}
