package com.example.loginapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button Register;
    private TextView tvInfo;
    private TextView Login;
    String userEmail, userPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = (EditText) findViewById(R.id.etRegisterEmail);
        etPassword = (EditText) findViewById(R.id.etRegisterPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        Register = (Button) findViewById(R.id.btnRegister);
        tvInfo = (TextView) findViewById(R.id.tvRegisterInfo);
        tvInfo.setTextColor(Color.RED);
        Login = (TextView) findViewById(R.id.tvLogin);

        mAuth = FirebaseAuth.getInstance();

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    public void createAccount(){
        userEmail = etEmail.getText().toString().trim();
        userPassword = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if(userEmail.isEmpty() && userPassword.isEmpty() && confirmPassword.isEmpty()){
            tvInfo.setText("All fields are required.");
        }else{
            if (userPassword.equals(confirmPassword)){

                mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("RegistrationActivity", "createUserWithEmail:success");
                                    Toast.makeText(RegisterActivity.this, "Registration Completed.",
                                            Toast.LENGTH_SHORT).show();
//                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
//                                    startActivity(intent);
                                    Toast.makeText(getApplicationContext(), "Login now!",
                                            Toast.LENGTH_SHORT).show();
                                    FirebaseAuth.getInstance().signOut();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("RegistrationActivity", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterActivity.this, "Authentication failed. Check your internet connection and try again!",
                                            Toast.LENGTH_SHORT).show();

                                }

                                // ...
                            }
                        });
            }else{
                tvInfo.setText("Password didn't matched.");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }
}
