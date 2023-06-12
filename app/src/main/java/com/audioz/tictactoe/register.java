package com.audioz.tictactoe;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class register extends AppCompatActivity {
    //EditTexts
    private EditText username,email,password;
    //Buttons
    private ImageButton pfpbtn;
    //Strings
    private String name,mail,pass;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseStorage storage; // firebase storage for profile pictures.
    private StorageReference sRef,sAvatars;
    // 2 booleans to identify action (open camera; open gallery).
    private boolean cameraOpen=false,galleryOpen=false;
    // Selected avatar image in bytes
    private byte[] imageData;
    // Progressbar for registering data.
    private ProgressDialog pg;
    // ActivityResult launcher setting internally so we can access in onRequestPermissionsResult func.
    private ActivityResultLauncher<Intent> launcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // SharePreferences for profile picture.
//        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
//        editor = sharedPreferences.edit();

        //Firebase
        // * Getting our authentication instance.
        mAuth = FirebaseAuth.getInstance(); // getting Authentication instance.
        storage = FirebaseStorage.getInstance("gs://tic-tac-toe-pocket.appspot.com"); // getting Storage instance. specifying instance just in-case..
        sRef = storage.getReference(); // Storage reference to get/uplaod images.

        // TextViews
        TextView status = findViewById(R.id.status);
        // EditTexts
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        // Buttons
        Button registerbtn = findViewById(R.id.loginbtn);
        ImageButton leavesetup = findViewById(R.id.leavesetup);
        pfpbtn = findViewById(R.id.pfpbtn);

        // Creating dialog which will open when pfp logo is clicked.
        Dialog galleryOrCamera = new Dialog(register.this);
        galleryOrCamera.setContentView(R.layout.dialog_choose_image);

        // ActivityResultLauncher for Camera or Gallery intents.
        launcher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // if user enter one of the options and don't select nothing don't do any action.
                if (result.getData()!=null) {
                    // if camera option is picked do the following.
                    if (cameraOpen) {
                        Intent cameraIntent = result.getData();
                        Bitmap bitmap = (Bitmap) cameraIntent.getExtras().get("data");

                        // Converting bitmap to bytes for firebase ;)
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        // Compressing bitmap image to jpg file extension, not degrading quality, out-put to bytes file.
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        imageData = bytes.toByteArray(); // setting image bytes to selected avatar.

                        pfpbtn.setImageBitmap(bitmap);
                        cameraOpen = false;
                        galleryOrCamera.dismiss();
                    }
                    // if gallery option is picked do the following.
                    if (galleryOpen) {
                        Intent galleryIntent = result.getData();
                        Uri selectedImage = galleryIntent.getData();

                        // Converting Uri to bitmap.. smh
                        getContentResolver().notifyChange(selectedImage, null);
                        ContentResolver cr = getContentResolver();
                        Bitmap bitmap = null;

                        try { // converting Uri to bitmap.
                            bitmap = MediaStore.Images.Media
                                        .getBitmap(cr, selectedImage);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        // Converting bitmap to bytes for firebase ;)
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        // Compressing bitmap image to jpg file extension, not degrading quality, out-put to bytes file.
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        imageData = bytes.toByteArray(); // setting image bytes to selected avatar.

                        pfpbtn.setImageURI(selectedImage);
                        galleryOpen = false;
                        galleryOrCamera.dismiss();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Upload An image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Creating dialog which will open when pfp logo is clicked.
        galleryOrCamera.setCanceledOnTouchOutside(true); // cancling image picking by clicking out-side the dialog.
        galleryOrCamera.setCancelable(true);

        LinearLayout cam,gal;
        cam = galleryOrCamera.findViewById(R.id.lytCameraPick);
        gal = galleryOrCamera.findViewById(R.id.lytGalleryPick);

        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
                if(cameraOpen)
                    launcher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            }
        });

        gal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryOpen=true;
                launcher.launch(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"));
            }
        });

        pfpbtn.setOnClickListener(view -> {
          galleryOrCamera.show();
        });

        leavesetup.setOnClickListener(view -> {
            finish();
        });
        status.setOnClickListener(view -> {
            finish();
        });

        registerbtn.setOnClickListener(view -> {
            name = String.valueOf(username.getText());
            mail = String.valueOf(email.getText());
            pass = String.valueOf(password.getText());

            // the code speaks for itself...
            boolean error = false;

            if (TextUtils.isEmpty(name)) {
                username.setError("Username is required!");
                error = true;
            }
            if (name.contains(" ") || name.contains(".")) {
                username.setError("Spaces or dots are not allowed!");
                error = true;
            }
            if (TextUtils.isEmpty(mail) || !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                email.setError("Please provide a valid email!");
                error = true;
            }
            if (TextUtils.isEmpty(pass)) {
                password.setError("Password is required!");
                error = true;
            }
            if (name.length()<3 || name.length()>8) {
                username.setError("Password length should be greater than 5!");
                error = true;
            }
            if (pass.length()<5) {
                password.setError("Password length should be greater than 5!");
                error = true;
            }
            if(error)
                return;

            // Creating ProgressDialog so user won't spam requests and won't left hanging in register activity without action.
            pg = new ProgressDialog(this);
            pg.setTitle("Registering");
            pg.setMessage("Please wait, registering ur information.");
            pg.setCancelable(false);
            pg.show();

            mAuth.createUserWithEmailAndPassword(mail, pass) // creating a user with email and password.
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // if user didn't select a profile picture, continue with default pfp/url.
                                if(!(imageData==null)){
                                    /*Creating User-profile with custom/selected pfp-url start*/
                                    sAvatars = sRef.child("avatars/"+mAuth.getCurrentUser().getUid()+".jpg"); // setting path to avatars, setting filename to random uuid.jpg
                                    UploadTask uploadTask = sAvatars.putBytes(imageData); // putting/uploading image to storage.
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(name)
                                                    .setPhotoUri(Uri.parse("avatars/"+mAuth.getCurrentUser().getUid()+".jpg")) // Uploaded image url.
                                                    .build();
                                            mAuth.getCurrentUser().updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    pg.dismiss(); // disabling progressdialog.
                                                    Toast.makeText(register.this, "You have been registered successfully!", Toast.LENGTH_SHORT)
                                                            .show();
                                                    Intent i = new Intent(register.this, RoomSelector.class);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                    /*Creating User-profile with custom/selected pfp-url end*/
                                }else {
                                    /*Creating User-profile with default pfp-url start*/
                                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .setPhotoUri(Uri.parse("avatars/default_profile.webp"))
                                            .build();
                                    mAuth.getCurrentUser().updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            pg.dismiss(); // disabling progressdialog.
                                            Toast.makeText(register.this, "You have been registered successfully!", Toast.LENGTH_SHORT)
                                                    .show();
                                            Intent i = new Intent(register.this, RoomSelector.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    });
                                    /*Creating User-profile with default pfp-url end*/
                                }
                            }else // (!task.isSuccessful())
                            {
                                email.setError("E-mail is already used!");
                               pg.dismiss();
                                Toast.makeText(register.this, "Register Failed! Contact author for support.",Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });

        });

    }

    // Checks if user granted camera permission and act accordingly to the result.
    private void checkCameraPermission() {
        /* if user don't have camera permission then:
         *  request camera permission: goto onRequest func code.
         *  else:
         *  set cameraOpen to true which will make camera opening function continue normally.
         */
       if(ActivityCompat.checkSelfPermission(register.this,
               Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
           ActivityCompat.requestPermissions(register.this,
                   new String[]{android.Manifest.permission.CAMERA}, 101);
       }else{
           cameraOpen=true;
       }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //The request code of camera permission is 101.
        if(requestCode==101){
            /* Camera permission section.*/
            /* if user granted permission then:
            *   set cameraOpen to true and launch our camera.
            *  else:
            *   make a Toast which will alert the user that camera permission is a must in order to take an avatar using the camera.*/
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraOpen=true;
                launcher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            }else{
                Toast.makeText(this,getString(R.string.permission_camera_denied),Toast.LENGTH_LONG).show();
            }
            /* Camera permission section end. */
    }
}
}