package com.firebaseuserregistration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView logout,profileImage;
    private TextView username,email,verifyAccount;
    private Button resendCode,resetPassword,changeProfileImage;

   StorageReference storageReference;


    FirebaseUser user;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        logout =findViewById(R.id.logout);
        profileImage =findViewById(R.id.profileImage);
        username =findViewById(R.id.username);
        email =findViewById(R.id.email);
        changeProfileImage =findViewById(R.id.changeProfile);

        verifyAccount =findViewById(R.id.verifyAccount);
        resendCode =findViewById(R.id.resendCode);
        resetPassword =findViewById(R.id.resetPassowd);


        mAuth =FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        storageReference  = FirebaseStorage.getInstance().getReference();

        //-- image will be uploaded to the user id folder
        StorageReference profileRef =storageReference.child("profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // storage reference gets download image uri and load with glide
                //the profile image will be auto loaded when user open app without having to
                // to update or upload in order to see it
                Glide.with(getApplicationContext())
                        .load(uri)
                        .into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error :" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

         user  =mAuth.getCurrentUser();

        if (!user.isEmailVerified()){
            verifyAccount.setVisibility(View.VISIBLE);
            resendCode.setVisibility(View.VISIBLE);
            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user =mAuth.getCurrentUser();
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Verification email Has been sent", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: "+e.getMessage());
                        }
                    });
                }
            });
        }

        // get users details fro the document
        DocumentReference documentReference  = db.collection("users").document(userId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
        //If there was an error while trying to read the document, the documentSnapshot variable will be null
                //
                if (e != null) {
                    Log.d(TAG, "onEvent: "+e.getMessage());
                    return;
                }

                 assert documentSnapshot != null;
                if (documentSnapshot.exists()){
                    username.setText(documentSnapshot.getString("username"));
                    email.setText(documentSnapshot.getString("email"));
                }else {

                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetPassword =new EditText(view.getContext());

                AlertDialog.Builder passwordResetDialog =new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter the new password > 6 characters");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newPassword = resetPassword.getText().toString();
                        //update user password
                        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

        // log out
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent =new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // change profile image
        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open galler
                Intent intent = new Intent(getApplicationContext(),EditProfile.class);
                intent.putExtra("userName",username.getText().toString());
                intent.putExtra("email",email.getText().toString());
                startActivity(intent);

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}