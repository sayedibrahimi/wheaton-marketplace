package com.example.wheaton_market;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


// SignupActivity.java
public class Signup extends AppCompatActivity {

    EditText
            signUp_email_editText,
            signUp_password_editText,
            signUp_firstName_editText,
            signUp_lastName_editText,
            signUp_wheatonID_editText;
    Button
            signUp_signUp_Button,
            signUp_login_Button;

    TextView
            signUp_warning_textView;

    FirebaseAuth mAuth;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        signUp_firstName_editText = findViewById(R.id.signUp_firstName_editText);
        signUp_lastName_editText = findViewById(R.id.signUp_lastName_editText);
        signUp_wheatonID_editText = findViewById(R.id.signUp_wheatonID_editText);
        signUp_email_editText = findViewById(R.id.signUp_email_editText);
        signUp_password_editText = findViewById(R.id.signUp_password_editText);
        signUp_signUp_Button = findViewById(R.id.signUp_signUp_Button);
        signUp_login_Button = findViewById(R.id.signUp_login_Button);
        signUp_warning_textView = findViewById(R.id.signUp_warning_textView);
//        Missing info: First name, Last name, WID, Email, Password

        Button linkToLoginPage = signUp_login_Button;
        linkToLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Signup.this, Login.class));
                finish();
            }

        });

        Button signupButton = signUp_signUp_Button;
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String firstName, lastName, wheatonID, email, password;
                firstName = String.valueOf(signUp_firstName_editText.getText());
                lastName = String.valueOf(signUp_lastName_editText.getText());
                wheatonID = String.valueOf(signUp_wheatonID_editText.getText());
                email = String.valueOf(signUp_email_editText.getText());
                password = String.valueOf(signUp_password_editText.getText());

                StringBuilder builder = new StringBuilder();
                builder.append("WARNING MISSING: ");
                boolean missing_item = false;

                if (TextUtils.isEmpty(firstName)) {
                    Toast.makeText(Signup.this, "Enter first name", Toast.LENGTH_SHORT).show();
                    builder.append("first name");
                    missing_item = true;
                }
                if (TextUtils.isEmpty(lastName)) {
                    Toast.makeText(Signup.this, "Enter last name", Toast.LENGTH_SHORT).show();
                    if (missing_item) {
                        builder.append(", last name");
                    } else {
                        builder.append("last name");
                    }
                    missing_item = true;
                }
                if (TextUtils.isEmpty(wheatonID)) {
                    Toast.makeText(Signup.this, "Enter Wheaton ID", Toast.LENGTH_SHORT).show();
                    if (missing_item) {
                        builder.append(", WID");
                    } else {
                        builder.append("WID");
                    }
                    missing_item = true;
                }
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Signup.this, "Enter email", Toast.LENGTH_SHORT).show();
                    if (missing_item) {
                        builder.append(", email");
                    } else {
                        builder.append("email");
                    }
                    missing_item = true;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Signup.this, "Enter password", Toast.LENGTH_SHORT).show();
                    if (missing_item) {
                        builder.append(", password");
                    } else {
                        builder.append("password");
                    }
                    missing_item = true;
                }

                if (missing_item) {
                    signUp_warning_textView.setText(builder.toString());
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                StringBuilder auth = new StringBuilder();
                                auth.append("Failed to save user information");

                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        // Create a new User object
                                        String userPictureURL = "";
                                        User user = new User(firstName, lastName, wheatonID, email, userPictureURL);

                                        // Save user data to the database
                                        FirebaseDatabase.getInstance().getReference("users")
                                                .child(firebaseUser.getUid())
                                                .setValue(user)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            startActivity(new Intent(Signup.this, Login.class));
                                                            finish();
                                                            Toast.makeText(Signup.this, "Account created Successfully",
                                                                    Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(Signup.this, "Failed to save user information",
                                                                    Toast.LENGTH_SHORT).show();
                                                            signUp_warning_textView.setText(builder.toString());
                                                        }
                                                    }
                                                });
                                    }
                                }
                                else {
                                    // Signup failed, display an error message
                                    Toast.makeText(Signup.this, "Failed to create account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        });

    }


}