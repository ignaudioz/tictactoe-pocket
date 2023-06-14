package com.audioz.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class login extends AppCompatActivity {
    //TextView
    TextView status;
    //EditTexts
    EditText email, password;
    //Buttons
    Button loginbtn;
    ImageButton leavesetup;
    // Strings.
    String mail, pass;
    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Progress bar

        //Firebase
        // * Getting our authentication instance.
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null) { // if user uid isn't null, which means he is logged in. bring him to da next page ;~
            Toast.makeText(login.this, "You have logged-in successfully!", Toast.LENGTH_SHORT)
                    .show();
            Intent i = new Intent(login.this, RoomSelector.class);
            startActivity(i);
            finish();
        }

        // TextViews
        status = findViewById(R.id.status);
        // EditTexts
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        // Buttons
        loginbtn = findViewById(R.id.loginbtn);
        leavesetup = findViewById(R.id.leavesetup);

        leavesetup.setOnClickListener(view -> {
            finish();
        });



        status.setOnClickListener(view -> {
            Intent i = new Intent(this,register.class);
            startActivity(i);
            finish();
        });

        loginbtn.setOnClickListener(view ->{
            mail = String.valueOf(email.getText());
            pass = String.valueOf(password.getText());

            // Pretty self-explanatory..
            if (TextUtils.isEmpty(mail)) {
                email.setError("E-mail is required!");
                email.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(pass)) {
                password.setError("Password is required!");
                password.requestFocus();
                return;
            }
            if (pass.length()<5) {
                password.setError("Password length should be greater than 5!");
                password.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                email.setError("Please provide valid email!");
                email.requestFocus();
                return;
            }

            /* Signing in with provided email&password.
            if task is successful which means credentials were correct then continue, else:
            alert the user of false authentication credentials. */
            mAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(login.this, "You have logged-in successfully!",Toast.LENGTH_SHORT)
                                .show();
                        Intent i = new Intent(login.this,RoomSelector.class);
                        startActivity(i);
                        finish();
                    }else{
                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                        switch (errorCode){
                            case "ERROR_USER_NOT_FOUND":
                                Toast.makeText(login.this, "There is no user record corresponding to this identifier. The user may have been deleted.", Toast.LENGTH_LONG).show();
                                break;
                            case "ERROR_WRONG_PASSWORD":
                                Toast.makeText(login.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                                password.setError("password is incorrect ");
                                password.requestFocus();
                                password.setText("");
                                break;
                            default:
                                Toast.makeText(login.this,errorCode, Toast.LENGTH_LONG).show();
                        }
                        email.setError("");
                    }
                }
            });
        });
    }
}