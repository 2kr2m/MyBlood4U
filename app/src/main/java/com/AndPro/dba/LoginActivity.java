package com.AndPro.dba;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    //initializing variables
    private TextView registerButton;
    private TextInputEditText loginEmail,loginPassword;
    private TextView forgotPassword;
    private Button loginButton;

    private ProgressDialog loader;

    //intializing firebase variables wich will let me deal with data in firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //getting the current user
                FirebaseUser user = mAuth.getCurrentUser();
                //if there is a connected user we move directly to the main page
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            }


        };
        //linking my views to the initialized variables
        registerButton=findViewById(R.id.registerButton);
        loginEmail=findViewById(R.id.loginEmail);
        loginPassword=findViewById(R.id.loginPassword);
        forgotPassword=findViewById(R.id.forgotPassword);
        loginButton=findViewById(R.id.loginButton);


        //making a loader
        loader=new ProgressDialog(this);


        //if we don't have an account this button let us make one
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,SelectRegistrationActivity.class);
                startActivity(intent);
            }
        });

        //after verifying typed data loginbutton sign the user to his account
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //linking mail and password
                final String email=loginEmail.getText().toString().trim();
                final String password=loginPassword.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    loginEmail.setError("type your email please!");
                }
                if (TextUtils.isEmpty(password)){
                    loginPassword.setError("type your password please!");
                }
                else{
                    //if all is going well th application processing logging
                    loader.setMessage("Processing Login ... ");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //in this case the user logged successfully
                            if (task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "You are successfully logged in", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            //in this case the user failed to log
                            }else {
                                Toast.makeText(LoginActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                            loader.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //in the opening of the app we want remember the last connected user
        //mAuth.addAuthStateListener(authStateListener);
        mAuth.removeAuthStateListener(authStateListener);//in the opening of the app we don't want remember the last connected user
    }



    @Override
    protected void onStop() {
        super.onStop();
        //removing the authentication
        mAuth.removeAuthStateListener(authStateListener);
    }
}