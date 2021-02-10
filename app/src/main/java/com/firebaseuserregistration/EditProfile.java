package com.firebaseuserregistration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private EditText usernameEdit;
    private EditText emailEdit;
    private ImageView profileImage;
    private Button saveBtn;

    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        usernameEdit =findViewById(R.id.editUsername);
        emailEdit = findViewById(R.id.editEmail);
        profileImage =findViewById(R.id.editProfile);
        saveBtn =findViewById(R.id.saveProfile);

        // firebase

        mAuth =FirebaseAuth.getInstance();
        fStore =FirebaseFirestore.getInstance();
        user =mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        userId =mAuth.getCurrentUser().getUid();


        Intent data = getIntent();
        String fullName =data.getStringExtra("userName");
        String email = data.getStringExtra("email");

        usernameEdit.setText(fullName);
        emailEdit.setText(email);

        StorageReference profileRef = storageReference.child("profile.jpg");
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
                Toast.makeText(EditProfile.this, "Error :" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGalleryIntent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usernameEdit.getText().toString().isEmpty() || emailEdit.getText().toString().isEmpty()){
                    Toast.makeText(EditProfile.this, "One of the fields is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                  String email = emailEdit.getText().toString();
                user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef =fStore.collection("users").document(user.getUid());

                        Map<String,Object> edited  =new HashMap<>();
                        edited.put("email",email);
                        edited.put("username",usernameEdit.getText().toString());

                        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                finish();
                            }
                        });

                        Toast.makeText(EditProfile.this, "Email is changed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "Error :"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK){
            if (data == null){
                Uri imageUri =data.getData();
                profileImage.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            }
        }
    }
    private void uploadImageToFirebase(Uri imageUri) {

        // upload image to firebase storage
        StorageReference fileRef = storageReference.child("profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .into(profileImage);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Failed : " +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
