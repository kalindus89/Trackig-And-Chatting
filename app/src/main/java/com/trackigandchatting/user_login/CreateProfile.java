package com.trackigandchatting.user_login;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.trackigandchatting.main_chat_activities.MainActivity;
import com.trackigandchatting.R;
import com.trackigandchatting.SessionManagement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateProfile extends AppCompatActivity {

    ImageView getuserimageinimageview;
    CardView getuserimage;
    EditText getusername;
    Button saveProfile;
    ProgressBar progressbarofsetProfile;
    Uri imagePath;
    String name;
    private static int PICK_IMAGE = 123;

    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage; // save images
    FirebaseFirestore firebaseFirestore; // save users profile urls and chats
    String imageUriAccessToken;

    ActivityResultLauncher<Intent> getImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        imagePath = data.getData();
                        getuserimageinimageview.setImageURI(imagePath);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getuserimageinimageview = findViewById(R.id.getuserimageinimageview);
        getuserimage = findViewById(R.id.getuserimage);
        getusername = findViewById(R.id.getusername);
        progressbarofsetProfile = findViewById(R.id.progressbar);
        saveProfile = findViewById(R.id.saveProfile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        getuserimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                getImageResultLauncher.launch(intent);
            }
        });


        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = getusername.getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();
                } else if (imagePath == null) {
                    Toast.makeText(getApplicationContext(), "Image is Empty", Toast.LENGTH_SHORT).show();
                } else {
                    progressbarofsetProfile.setVisibility(View.VISIBLE);
                    saveProfile.setEnabled(false);
                    sendImageToStorage();
                }
            }

        });

    }

    private void sendImageToStorage() {

        StorageReference storageReference = firebaseStorage.getReference().child("Images").child(firebaseAuth.getUid()).child("Profile Pic");

        //Image compresesion
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        ///putting image to storage

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUriAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "Uri get success", Toast.LENGTH_SHORT).show();
                        sendDataToCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressbarofsetProfile.setVisibility(View.INVISIBLE);
                        saveProfile.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "URI get Failed", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(getApplicationContext(), "Image is uploaded", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressbarofsetProfile.setVisibility(View.INVISIBLE);
                saveProfile.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Image Not UpLoaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDataToCloudFirestore() {

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String, Object> userdata = new HashMap<>();
        userdata.put("name", name);
        userdata.put("image", imageUriAccessToken);
        userdata.put("uid", firebaseAuth.getUid());
        userdata.put("status", "Online");

        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Data on Cloud Firestore send success", Toast.LENGTH_SHORT).show();

                progressbarofsetProfile.setVisibility(View.INVISIBLE);
                saveProfile.setEnabled(true);

                SessionManagement.setUserDetails(getApplicationContext(),name,imageUriAccessToken);

                Intent intent = new Intent(CreateProfile.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

}