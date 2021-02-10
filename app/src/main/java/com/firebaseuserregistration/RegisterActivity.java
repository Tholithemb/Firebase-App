package com.firebaseuserregistration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "add user data";
    FirebaseAuth mAuth;

    private ProgressBar progressBar;
    private TextInputLayout username, email, password, confirmPassword;
    String textUsername,textEmail,textPassword;
    Button register;
    ImageView backLogin;

    FirebaseFirestore db;
    String userId;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{4,}" +               //at least 4 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        backLogin = findViewById(R.id.backLogin);
        register = findViewById(R.id.register);
        username = findViewById(R.id.username);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);

        progressBar = findViewById(R.id.progressBar);

        // instantiate Firebase object

        mAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!validateUsername() | !validateEmail() | !validatePassword()) {
                    Toast.makeText(RegisterActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                // sign up new user with email and password
                mAuth.createUserWithEmailAndPassword(textEmail,textPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // verify email address
                                    FirebaseUser user =mAuth.getCurrentUser();
                                    assert user != null;
                                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RegisterActivity.this, "Verification email Has been sent", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Log.d(TAG, "onFailure: "+e.getMessage());

                                        }
                                    });

                                    Toast.makeText(RegisterActivity.this, "New account is created", Toast.LENGTH_SHORT).show();
                                    addUserDataFirebase();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });
        backLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private boolean validateUsername() {
         textUsername = username.getEditText().getText().toString().trim();
        if (textUsername.isEmpty()) {
            username.setError("Field cannot be empty!");
            return false;
        } else if (textUsername.length() > 20 || textUsername.length() < 8) {
            username.setError("username should be between 8-20 characters");
            return false;
        } else {
            username.setErrorEnabled(false);
            username.setError(null);
            return true;
        }

    }
    private boolean validateEmail() {
         textEmail = email.getEditText().getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
            email.setError("Enter a valid email!");
            return false;
        } else if (textEmail.isEmpty()) {
            email.setError("Field cannot be empty");
            return false;
        } else {
            email.setErrorEnabled(false);
            email.setError(null);
            return true;
        }
    }
    private boolean validatePassword() {
         textPassword = password.getEditText().getText().toString().trim();
        String cpassword = confirmPassword.getEditText().getText().toString().trim();

        if (textPassword.isEmpty() && cpassword.isEmpty()) {
            password.setError("Field cannot be empty ");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(textPassword).matches()) {
            password.setError("Password too weak");
            return false;
        } else if (!textPassword.equals(cpassword)) {
            confirmPassword.setError("Password does not match");
            return false;
        } else {
            password.setErrorEnabled(false);
            password.setError(null);
            return true;
        }
    }

private void addUserDataFirebase(){

    // Add a new document with a generated ID
    userId = mAuth.getCurrentUser().getUid();
     DocumentReference documentReference= db.collection("users").document(userId);

    // Create a new user with a first and last name
    Map<String, Object> user = new HashMap<>();

    user.put("username", textUsername);
    user.put("email", textEmail);
    user.put("born", 1815);

    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
         @Override
         public void onSuccess(Void aVoid) {
             Log.d(TAG, "onSuccess: "+ "user profile is created for "+ userId);
         }
     }).addOnFailureListener(new OnFailureListener() {
         @Override
         public void onFailure(@NonNull Exception e) {
             Log.d(TAG, "onFailure: "+ e.getMessage());
         }
     });

    }
}