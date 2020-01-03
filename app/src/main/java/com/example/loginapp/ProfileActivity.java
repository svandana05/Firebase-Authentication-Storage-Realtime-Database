package com.example.loginapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.loginapp.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    EditText etName, etEmail;
    ImageView ivImageAvtar;
    FloatingActionButton editImage;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    private Uri fileUri;
    Bitmap bitmap;
    String downloadUrl;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Users/"+uid);
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference("UserAvatar/" + uid );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = (EditText) findViewById(R.id.editTextName);
        etEmail = (EditText) findViewById(R.id.editText1Email);
        ivImageAvtar = findViewById(R.id.userImage);
        editImage = (FloatingActionButton) findViewById(R.id.editImage);

        //etEmail.setText(user.getEmail());

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange( DataSnapshot dataSnapshot) {
                    User user1 = dataSnapshot.getValue(User.class);
                    if (user1 != null){
                        etName.setText(user1.getUserName());
                        etEmail.setText(user1.getUserEmail());
                        downloadUrl = user1.getUserImage();
                        Glide.with(getApplicationContext()).load(downloadUrl).into(ivImageAvtar);
                    }

                }

                @Override
                public void onCancelled( DatabaseError databaseError) {

                }
            });


        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if (requestCode==1 && resultCode==RESULT_OK && data!=null){
            fileUri = data.getData();
            Toast.makeText(this,"You choose file: "+ fileUri, Toast.LENGTH_SHORT).show();
            ivImageAvtar.setImageURI(fileUri);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //uploadFile();
        }
        else {
            Toast.makeText(this,"Please select a file.", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void uploadFile(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        final StorageReference sRef  = storageReference;

        sRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.dismiss();

                                downloadUrl = uri.toString();
                            }
                        });

                        Toast.makeText(getApplicationContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed to upload file.", Toast.LENGTH_SHORT).show();
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
        ContentResolver cR = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void saveDetail(){
        String name, email, imageUrl = "";
        name = etName.getText().toString();
        email = etEmail.getText().toString();

        User user = new User();
        user.setUserName(name);
        user.setUserEmail(email);
        try {
            imageUrl = downloadUrl;
            if (imageUrl.length()>1){
                user.setUserImage(imageUrl);
            }

        }catch (NullPointerException e){
            e.printStackTrace();
        }

        myRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete( Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(ProfileActivity.this, "User is saved successfully.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ProfileActivity.this, "User is not saved.", Toast.LENGTH_SHORT).show();
            }
        });

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Name: "+etName.getText().toString());
                builder.setMessage("Email: "+etEmail.getText().toString());
                builder.show();
                createDirectoryAndSaveFile(bitmap, System.currentTimeMillis()+".jpg");

                //saveDetail();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File directory = new File(Environment.getExternalStorageDirectory() + "/LoginApp/");

        if (!directory.exists()) {
            File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + "/LoginApp/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File(Environment.getExternalStorageDirectory() + "/LoginApp/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(this, "QRCode Saved Successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
