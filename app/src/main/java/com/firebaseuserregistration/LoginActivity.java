package com.firebaseuserregistration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    Button signUp, createAccount,forgetPassword;
    String shareEmail, sharePassword;
    private TextInputLayout email, password;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;

    // regular expression for password pattern
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
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signUp = findViewById(R.id.signup);
        forgetPassword =findViewById(R.id.forgetPassword);
        createAccount = findViewById(R.id.create_account);
        progressBar = findViewById(R.id.progressBarLogin);
        // Firebase instance
        mAuth = FirebaseAuth.getInstance();

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!validateEmail() | !validatePassword()) {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    // sign in an already existing user
                    mAuth.signInWithEmailAndPassword(email.getEditText().getText().toString(), password.getEditText().getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progressBar.setVisibility(View.GONE);
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(LoginActivity.this, "Successfully logged in ", Toast.LENGTH_SHORT).show();
                                    } else {

                                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });

                } catch (Exception exception) {
                    Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // reset user password
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetMail =new EditText(view.getContext());

                AlertDialog.Builder passwordResetDialog =new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mail = resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset Link has been sent to your email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error ! Reset Link not sent :"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                passwordResetDialog.create().show();
            }
        });

    }

    private boolean validateEmail() {
        String textEmail = email.getEditText().getText().toString().trim();

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
        String textPassword = password.getEditText().getText().toString().trim();

        if (textPassword.isEmpty()) {
            password.setError("Field cannot be empty ");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(textPassword).matches()) {
            password.setError("Password too weak");
            return false;
        } else {
            password.setErrorEnabled(false);
            password.setError(null);
            return true;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        // check to see if the user is currently signed in.
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}