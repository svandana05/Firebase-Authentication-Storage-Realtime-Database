package com.example.loginapp.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginapp.Adapter.AudioAdapter;
import com.example.loginapp.Adapter.PhotoAdapter;
import com.example.loginapp.Models.Upload;
import com.example.loginapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioFragment extends Fragment {
    private RecyclerView recyclerView;

    List<Upload> photoList = new ArrayList<>();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    String pushKey;
    List<String> keysList = new ArrayList<>();

    private StorageReference storageReference = FirebaseStorage.getInstance().getReference("Audios/" + uid );
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Audios/"+uid);

    private static final int PICK_REQUEST = 1;

    private Uri fileUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audio, container, false);
        ImageView add = rootView.findViewById(R.id.add);
        recyclerView = rootView.findViewById(R.id.recycler_view);

        //FirebaseApp.initializeApp(getContext());

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    addFile();
                }
                else {
                    //Toast.makeText(getActivity(),"Allow Permission.", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            addFile();
        }else {
            Toast.makeText(getActivity(),"Allow permission.", Toast.LENGTH_SHORT).show();
        }
    }

    public void addFile(){
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_REQUEST);
    }

    public void uploadFile(){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading");
        progressDialog.show();

        final String fileName = System.currentTimeMillis()+"";
        final StorageReference sRef  = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(fileUri));

        sRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.dismiss();

                                String downloadUrl = uri.toString();
                                Upload upload = new Upload(fileName, downloadUrl);
                                String uploadId = myRef.push().getKey();
                                myRef.child(uploadId).setValue(upload);
                            }
                        });

                        Toast.makeText(getActivity(),"File uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(),"Failed to upload file.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
            }
        });

    }


    public String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==PICK_REQUEST && resultCode==RESULT_OK && data!=null){
            fileUri = data.getData();
            Toast.makeText(getActivity(),"You choose file: "+ fileUri, Toast.LENGTH_SHORT).show();
            uploadFile();
        }
        else {
            Toast.makeText(getActivity(),"Please select a file.", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                photoList.clear();
                for(DataSnapshot noteSnapshot : dataSnapshot.getChildren()){
                    Upload upload = noteSnapshot.getValue(Upload.class);
                    photoList.add(upload);
                    keysList.add(noteSnapshot.getKey());
                }
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);
                AudioAdapter adapter = new AudioAdapter(AudioFragment.this, photoList, keysList);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public void deleteDialog(final int position){
        String name = photoList.get(position).getName();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete audio file");
        builder.setMessage("Do you want to delete - "+name);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pushKey = keysList.get(position);
                Log.e("PhotoFragment", "Deleted item keysList- " + pushKey);
                try {
                    myRef.child(pushKey).setValue(null)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoList.get(position).getUrl());
                                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getContext(), "Photo deleted successfully. ", Toast.LENGTH_SHORT).show();
                                        }
                                    });
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
