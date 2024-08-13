//package com.example.wheaton_market;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.EmailAuthProvider;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class AccountEditPage extends AppCompatActivity {
//
//    private EditText firstNameEditText, lastNameEditText, emailEditText, widEditText;
//    private EditText currentPasswordEditText, newPasswordEditText;
//    private DatabaseReference database;
//    private Button saveButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_account_edit_page);
//
//        database = FirebaseDatabase.getInstance().getReference("users");
//
//        firstNameEditText = findViewById(R.id.acc_edit_first_editText);
//        lastNameEditText = findViewById(R.id.acc_edit_last_editText);
//        // emailEditText = findViewById(R.id.acc_edit_email_editText); // Uncomment if using
//        widEditText = findViewById(R.id.acc_edit_id_editText);
//        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
//        newPasswordEditText = findViewById(R.id.newPasswordEditText);
//        Button changePasswordButton = findViewById(R.id.changePasswordButton);
//        saveButton = findViewById(R.id.save);
//
//        changePasswordButton.setOnClickListener(view -> {
//            currentPasswordEditText.setVisibility(View.VISIBLE);
//            newPasswordEditText.setVisibility(View.VISIBLE);
//            saveButton.setVisibility(View.VISIBLE);
//        });
//
//        saveButton.setOnClickListener(view -> {
//            if (currentPasswordEditText.getVisibility() == View.VISIBLE && !newPasswordEditText.getText().toString().trim().isEmpty()) {
//                changeUserPassword();
//            } else {
//                saveUserInformation(); // Call this when not changing password
//            }
//        });
//    }
//
//    private void changeUserPassword() {
//        String currentPassword = currentPasswordEditText.getText().toString().trim();
//        String newPassword = newPasswordEditText.getText().toString().trim();
//
//        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
//            Toast.makeText(this, "Please enter all password details", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
//            user.reauthenticate(credential).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
//                        if (task1.isSuccessful()) {
//                            Toast.makeText(this, "Password successfully updated", Toast.LENGTH_SHORT).show();
//                            saveUserInformation(); // Assuming updates beyond password are managed separately
//                        } else {
//                            Toast.makeText(this, "Error updating password: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } else {
//                    Toast.makeText(this, "Reauthentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                }
//            });
//        } else {
//            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void saveUserInformation() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
//            Map<String, Object> userUpdates = new HashMap<>();
//            userUpdates.put("firstName", firstNameEditText.getText().toString().trim());
//            userUpdates.put("lastName", lastNameEditText.getText().toString().trim());
//            // Include other fields as necessary
//            ref.updateChildren(userUpdates)
//                    .addOnSuccessListener(aVoid -> Toast.makeText(AccountEditPage.this, "User info updated successfully.", Toast.LENGTH_SHORT).show())
//                    .addOnFailureListener(e -> Toast.makeText(AccountEditPage.this, "Failed to update user info.", Toast.LENGTH_SHORT).show());
//        } else {
//            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
//        }
//    }
//}


package com.example.wheaton_market;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AccountEditPage extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, emailEditText, widEditText;
    private EditText currentPasswordEditText, newPasswordEditText;
    private DatabaseReference database;
    private Button saveChangesButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit_page);

        database = FirebaseDatabase.getInstance().getReference("users");

        // Setup UI components
        firstNameEditText = findViewById(R.id.acc_edit_first_editText);
        lastNameEditText = findViewById(R.id.acc_edit_last_editText);
        //emailEditText = findViewById(R.id.acc_edit_email_editText);  // Uncommented for email
        widEditText = findViewById(R.id.acc_edit_id_editText);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        saveChangesButton = findViewById(R.id.acc_edit_save_Button);
        cancelButton = findViewById(R.id.acc_edit_cancel_Button);

        // Setup the password change button
        Button changePasswordButton = findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(view -> {
            currentPasswordEditText.setVisibility(View.VISIBLE);
            newPasswordEditText.setVisibility(View.VISIBLE);
            saveChangesButton.setVisibility(View.VISIBLE);
        });

        // Setup the save changes button
        saveChangesButton.setOnClickListener(view -> {
            if (currentPasswordEditText.getVisibility() == View.VISIBLE && !newPasswordEditText.getText().toString().trim().isEmpty()) {
                changeUserPassword();
            } else {
                saveUserInformation();  // Call this when not changing password
            }
        });

        // Setup the cancel button
        cancelButton.setOnClickListener(view -> finish());  // Just finish the activity
    }

    private void changeUserPassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all password details", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(this, "Password successfully updated", Toast.LENGTH_SHORT).show();
                            saveUserInformation(); // Update user information after password change
                        } else {
                            Toast.makeText(this, "Error updating password: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Reauthentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = database.child(user.getUid());
            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("firstName", firstNameEditText.getText().toString().trim());
            userUpdates.put("lastName", lastNameEditText.getText().toString().trim());
            userUpdates.put("email", emailEditText.getText().toString().trim());
            userUpdates.put("wheatonID", widEditText.getText().toString().trim());
            ref.updateChildren(userUpdates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "User info updated successfully.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update user info.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
