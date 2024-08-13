package com.example.wheaton_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

// LoginActivity.java
public class Login extends AppCompatActivity {

    EditText
            login_email_editText,
            login_password_editText;

    Button
            login_login_Button,
            login_creatAccount_Button;

    TextView
            login_warning_textView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        login_email_editText = findViewById(R.id.login_email_editText);
        login_password_editText = findViewById(R.id.login_password_editText);
        login_login_Button = findViewById(R.id.login_login_Button);
        login_creatAccount_Button = findViewById(R.id.login_creatAccount_Button);
        login_warning_textView = findViewById(R.id.login_warning_textView);

        Button linkToSignupPage = login_creatAccount_Button;
        linkToSignupPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Signup.class));
                finish();
            }
        });

        Button loginButton = login_login_Button;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email, password;
                email = String.valueOf(login_email_editText.getText());
                password = String.valueOf(login_password_editText.getText());

                StringBuilder builder = new StringBuilder();
                builder.append("WARNING MISSING: ");
                boolean missing_item = false;

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                    builder.append("email");
                    missing_item = true;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                    if (missing_item) {
                        builder.append(", password");
                    } else {
                        builder.append("password");
                    }
                    missing_item = true;
                }

                if (missing_item) {
                    login_warning_textView.setText(builder.toString());
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                StringBuilder auth = new StringBuilder();
                                auth.append("Invalid email or password");

                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Login Successful",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    login_warning_textView.setText(auth.toString());
                                }
                            }
                        });
            }
        });

        // Add login functionality
    }
}